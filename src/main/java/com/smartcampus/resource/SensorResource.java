package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.*;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @POST
    public Response registerSensor(Sensor sensor) {
        // Validate roomId existence
        Room room = DataStore.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }

        // Save sensor
        DataStore.getSensors().put(sensor.getId(), sensor);

        // Add sensor id to room's list if not already present
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> allSensors = DataStore.getSensors().values();
        
        if (type == null || type.isEmpty()) {
            return Response.ok(allSensors).build();
        }

        List<Sensor> filtered = new ArrayList<>();
        for (Sensor s : allSensors) {
            if (s.getType().equalsIgnoreCase(type)) {
                filtered.add(s);
            }
        }
        return Response.ok(filtered).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found");
            error.put("sensorId", sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(sensor).build();
    }

    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
