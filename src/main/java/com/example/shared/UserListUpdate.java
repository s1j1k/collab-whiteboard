package com.example.shared;

import java.io.Serializable;
import java.util.List;

public class UserListUpdate implements Serializable {
    private final List<String> usernames;

    public UserListUpdate(List<String> usernames) {
        this.usernames = usernames;
    }

    public List<String> getUsernames() {
        return usernames;
    }
}
