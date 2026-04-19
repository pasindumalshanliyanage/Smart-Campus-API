package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @POST
    public Response registerSensor(Sensor sensor) {
        // Validate roomId existence
        Room room = DataStore.getRooms().get(sensor.getRoomId());
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Linked resource not found");
            error.put("details", "Room with id '" + sensor.getRoomId() + "' does not exist.");
            return Response.status(422).entity(error).build(); // 422 Unprocessable Entity
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
}
