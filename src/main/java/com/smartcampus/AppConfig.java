package com.smartcampus;

import com.smartcampus.mapper.*;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        register(DiscoveryResource.class);
        register(RoomResource.class);
        register(SensorResource.class);
        register(com.smartcampus.resource.SensorReadingResource.class);
        register(JacksonFeature.class);
        
        // Exception Mappers
        register(RoomNotEmptyExceptionMapper.class);
        register(LinkedResourceNotFoundExceptionMapper.class);
        register(SensorUnavailableExceptionMapper.class);
        register(GlobalExceptionMapper.class);
        
        // Filters
        register(LoggingFilter.class);
    }
}
