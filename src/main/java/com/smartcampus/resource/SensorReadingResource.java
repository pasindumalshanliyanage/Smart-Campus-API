package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found");
            error.put("sensorId", sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        List<SensorReading> readings = DataStore.getReadings().get(sensorId);
        if (readings == null) {
            readings = new ArrayList<>();
        }
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading readingRequest) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found");
            error.put("sensorId", sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        String readingId = UUID.randomUUID().toString();
        SensorReading reading = new SensorReading(readingId, System.currentTimeMillis(), readingRequest.getValue());

        // Save reading
        DataStore.getReadings().computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        // Update sensor currentValue
        sensor.setCurrentValue(reading.getValue());

        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + readingId);
        return Response.created(location).entity(reading).build();
    }
}
