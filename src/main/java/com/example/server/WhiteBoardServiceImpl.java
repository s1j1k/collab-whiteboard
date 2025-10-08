package com.example.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import com.example.shared.WhiteBoardService;

public class WhiteBoardServiceImpl implements WhiteBoardService {
    private final List<Point> drawing = new ArrayList<>();

    @Override
    public synchronized void drawPoint(Point p, String color) throws RemoteException {
        drawing.add(p);
        System.out.println("Point drawn at: " + p);
    }

    @Override
    public synchronized List<Point> getCurrentDrawing() throws RemoteException {
        return new ArrayList<>(drawing);
    }
}
