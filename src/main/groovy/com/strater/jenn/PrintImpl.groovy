package com.strater.jenn

import groovy.util.logging.Slf4j
import java.rmi.RemoteException
import java.sql.Timestamp

@Slf4j
class PrintImpl implements Print {
    Auth auth = new Auth()

    String defaultErrorMessage = 'Invalid Credentials'

    List<PrintJob> printQueue = []
    Map<String, String> config = [:]
    String printerStatus = 'Ready'
    Integer jobNumber = 1

    Map authorizationMechanisms = [
            1: 'authenticateAndAuthorizeWithACL',
            2: 'authenticateAndAuthorizeWithRbac'
    ]

    Integer print(String filename, String printer, String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'print')) {
            log.info 'user ' + username + ' invoked print'
            return print(filename, printer)
        }
        return -1
    }

    Integer print(String filename, String printer, String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'print')
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

    String queue(String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'queue')) {
            log.info 'user ' + username + ' invoked queue'
            return queue()
        }
        return defaultErrorMessage
    }

    String queue(String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'queue')
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

    String topQueue(Integer job, String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'topQueue')) {
            log.info 'user ' + username + ' invoked topQueue'
            return topQueue(job)
        }
        return defaultErrorMessage
    }

    String topQueue(Integer job, String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'topQueue')
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

    String start(String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'start')) {
            log.info 'user ' + username + ' invoked start'
            return start()
        }
        return defaultErrorMessage

    }

    String start(String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'start')
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

    String stop(String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'stop')) {
            log.info 'user ' + username + ' invoked stop'
            return stop()
        }
        return defaultErrorMessage

    }

    String stop(String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'stop')
        if (username) {
            log.info 'user ' + username + ' invoked stop'
            return stop()
        }
        return defaultErrorMessage
    }

    String stop() throws RemoteException {
        log.info 'Stopping Print Server'
        return 'Success'
    }

    String restart(String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'restart')) {
            log.info 'user ' + username + ' invoked restart'
            return restart()
        }
        return defaultErrorMessage
    }

    String restart(String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'restart')
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

    String status(String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'status')) {
            log.info 'user ' + username + ' invoked printerStatus'
            return status()
        }
        return defaultErrorMessage
    }

    String status(String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'status')
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

    String readConfig(String parameter, String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'readConfig')) {
            log.info 'user ' + username + ' invoked readConfig'
            return readConfig(parameter)
        }
        return defaultErrorMessage
    }

    String readConfig(String parameter, String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'readConfig')
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

    String setConfig(String parameter, String value, String username, String password, Integer authorizationMechanism) {
        if (auth."${authorizationMechanisms[authorizationMechanism]}"(username, password, 'setConfig')) {
            log.info 'user ' + username + ' invoked setConfig'
            return setConfig(parameter, value)
        }
        return defaultErrorMessage
    }

    String setConfig(String parameter, String value, String sessionId, Integer authorizationMechanism) {
        String username = auth."${authorizationMechanisms[authorizationMechanism]}"(sessionId, 'setConfig')
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

    private class PrintJob {
        Integer jobNumber
        String filename
    }

    PrintJob createPrintJob(String fileName) {
        PrintJob job = new PrintJob(jobNumber: this.jobNumber, filename: fileName)
        jobNumber++
        return job
    }

    String startSession(String username, String password) {
        if (auth.authenticate(username, password)) {
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
