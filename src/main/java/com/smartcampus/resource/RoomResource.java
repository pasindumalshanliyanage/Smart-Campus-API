package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.*;
import java.util.UUID;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Response getAllRooms() {
        Collection<Room> rooms = DataStore.getRooms().values();
        return Response.ok(rooms).build();
    }

    @POST
    public Response createRoom(Room roomRequest) {
        String id = UUID.randomUUID().toString();
        Room newRoom = new Room(id, roomRequest.getName(), roomRequest.getCapacity(), new ArrayList<>());
        
        DataStore.getRooms().put(id, newRoom);
        
        URI location = URI.create("/api/v1/rooms/" + id);
        return Response.created(location).entity(newRoom).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found");
            error.put("roomId", roomId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        DataStore.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
