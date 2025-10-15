package com.example.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Messages to be sent from Server to Client
 */

public interface WhiteBoardClientInterface extends Remote {
    void receiveDrawAction(DrawAction action) throws RemoteException;
}
