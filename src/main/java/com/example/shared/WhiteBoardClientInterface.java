package com.example.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WhiteBoardClientInterface extends Remote {
    void receiveDrawAction(DrawAction action) throws RemoteException;
}
