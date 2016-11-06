package com.strater.jenn

import groovy.util.logging.Slf4j
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.rmi.server.UnicastRemoteObject

@Slf4j
class Server {
    static void main(String[] args) throws RemoteException {
        try {
            String serviceName = 'print'
            PrintImpl servant = new PrintImpl()
            Print stub = (Print) UnicastRemoteObject.exportObject(servant, 0)

            Registry registry = LocateRegistry.createRegistry(1099)
            registry.rebind(serviceName, stub)

            Database.setupTestUsers()

            log.info 'Print Server ready'
        } catch (RemoteException e) {
            log.error "Print Service Exception: ${e.toString()}"
            log.error ('stacktrace: ', e)
        }
    }
}
