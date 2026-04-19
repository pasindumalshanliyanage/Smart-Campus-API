package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
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
}
