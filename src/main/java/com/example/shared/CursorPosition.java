package com.example.shared;

import java.io.Serializable;

/**
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class CursorPosition implements Serializable {
    public String username;
    public int x, y;
    public long timestamp;

    public CursorPosition(String username, int x, int y) {
        this.username = username;
        this.x = x;
        this.y = y;
    }
}
