package com.example.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * Define what messages can be sent from Client to Server
 */

public interface WhiteboardServerInterface extends Remote {
    void registerClient(WhiteBoardClientInterface client) throws RemoteException;
    void sendMessage(String username, String message) throws RemoteException;
    void receiveDrawAction(DrawAction action) throws RemoteException;
    // void sendDrawAction(DrawAction action) throws RemoteException;
}