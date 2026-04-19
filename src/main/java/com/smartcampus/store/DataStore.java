package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();

    static {
        // Sample Rooms
        Room r1 = new Room("room-101", "Main auditorium", 250, new ArrayList<>());
        Room r2 = new Room("room-202", "Computing Lab 1", 40, new ArrayList<>());
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        // Sample Sensors
        Sensor s1 = new Sensor("sn-9001", "TEMPERATURE", "ACTIVE", 22.5, "room-101");
        Sensor s2 = new Sensor("sn-9002", "CO2", "ACTIVE", 450.0, "room-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);

        // Link sensors to room
        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
    }

    public static ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }

    public static ConcurrentHashMap<String, Sensor> getSensors() {
        return sensors;
    }
}
