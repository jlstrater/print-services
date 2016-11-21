package com.strater.jenn

import java.rmi.Remote
import java.rmi.RemoteException

interface Print extends Remote {
    Integer print(String filename, String printer, String username, String password, Integer authorizationMechanism)
            throws RemoteException
    Integer print(String filename, String printer, String sessionId, Integer authorizationMechanism)
            throws RemoteException

    String queue(String username, String password, Integer authorizationMechanism) throws RemoteException
    String queue(String sessionId, Integer authorizationMechanism) throws RemoteException

    String topQueue(Integer job, String username, String password, Integer authorizationMechanism)
            throws RemoteException
    String topQueue(Integer job, String sessionId, Integer authorizationMechanism) throws RemoteException

    String start(String username, String password, Integer authorizationMechanism) throws RemoteException
    String start(String sessionId, Integer authorizationMechanism) throws RemoteException

    String stop(String username, String password, Integer authorizationMechanism) throws RemoteException
    String stop(String sessionId, Integer authorizationMechanism) throws RemoteException

    String restart(String username, String password, Integer authorizationMechanism) throws RemoteException
    String restart(String sessionId, Integer authorizationMechanism) throws RemoteException

    String status(String username, String password, Integer authorizationMechanism) throws RemoteException
    String status(String sessionId, Integer authorizationMechanism) throws RemoteException

    String readConfig(String parameter, String username, String password, Integer authorizationMechanism)
            throws RemoteException
    String readConfig(String parameter, String sessionId, Integer authorizationMechanism) throws RemoteException

    String setConfig(String parameter, String value, String username, String password, Integer authorizationMechanism)
            throws RemoteException
    String setConfig(String parameter, String value, String sessionId, Integer authorizationMechanism)
            throws RemoteException

    String startSession(String username, String password) throws RemoteException
}
