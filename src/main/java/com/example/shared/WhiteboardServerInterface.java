package com.example.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Define what messages can be sent from Client to Server
 */

public interface WhiteBoardServerInterface extends Remote {
    void registerClient(String username, WhiteBoardClientInterface client) throws RemoteException;

    void unregisterClient(String username) throws RemoteException;

    void sendChatMessage(ChatMessage message) throws RemoteException;

    void broadcastDrawAction(DrawAction action) throws RemoteException;

    byte[] getCanvasSnapshot() throws RemoteException;

    ArrayList<ChatMessage> getChatHistory() throws RemoteException;

    void broadcastCursorPosition(CursorPosition position) throws RemoteException;

    // void broadcastUserList() throws RemoteException;

    void kickUser(String username) throws RemoteException;

    // void broadcastCanvasImage(byte[] ) throws RemoteException;

}