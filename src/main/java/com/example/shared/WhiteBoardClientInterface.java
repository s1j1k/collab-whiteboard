package com.example.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Messages to be sent from Server to Client
 */

public interface WhiteBoardClientInterface extends Remote {
    void receiveDrawAction(DrawAction action) throws RemoteException;
    void receiveChatMessage(ChatMessage message) throws RemoteException;
}
