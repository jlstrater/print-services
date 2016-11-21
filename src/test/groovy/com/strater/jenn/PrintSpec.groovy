package com.strater.jenn

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.rmi.Naming

@Unroll
class PrintSpec extends Specification {

    @Shared
    Print printService

    @Shared
    String sessionId

    static String username = 'testAdmin'
    static String password = 'test123'

    def setupSpec() {
        //if there is no server running, this will start a new one. If one is already running or has an error, it will print the stack trace and continue.
        Server server = new Server()
        server.main()

        printService = Naming.lookup('rmi://localhost:1099/print') as Print
        sessionId = printService.startSession(username, password)
    }

    def setup() {
        printService.restart(sessionId, 1)
    }

    def 'add print job with credentials'() {
        when:
        String response = printService.print('file.txt', 'myPrinter', username, password, 1)

        then:
        response.toInteger() == 1
    }

    def 'add print job with sessionId'() {
        when:
        String response = printService.print('file.txt', 'myPrinter', sessionId, 1)

        then:
        response.toInteger() == 1
    }

    def 'attempt to add print job with bad credentials'() {
        when:
        String response = printService.print('file.txt', 'myPrinter', 'notFound', 'anything', 1)

        then:
        response.toInteger() == -1
    }

    def 'test empty queue response with credentials'() {
        when:
        String response = printService.queue(username, password, 1)

        then:
        !response
    }

    def 'test empty queue response with sessionId'() {
        when:
        String response = printService.queue(sessionId, 1)

        then:
        !response
    }

    def 'test nonempty queue response with credentials'() {
        setup:
        printService.print('test.txt', 'test suite', sessionId, 1)

        when:
        String response = printService.queue(username, password, 1)

        then:
        response == '<1> <test.txt>'
    }

    def 'test nonempty queue response with sessionId'() {
        setup:
        printService.print('test.txt', 'test suite', sessionId, 1)

        when:
        String response = printService.queue(sessionId, 1)

        then:
        response == '<1> <test.txt>'
    }

    def 'test queue with multiple jobs'() {
        setup:
        printService.print('test.txt', 'test suite', sessionId, 1)
        printService.print('test2.txt', 'test suite', sessionId, 1)

        when:
        String response = printService.queue(sessionId, 1)
        then:
        response == '''<1> <test.txt>\n<2> <test2.txt>'''
    }

    def 'test sending a job to the top of the queue with sessionId'() {
        setup: 'setup 3 jobs'
        printService.print('test.txt', 'test suite', sessionId, 1)
        printService.print('test2.txt', 'test suite', sessionId, 1)
        printService.print('test3.txt', 'test suite', sessionId, 1)

        when: 'the third job is sent to the top of the queue'
        String response = printService.topQueue(3, sessionId, 1)

        then: 'the third job should appear at the top'
        response == 'Moved job number 3 to the top of the queue'

        and:
        String newQueue = printService.queue(sessionId, 1)
        newQueue.startsWith('<3> <test3.txt>')
    }

    def 'test sending a job to the top of the queue with credentials'() {
        setup: 'setup 3 jobs'
        printService.print('test.txt', 'test suite', sessionId, 1)
        printService.print('test2.txt', 'test suite', sessionId, 1)
        printService.print('test3.txt', 'test suite', sessionId, 1)

        when: 'the third job is sent to the top of the queue'
        String response = printService.topQueue(3, username, password, 1)

        then: 'the third job should appear at the top'
        response == 'Moved job number 3 to the top of the queue'

        and:
        String newQueue = printService.queue(sessionId, 1)
        newQueue.startsWith('<3> <test3.txt>')
    }

    def 'test the mocked start method invokation with credentials'() {
        when:
        String response = printService.start(username, password, 1)

        then:
        response == 'Success'
    }

    def 'test the mocked start method invokation with sessionId'() {
        when:
        String response = printService.start(sessionId, 1)

        then:
        response == 'Success'
    }

    def 'test the mocked restart method invokation with credentials'() {
        when:
        String response = printService.restart(username, password, 1)

        then:
        response == 'Success'
    }

    def 'test the mocked restart method invokation with sessionId'() {
        when:
        String response = printService.restart(sessionId, 1)

        then:
        response == 'Success'
    }

    def 'test the printer status response with credentials'() {
        when:
        String response = printService.status(username, password, 1)

        then:
        response == 'Ready'
    }

    def 'test the printer status response with sessionId'() {
        when:
        String response = printService.status(sessionId, 1)

        then:
        response == 'Ready'
    }

    def 'test setting a config param when varname is #varName and value is #value'() {
        setup:
        String response = printService.setConfig(varName, value, sessionId, 1)

        expect:
        response == expected

        where:
        varName | value   | expected
        ''      | ''      | 'Success'
        null    | ''      | 'Success'
        null    | null    | 'Success'
        'myVar' | 'myVal' | 'Success'
    }

    def 'test reading a config paramter when #varName'() {
        setup:
        String response = printService.readConfig(varName, sessionId, 1)

        expect:
        !response

        where:
        varName << ['', 'notFound', null]
    }

    def 'test reading a config parameter when it exists'() {
        setup:
        printService.setConfig('myVar', 'myVal', sessionId, 1)

        when:
        String response = printService.readConfig('myVar', sessionId, 1)

        then:
        response == 'myVal'
    }

    def 'test session token creation for good credentials'() {
        when:
        String response = printService.startSession(username, password)

        then:
        response.size() == 64
        response.class == String
    }

    def 'test session token creation for bad credentials'() {
        when:
        String response = printService.startSession('test', 'bad password')

        then:
        response == 'Invalid Credentials'
    }

    def 'test session token creation when user does not exist'() {
        when:
        String response = printService.startSession('notFound', 'anything')

        then:
        response == 'Invalid Credentials'
    }

    def 'test use of old session token'() {
        setup:
        String oldSessionId = printService.startSession(username, password)
        String newSessionId = printService.startSession(username, password)

        when:
        String badResponse = printService.status(oldSessionId, 1)

        then:
        badResponse == 'Invalid Credentials'

        when:
        String goodResponse = printService.status(newSessionId, 1)

        then:
        goodResponse == 'Ready'
    }
}
