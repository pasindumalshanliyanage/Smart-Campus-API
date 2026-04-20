package com.smartcampus.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", "Linked resource not found");
        body.put("details", "Room with id '" + exception.getRoomId() + "' does not exist in the system.");
        
        return Response.status(422) // Unprocessable Entity
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
