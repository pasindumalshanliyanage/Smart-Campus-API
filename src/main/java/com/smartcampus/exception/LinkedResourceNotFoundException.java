package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    private final String roomId;

    public LinkedResourceNotFoundException(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
