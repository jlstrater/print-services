package com.strater.jenn

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import java.rmi.RemoteException
import java.sql.Timestamp

@Slf4j
class PrintImpl implements Print {

    String defaultErrorMessage = 'Invalid Credentials'

    List<PrintJob> printQueue = []
    Map<String, String> config = [:]
    String printerStatus = 'Ready'
    Integer jobNumber = 1

    Integer print(String filename, String printer, String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' invoked print'
            return print(filename, printer)
        }
        return -1
    }

    Integer print(String filename, String printer, String sessionId) {
        String username = authenticate(sessionId)
        if (username) {
            log.info 'user ' + username + ' invoked print'
            return print(filename, printer)
        }
        return -1
    }

    Integer print(String filename, String printer) throws RemoteException {
        String message = "Adding $filename to print queue for printer $printer"
        log.info message
        PrintJob job = createPrintJob(filename)
        printQueue.add(job)
        printerStatus = 'Ready'
        return job.jobNumber
    }

    String queue(String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' invoked queue'
            return queue()
        }
        return defaultErrorMessage
    }

    String queue(String sessionId) {
        String username = authenticate(sessionId)
        if (username) {
            log.info 'user ' + username + ' invoked queue'
            return queue()
        }
        return defaultErrorMessage
    }

    String queue() throws RemoteException {
        log.info 'Print Queue: '
        String output = printQueue.collect {
            '<' + it.jobNumber + '> <' + it.filename + '>'
        }.join('\n')
        log.info output
        output
    }

    String topQueue(Integer job, String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' invoked topQueue'
            return topQueue(job)
        }
        return defaultErrorMessage
    }

    String topQueue(Integer job, String sessionId) {
        String username = authenticate(sessionId)
        if (username) {
            log.info 'user ' + username + ' invoked topQueue'
            return topQueue(job)
        }
        return defaultErrorMessage
    }

    String topQueue(Integer job) throws RemoteException {
        String message = "Moved job number $job to the top of the queue"
        log.info message
        PrintJob topJob = printQueue.find { it.jobNumber == job }
        printQueue.remove(topJob)
        printQueue.add(0, topJob)
        return message
    }

    String start(String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' invoked start'
            return start()
        }
        return defaultErrorMessage

    }

    String start(String sessionId) {
        String username = authenticate(sessionId)
        if (username) {
            log.info 'user ' + username + ' invoked start'
            return start()
        }
        return defaultErrorMessage

    }

    String start() throws RemoteException {
        log.info 'Started Print Server'
        return 'Success'
    }

    String restart(String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' invoked restart'
            return restart()
        }
        return defaultErrorMessage
    }

    String restart(String sessionId) {
        String username = authenticate(sessionId)
        if (username) {
            log.info 'user ' + username + ' invoked restart'
            return restart()
        }
        return defaultErrorMessage
    }

    String restart() throws RemoteException {
        log.info 'Stopping Print Server'
        printQueue = []
        jobNumber = 1
        start()
        return 'Success'
    }

    String status(String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' invoked printerStatus'
            return status()
        }
        return defaultErrorMessage
    }

    String status(String sessionId) {
        String username = authenticate(sessionId)
        if (username) {
            log.info 'user ' + username + ' invoked printerStatus'
            return status()
        }
        return defaultErrorMessage
    }

    String status() throws RemoteException {
        log.info "Print Status: $printerStatus"
        return printerStatus
    }

    String readConfig(String parameter, String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' invoked readConfig'
            return readConfig(parameter)
        }
        return defaultErrorMessage
    }

    String readConfig(String parameter, String sessionId) {
        String username = authenticate(sessionId)
        if (username) {
            log.info 'user ' + username + ' invoked readConfig'
            return readConfig(parameter)
        }
        return defaultErrorMessage

    }

    String readConfig(String parameter) throws RemoteException {
        log.info "Reading config parameter $parameter"
        return config[parameter]
    }

    String setConfig(String parameter, String value, String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' invoked setConfig'
            return setConfig(parameter, value)
        }
        return defaultErrorMessage
    }

    String setConfig(String parameter, String value, String sessionId) {
        String username = authenticate(sessionId)
        if (username) {
            log.info 'user ' + username + ' invoked setConfig'
            return setConfig(parameter, value)
        }
        return defaultErrorMessage
    }

    String setConfig(String parameter, String value) throws RemoteException {
        log.info "Setting config parameter $parameter to $value"
        config[parameter] = value
        return 'Success'
    }

    class PrintJob {
        Integer jobNumber
        String filename
    }

    PrintJob createPrintJob(String fileName) {
        PrintJob job = new PrintJob(jobNumber: this.jobNumber, filename: fileName)
        jobNumber++
        return job
    }

    Boolean authenticate(String username, String password) {
        Sql db = Database.db
        GroovyRowResult row = db.firstRow('select password from users where username=:username', [username: username])

        return Crypto.compare(username, password, row?.get('password')?.toString())
    }

    String authenticate(String sessionId) {
        Sql db = Database.db
        GroovyRowResult row = db.firstRow('select username from users where sessionId=:sessionId and ' +
                'expiration >= :now',
                [sessionId: sessionId, now: new Timestamp(System.currentTimeMillis())])
        return row?.get('username')
    }

    String startSession(String username, String password) {
        if (authenticate(username, password)) {
            log.info 'user ' + username + ' started a new session'
            String sessionId = Crypto.generateSessionId()
            Timestamp expiration = new Timestamp(System.currentTimeMillis() + 1800 * 1000)
            Database.db.executeUpdate('update users set (sessionId, expiration) = (:sessionId, :expiration) ' +
                    'where username = :username',
                    [username: username, sessionId: sessionId, expiration: expiration])
            return sessionId
        }
        return 'Invalid Credentials'
    }
}
