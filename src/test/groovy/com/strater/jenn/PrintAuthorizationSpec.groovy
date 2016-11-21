package com.strater.jenn

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.rmi.Naming

@Unroll
class PrintAuthorizationSpec extends Specification {

    @Shared
    Print printService

    @Shared
    String sessionId

    static String adminUsername = 'admin'
    static String adminPassword = 'supersecret456'

    static String technicianUsername = 'bobTheBuilder'
    static String technicianPassword = 'nightCrewRulez'

    static String userUsername = 'oridinaryJoe'
    static String userPassword = 'password'

    static String powerUsername = 'powerUser1'
    static String powerUserPassword = 'supersecret123'

    def setupSpec() {
        //if there is no server running, this will start a new one. If one is already running or has an error, it will print the stack trace and continue.
        Server server = new Server()
        server.main()

        printService = Naming.lookup('rmi://localhost:1099/print') as Print
        sessionId = printService.startSession(adminUsername, adminPassword)
    }

    def setup() {
        //reset all test data between tests
        printService.restart(sessionId, 1)
    }

    def 'add print job with #username credentials and #mechanismName and expect authorization'() {
        when:
        String response = printService.print('file.txt', 'myPrinter', username, password, mechanismId)

        then:
        response.toInteger() == 1

        where:
        username | password | mechanismId | mechanismName
        adminUsername | adminPassword | 1 | 'ACL'
        adminUsername | adminPassword | 2 | 'RBAC'
        userUsername | userPassword | 1 | 'ACL'
        userUsername | userPassword | 2 | 'RBAC'
        powerUsername | powerUserPassword | 1 | 'ACL'
        powerUsername | powerUserPassword | 2 | 'RBAC'
    }

    def 'add print job with #username credentials and #mechanismName and expect unauthorized'() {
        when:
        String response = printService.print('file.txt', 'myPrinter', username, password, mechanismId)

        then:
        response.toInteger() == -1

        where:
        username | password | mechanismId | mechanismName
        technicianUsername | technicianPassword | 1 | 'ACL'
        technicianUsername | technicianPassword | 2 | 'RBAC'
    }

    def 'test nonempty queue response with #username credentials and #mechanismName and expect authorization'() {
        setup:
        printService.print('test.txt', 'test suite', sessionId, 1)

        when:
        String response = printService.queue(username, password, mechanismId)

        then:
        response == '<1> <test.txt>'

        where:
        username | password | mechanismId | mechanismName
        adminUsername | adminPassword | 1 | 'ACL'
        adminUsername | adminPassword | 2 | 'RBAC'
        userUsername | userPassword | 1 | 'ACL'
        userUsername | userPassword | 2 | 'RBAC'
        powerUsername | powerUserPassword | 1 | 'ACL'
        powerUsername | powerUserPassword | 2 | 'RBAC'
    }

    def 'test nonempty queue response with #username credentials and #mechanismName and expect unauthorized'() {
        setup:
        printService.print('test.txt', 'test suite', sessionId, 1)

        when:
        String response = printService.queue(username, password, mechanismId)

        then:
        response == 'Invalid Credentials'

        where:
        username | password | mechanismId | mechanismName
        technicianUsername | technicianPassword | 1 | 'ACL'
        technicianUsername | technicianPassword | 2 | 'RBAC'
    }

    def 'test sending a job to the top of the queue with #username credentials and #mechanismName and expect auth'() {
        setup: 'setup 3 jobs'
        printService.print('test.txt', 'test suite', sessionId, 1)
        printService.print('test2.txt', 'test suite', sessionId, 1)
        printService.print('test3.txt', 'test suite', sessionId, 1)

        when: 'the third job is sent to the top of the queue'
        String response = printService.topQueue(3, username, password, mechanismId)

        then: 'the third job should appear at the top'
        response == 'Moved job number 3 to the top of the queue'

        and:
        String newQueue = printService.queue(sessionId, mechanismId)
        newQueue.startsWith('<3> <test3.txt>')

        where:
        username | password | mechanismId | mechanismName
        adminUsername | adminPassword | 1 | 'ACL'
        adminUsername | adminPassword | 2 | 'RBAC'
        powerUsername | powerUserPassword | 1 | 'ACL'
        powerUsername | powerUserPassword | 2 | 'RBAC'
    }

    def 'test sending a job to the top of the queue with #username credentials and #mechanismName and expect unauth'() {
        setup: 'setup 3 jobs'
        printService.print('test.txt', 'test suite', sessionId, 1)
        printService.print('test2.txt', 'test suite', sessionId, 1)
        printService.print('test3.txt', 'test suite', sessionId, 1)

        when: 'the third job is sent to the top of the queue'
        String response = printService.topQueue(3, username, password, mechanismId)

        then:
        response == 'Invalid Credentials'

        and:
        String newQueue = printService.queue(sessionId, 1)
        newQueue.startsWith('<1> <test.txt>') //verify the old ordering still holds

        where:
        username | password | mechanismId | mechanismName
        technicianUsername | technicianPassword | 1 | 'ACL'
        technicianUsername | technicianPassword | 2 | 'RBAC'
        userUsername | userPassword | 1 | 'ACL'
        userUsername | userPassword | 2 | 'RBAC'
    }

    def 'test the mocked start method invokation with #username credentials and #mechanismName and expect auth'() {
        when:
        String response = printService.start(username, password, mechanismId)

        then:
        response == 'Success'

        where:
        username | password | mechanismId | mechanismName
        adminUsername | adminPassword | 1 | 'ACL'
        adminUsername | adminPassword | 2 | 'RBAC'
        technicianUsername | technicianPassword | 1 | 'ACL'
        technicianUsername | technicianPassword | 2 | 'RBAC'
    }

    def 'test the mocked start method invokation with #username credentials and #mechanismName and expect unauth'() {
        when:
        String response = printService.start(username, password, mechanismId)

        then:
        response == 'Invalid Credentials'

        where:
        username | password | mechanismId | mechanismName
        userUsername | userPassword | 1 | 'ACL'
        userUsername | userPassword | 2 | 'RBAC'
        powerUsername | powerUserPassword | 1 | 'ACL'
        powerUsername | powerUserPassword | 2 | 'RBAC'
    }

    def 'test the mocked restart method invokation with #username credentials and #mechanismName and expect unauth'() {
        when:
        String response = printService.restart(username, password, mechanismId)

        then:
        response == 'Invalid Credentials'

        where:
        username | password | mechanismId | mechanismName
        userUsername | userPassword | 1 | 'ACL'
        userUsername | userPassword | 2 | 'RBAC'
    }

    def 'test the mocked restart method invokation with #username credentials and #mechanismName and expect auth'() {
        when:
        String response = printService.restart(username, password, mechanismId)

        then:
        response == 'Success'

        where:
        username | password | mechanismId | mechanismName
        adminUsername | adminPassword | 1 | 'ACL'
        adminUsername | adminPassword | 2 | 'RBAC'
        powerUsername | powerUserPassword | 1 | 'ACL'
        powerUsername | powerUserPassword | 2 | 'RBAC'
        technicianUsername | technicianPassword | 1 | 'ACL'
        technicianUsername | technicianPassword | 2 | 'RBAC'
    }

    def 'test the printer status response with #username credentials and #mechanismName and expect authorized'() {
        when:
        String response = printService.status(username, password, mechanismId)

        then:
        response == 'Ready'

        where:
        username | password | mechanismId | mechanismName
        adminUsername | adminPassword | 1 | 'ACL'
        adminUsername | adminPassword | 2 | 'RBAC'
        technicianUsername | technicianPassword | 1 | 'ACL'
        technicianUsername | technicianPassword | 2 | 'RBAC'
    }

    def 'test the printer status response with #username credentials and #mechanismName and expect unauthorized'() {
        when:
        String response = printService.status(username, password, mechanismId)

        then:
        response == 'Invalid Credentials'

        where:
        username | password | mechanismId | mechanismName
        userUsername | userPassword | 1 | 'ACL'
        userUsername | userPassword | 2 | 'RBAC'
        powerUsername | powerUserPassword | 1 | 'ACL'
        powerUsername | powerUserPassword | 2 | 'RBAC'
    }

    def 'test reading a config parameter with #username credentials and #mechanismName and expect authorized'() {
        setup:
        printService.setConfig('myVar', 'myVal', username, password, mechanismId)

        when:
        String response = printService.readConfig('myVar', username, password, mechanismId)

        then:
        response == 'myVal'

        where:
        username | password | mechanismId | mechanismName
        technicianUsername | technicianPassword | 1 | 'ACL'
        technicianUsername | technicianPassword | 2 | 'RBAC'
        adminUsername | adminPassword | 1 | 'ACL'
        adminUsername | adminPassword | 2 | 'RBAC'
    }

    def 'test reading a config parameter with #username credentials and expect unauthorized'() {
        setup:
        printService.setConfig('myVar', 'myVal', username, password, mechanismId)

        when:
        String response = printService.readConfig('myVar', username, password, mechanismId)

        then:
        response == 'Invalid Credentials'

        where:
        username | password | mechanismId | mechanismName
        userUsername | userPassword | 1 | 'ACL'
        userUsername | userPassword | 2 | 'RBAC'
        powerUsername | powerUserPassword | 1 | 'ACL'
        powerUsername | powerUserPassword | 2 | 'RBAC'
    }

    def 'test removing user roles but keep authentication'() {
        setup:
        Database.db.execute('update users set role=:role where username=:username',
                [role: 'TERMINATED', username: 'bobTheBuilder'])

        when:
        String response = printService.restart(technicianUsername, technicianPassword, 1)

        then:
        response == 'Invalid Credentials'
    }

    def 'test adding new user with existing role'() {
        setup:
        String insert = 'insert into users (id, username, password, role) values(:id, :username, :password, :role)'
        Database.db.execute(insert, [id: 6, username: 'george', password: Crypto.encrypt('george', 'nightCrewRulez'),
                                     role: 'TECHNICIAN'])

        when:
        String response = printService.restart('george', technicianPassword, 1)

        then:
        response == 'Success'
    }

    def 'test promoting bob to an admin'() {
        setup:
        Database.db.execute('update users set role=:role where username=:username',
                [role: 'ADMIN', username: 'bobTheBuilder'])

        when:
        String response = printService.print('nightshiftstuff.txt', 'myPrinter', technicianUsername,
                technicianPassword, 1)

        then:
        response.toInteger() == 1
    }

    def 'test demoting power user to regular user'() {
        setup:
        Database.db.execute('update users set role=:role where username=:username',
                [role: 'USER', username: 'powerUser1'])

        when:
        String response = printService.restart(powerUsername, powerUserPassword, 1)

        then:
        response == 'Invalid Credentials'
    }
}
