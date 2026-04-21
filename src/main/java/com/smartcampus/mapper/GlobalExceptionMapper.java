package com.smartcampus.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // If it's a standard JAX-RS exception (like 404, 405, 415), respect its status code
        if (exception instanceof javax.ws.rs.WebApplicationException) {
            javax.ws.rs.WebApplicationException wae = (javax.ws.rs.WebApplicationException) exception;
            Response response = wae.getResponse();
            
            Map<String, String> body = new LinkedHashMap<>();
            body.put("error", Response.Status.fromStatusCode(response.getStatus()).getReasonPhrase());
            body.put("details", exception.getMessage());
            
            return Response.status(response.getStatus())
                    .entity(body)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        LOGGER.log(Level.SEVERE, "An unexpected error occurred", exception);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", "Internal server error");
        body.put("details", "An unexpected error occurred. Please contact support.");
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
