package com.strater.jenn

import java.rmi.Remote
import java.rmi.RemoteException

interface Print extends Remote {
    Integer print(String filename, String printer, String username, String password) throws RemoteException
    Integer print(String filename, String printer, String sessionId) throws RemoteException

    String queue(String username, String password) throws RemoteException
    String queue(String sessionId) throws RemoteException

    String topQueue(Integer job, String username, String password) throws RemoteException
    String topQueue(Integer job, String sessionId) throws RemoteException

    String start(String username, String password) throws RemoteException
    String start(String sessionId) throws RemoteException

    String restart(String username, String password) throws RemoteException
    String restart(String sessionId) throws RemoteException

    String status(String username, String password) throws RemoteException
    String status(String sessionId) throws RemoteException

    String readConfig(String parameter, String username, String password) throws RemoteException
    String readConfig(String parameter, String sessionId) throws RemoteException

    String setConfig(String parameter, String value, String username, String password) throws RemoteException
    String setConfig(String parameter, String value, String sessionId) throws RemoteException

    String startSession(String username, String password) throws RemoteException
}
