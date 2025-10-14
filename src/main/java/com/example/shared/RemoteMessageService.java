package com.example.shared;

// RemoteMessageService.java
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMessageService extends Remote {
    void sendMessage(String username, String message) throws RemoteException;
}