package com.example.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.awt.Point;
import java.util.List;

public interface WhiteBoardService extends Remote {
    void drawPoint(Point p, String color) throws RemoteException;
    List<Point> getCurrentDrawing() throws RemoteException;
}
