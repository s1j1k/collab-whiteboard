package com.example.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.example.shared.WhiteBoardService;

public class WhiteBoardServer {
    private WhiteBoardServiceImpl serviceImpl;

    public WhiteBoardServer() {
        this.serviceImpl = new WhiteBoardServiceImpl();
    }

    public void start() throws Exception {
        WhiteBoardService stub = (WhiteBoardService) UnicastRemoteObject.exportObject(serviceImpl, 0);
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("WhiteBoardService", stub);
        System.out.println("WhiteBoardService bound to RMI registry.");
    }

    public WhiteBoardService getService() {
        return serviceImpl;
    }
}
