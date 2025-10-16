package com.example.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * Define what messages can be sent from Client to Server
 */

public interface WhiteBoardServerInterface extends Remote {
    boolean registerClient(String username, WhiteBoardClientInterface client) throws RemoteException;
    void unregisterClient(String username) throws RemoteException;
    void sendChatMessage(ChatMessage message) throws RemoteException;
    void broadcastDrawAction(DrawAction action) throws RemoteException;
}