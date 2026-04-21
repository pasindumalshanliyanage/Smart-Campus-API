# Smart Campus Sensor & Room Management API

## 1. Overview
The Smart Campus Sensor & Room Management API is a specialized RESTful backend system designed to orchestrate the complex ecosystem of a modern, technology-driven university campus. By providing a centralized management layer for physical spaces (rooms) and their associated environmental monitoring devices (sensors), the API enables administrators and automated systems to monitor occupancy, capacity, and real-time sensor data across the institution's facilities. The system is architected to handle high-frequency data ingestion from sensors while maintaining strict business rules regarding spatial relationships and occupancy constraints.

Technologically, the project is built on the robust JAX-RS (Jersey 2.x) framework, leveraging an embedded Grizzly HTTP server for high-performance service delivery without the overhead of a heavyweight application server. Data serialization and deserialization are handled by the Jackson JSON library, ensuring seamless interoperability with modern client applications. The project utilizes a Maven-based build system to manage dependencies and is compatible with Java 11 or higher, adhering to established J2EE/Jakarta EE patterns for enterprise-grade software development.

The API exposes a well-defined resource hierarchy starting with a central discovery endpoint at `/api/v1` for navigational metadata. Room management is handled via the `/rooms` endpoint, supporting collection-level operations and single-resource management at `/rooms/{roomId}`. Sensors are managed through the `/sensors` resource, which includes advanced filtering capabilities via query parameters. Finally, sensor telemetry is managed through a sub-resource locator pattern at `/sensors/{sensorId}/readings`, providing an isolated domain for managing high-volume reading history.

- `/api/v1` &rarr; Discovery
- `/api/v1/rooms` &rarr; Room management
- `/api/v1/rooms/{roomId}` &rarr; Single room operations
- `/api/v1/sensors` &rarr; Sensor management
- `/api/v1/sensors?type={type}` &rarr; Filtered sensor list
- `/api/v1/sensors/{sensorId}/readings` &rarr; Reading history (sub-resource)

## 2. Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- Git

## 3. Build & Run Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/pasindumalshanliyanage/Smart-Campus-API.git
   ```
2. Navigate into the project folder:
   ```bash
   cd Smart-Campus-API
   ```
3. Run the clean package command to build the project:
   ```bash
   mvn clean package
   ```
4. Start the application using the Maven Exec plugin:
   ```bash
   mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
   ```
5. The server will start and listen at: **http://localhost:8080/api/v1**
6. Press **CTRL+C** in the terminal to stop the server.

## 4. API Endpoints Reference

| Method | Endpoint | Description | Success Status |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1` | System discovery and resource links | 200 OK |
| GET | `/api/v1/rooms` | Retrieve a list of all campus rooms | 200 OK |
| POST | `/api/v1/rooms` | Create a new room resource | 201 Created |
| GET | `/api/v1/rooms/{roomId}` | Fetch details for a specific room by ID | 200 OK |
| DELETE | `/api/v1/rooms/{roomId}` | Remove a room (requires no assigned sensors) | 204 No Content |
| GET | `/api/v1/sensors` | Retrieve a list of all registered sensors | 200 OK |
| GET | `/api/v1/sensors?type={type}` | Fetch sensors filtered by type (e.g., CO2, TEMP) | 200 OK |
| POST | `/api/v1/sensors` | Register a new sensor to a specific room | 201 Created |
| GET | `/api/v1/sensors/{sensorId}/readings` | Fetch historical readings for a specific sensor | 200 OK |
| POST | `/api/v1/sensors/{sensorId}/readings` | Post a new reading and update parent sensor value | 201 Created |

## 5. Sample curl Commands

# 1. Discovery endpoint
# Returns the API version and navigational links to rooms and sensors.
curl http://localhost:8080/api/v1

# 2. Get all rooms
# Returns a JSON array of all rooms currently stored in the in-memory database.
curl http://localhost:8080/api/v1/rooms

# 3. Create a new room (201 + Location header)
# Expect a 201 Created status and a Location header pointing to the new resource.
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Library Quiet Study","capacity":50}'

# 4. Get a specific room by ID
# Expect a 200 OK with the room's JSON data, or 404 if the ID doesn't exist.
curl http://localhost:8080/api/v1/rooms/room-101

# 5. Delete a room that has no sensors (204)
# Use a room ID that is empty to successfully remove the resource.
curl -X DELETE http://localhost:8080/api/v1/rooms/room-202

# 6. Try to delete a room that still has sensors (409 Conflict)
# Expect a 409 error with details explaining that the room must be cleared first.
curl -X DELETE http://localhost:8080/api/v1/rooms/room-101

# 7. Create a sensor with valid roomId (201)
# Linked to an existing room; returns the created sensor object.
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-005","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"room-101"}'

# 8. Try to create a sensor with invalid roomId (422)
# Expect a 422 Unprocessable Entity error when the roomId is not found.
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-ROOM"}'

# 9. Get all sensors
# Returns a list of all sensors across all rooms.
curl http://localhost:8080/api/v1/sensors

# 10. Filter sensors by type
# Case-insensitive filtering of sensors by their type attribute.
curl "http://localhost:8080/api/v1/sensors?type=CO2"

# 11. Post a new reading to a sensor (201)
# Updates the Parent Sensor's current value and records the reading in history.
curl -X POST http://localhost:8080/api/v1/sensors/sn-9001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":38.7}'

# 12. Try to post a reading to a MAINTENANCE sensor (403)
# Registering data to blocked sensors results in a 403 Forbidden response.
# (Note: Requires a sensor with status "MAINTENANCE")
curl -X POST http://localhost:8080/api/v1/sensors/sn-9002/readings \
  -H "Content-Type: application/json" \
  -d '{"value":21.0}'

# 13. Get reading history for a sensor
# Retrieves all chronological readings recorded for a specific sensor ID.
curl http://localhost:8080/api/v1/sensors/sn-9001/readings

## 6. Error Responses Reference

| HTTP Status | Scenario | Error Body Summary |
| :--- | :--- | :--- |
| 404 | Room not found | `{"error": "Room not found", "roomId": "{id}"}` |
| 404 | Sensor not found | `{"error": "Sensor not found", "sensorId": "{id}"}` |
| 409 | Delete room with sensors | `{"error": "Room not empty", "details": "..."}` |
| 422 | POST sensor with bad roomId | `{"error": "Linked resource not found", "details": "..."}` |
| 403 | POST reading to MAINTENANCE | `{"error": "Sensor unavailable", "details": "..."}` |
| 500 | Unexpected server error | `{"error": "Internal server error", "details": "..."}` |

---

### Section 7 — Project Structure

```
Smart-Campus-API/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── smartcampus/
                    ├── Main.java
                    ├── AppConfig.java
                    ├── model/
                    │   ├── Room.java
                    │   ├── Sensor.java
                    │   └── SensorReading.java
                    ├── store/
                    │   └── DataStore.java
                    ├── resource/
                    │   ├── DiscoveryResource.java
                    │   ├── RoomResource.java
                    │   ├── SensorResource.java
                    │   └── SensorReadingResource.java
                    ├── exception/
                    │   ├── RoomNotEmptyException.java
                    │   ├── LinkedResourceNotFoundException.java
                    │   └── SensorUnavailableException.java
                    └── mapper/
                        ├── RoomNotEmptyExceptionMapper.java
                        ├── LinkedResourceNotFoundExceptionMapper.java
                        ├── SensorUnavailableExceptionMapper.java
                        ├── GlobalExceptionMapper.java
                        └── LoggingFilter.java
```
