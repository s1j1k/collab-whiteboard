package com.example.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Messages to be sent from Server to Client
 * 
 * @author Sally Arnold
 */
public interface WhiteBoardClientInterface extends Remote {
    void onRegistrationApproved() throws RemoteException;
    void onRegistrationRejected(String reason) throws RemoteException;
    
    void receiveDrawAction(DrawAction action) throws RemoteException;

    void receiveChatMessage(ChatMessage message) throws RemoteException;

    void updateCursorPosition(CursorPosition position) throws RemoteException;

    void receiveUserListUpdate(UserListUpdate update) throws RemoteException;

    void kickedFromServer() throws RemoteException;

    void receiveCanvasImage(byte[] imageData) throws RemoteException;

    void onServerShutdown(String reason) throws RemoteException;



}
