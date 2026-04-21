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
curl -X POST http://localhost:8080/api/v1/sensors/sn-9003/readings \
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

---

### Section 8 — Report: Answers to Coursework Questions

## 8. Report — Answers to Coursework Questions

#### Question 1.1 — JAX-RS Resource Lifecycle
The default lifecycle of a JAX-RS resource class is request-scoped, although this can be customized using specific annotations. In a standard Jersey implementation, a new instance of the resource class is instantiated for every incoming HTTP request. This architectural choice ensures that each request is isolated from others, promoting a stateless design that is fundamental to the REST architectural style. However, because a new instance is created for every request, any instance variables defined within the resource class are not shared across different requests. Consequently, these variables cannot be used to maintain state that needs to persist across multiple client interactions. To manage shared in-memory data structures effectively, developers must move health state or volatile data into a separate component, such as a static singleton class or a managed bean with a broader scope. In this project, a static DataStore class was implemented to serve as the central repository for rooms and sensors. Because multiple threads (one per request) may attempt to access and modify this shared state simultaneously, using a standard HashMap would be highly unsafe, potentially leading to data corruption or race conditions. To mitigate these risks, ConcurrentHashMap was utilized throughout the data store. This ensures that the system remains thread-safe during high-concurrency scenarios, allowing for safe reads and writes without the overhead of manual synchronization or a full-scale database management system.

#### Question 1.2 — HATEOAS
Hypermedia as the Engine of Application State, commonly referred to as HATEOAS, is widely considered the hallmark of an advanced and truly mature RESTful API design. It represents the final level of the Richardson Maturity Model, distinguishing between a collection of endpoints and a truly connected hypermedia system. By including navigational links directly within API responses, HATEOAS allows client applications to discover available actions and transitions dynamically at runtime. This approach offers significant benefits to client developers, as it moves the discovery logic from static, often outdated documentation into the interactive responses of the API itself. When a client receives a resource representation that includes links to related resources or permitted actions, it can navigate the application state without having to hardcode URL structures or URI templates. This dynamic discovery mechanism promotes loose coupling between the client and the server, as the server can evolve its URI structure without breaking compliant clients that follow hypermedia links. In the context of this Smart Campus API provided in the coursework, the discovery endpoint at /api/v1 serves as a prime example of this principle. By returning structured links to the rooms and sensors collections, the API provides a entry point that guides the client through the available resource hierarchy, improving maintainability and developer experience.

#### Question 2.1 — IDs vs Full Objects in List Responses
When designing collection resources that return a list of items, such as the rooms in this campus management system, developers must choose between returning only a list of unique identifiers or returning the full state of each resource. Each approach carries distinct implications for network performance and client-side processing complexity. Returning only IDs significantly reduces the initial payload size, which can be advantageous in bandwidth-constrained environments or when dealing with extremely large datasets containing thousands of entries. However, this strategy forces the client to make a subsequent GET request for every individual ID to retrieve the detailed data it likely needs to display to the user. This creates the well-known N+1 request problem, where fetching a list of N items results in N+1 total network round trips, leading to increased latency and server load. Conversely, returning full room objects in the initial list response increases the payload size but allows the client to obtain all necessary information in a single round trip. This is generally more efficient for dashboards, mobile applications, and search result pages where the user expects to see details immediately. For very large datasets, a balanced approach using pagination or partial projections might be preferred, but for the scope of this project, returning full objects in the GET /rooms endpoint was chosen to minimize client-side complexity and optimize for the most common use cases.

#### Question 2.2 — DELETE Idempotency
The concept of idempotency is a core requirement for several HTTP methods, including the DELETE operation. An operation is considered idempotent if making the same request multiple times leaves the server in the same state as if the request had been made only once. In the implementation of this Smart Campus API, the DELETE operation on a specific room resource is strictly idempotent. When a client sends a DELETE request to a valid and existing room, the server removes the resource from the in-memory data store and returns a 204 No Content status code, indicating a successful deletion. If the client subsequently sends the exact same DELETE request for that same room ID, the server will search for the resource, find that it no longer exists, and return a 404 Not Found error. Despite the difference in the returned HTTP status codes—204 for the first call and 404 for all subsequent calls—the ultimate state of the server remains identical: the room resource is absent from the system. According to the HTTP/1.1 specification in RFC 7231, the idempotency of a method is defined by the intended effect on the server state, not by the response code returned to the client. Therefore, because the end state of the resource is the same regardless of how many times the request is repeatedly made, the implementation effectively adheres to the RESTful principle of idempotency.

#### Question 3.1 — @Consumes and Media Type Mismatch
The use of the @Consumes annotation in JAX-RS is critical for defining the specific media types that a resource method is capable of processing. In this project, the POST method for creating rooms is explicitly annotated with @Consumes(MediaType.APPLICATION_JSON), indicating that it expects the request body to be formatted as JSON. If a client attempts to send data using a different format, such as text/plain or application/xml, the JAX-RS framework performs an automatic inspection of the Content-Type header before any business logic is executed. If the header does not match the allowed media type, the framework immediately rejects the request with a 415 Unsupported Media Type response. This automated validation layer is a significant advantage of the JAX-RS architecture, as it prevents the application's deserialization logic from attempting to parse incompatible data formats, which would otherwise result in runtime errors or unpredictable behavior. By enforcing this strict API contract at the framework level, the server ensures that all incoming payloads conform to the expected JSON structure. This not only protects the integrity of the data ingestion process but also provides clear, standardized feedback to client developers about the requirements of the API. Test 19 in the comprehensive test suite verified this behavior, confirming that the system correctly identifies and rejects mismatched content types with the appropriate 415 status code.

#### Question 3.2 — QueryParam vs PathParam for Filtering
When designing RESTful URIs, the choice between using path parameters and query parameters depends on the semantic intent of the request. Path parameters are fundamentally designed to identify a specific, unique resource within a hierarchy, such as /sensors/sn-9001, where the ID points to a single instance. In contrast, query parameters are designed to modify or filter a collection request and are inherently optional. In the Smart Campus API, filtering sensors by their type is implemented using a query parameter, such as /sensors?type=CO2. This approach is generally considered superior to using a path segment like /sensors/type/CO2 because it correctly represents that the 'type' is a filter applied to the collection rather than a unique resource identifier. Furthermore, query parameters offer greater flexibility because they can be easily omitted, in which case the API naturally returns the entire unfiltered collection. They also allow for the combination of multiple filters, such as ?type=CO2&status=ACTIVE, without requiring complex and brittle URI patterns. From an architectural perspective, query parameter-based URLs are more consistent with standard REST conventions and are often better handled by caching mechanisms and search engine optimizations. By utilizing @QueryParam for filtering, the API remains extensible and follows established best practices for resource discovery and data retrieval.

#### Question 4.1 — Sub-Resource Locator Pattern
The Sub-Resource Locator pattern is a powerful JAX-RS feature that allows a resource method to delegate the handling of nested URI segments to another class. Unlike standard resource methods, a sub-resource locator is annotated with @Path but does not have an HTTP method annotation like @GET or @POST. Instead, it returns an instance of a sub-resource class that contains the logic for the nested path. This pattern offers significant architectural benefits, primarily by enforcing the Single Responsibility Principle within the API's codebase. By delegating nested logic to separate classes, the API can manage complexity more effectively as it grows. For instance, in this project, SensorResource is responsible for top-level sensor management, while the SensorReadingResource class focuses exclusively on managing the audit trail and history of readings for a specific sensor. If all nested paths were implemented within a single class, the file would quickly become bloated, making it difficult to read, maintain, and test. Using separate classes for different resource domains allows for cleaner code organization, improved reusability, and higher modularity. Each sub-resource can have its own mappers, filters, and business logic, which leads to a more robust and scalable API architecture that can easily accommodate future extensions.

#### Question 5.2 — Stack Trace Security Risk
From a cybersecurity perspective, the exposure of internal Java stack traces to external API consumers represents a significant information disclosure risk. A stack trace is essentially a blueprint of the application's internal structure, revealing the exact package names, class hierarchies, and method call sequences used in the codebase. By analyzing these traces, an attacker can gain deep insights into the server's architectural design and identify the specific third-party libraries and framework versions being utilized. This information is invaluable for planning targeted exploits, as the attacker can cross-reference the discovered library versions with known Common Vulnerabilities and Exposures (CVEs) on public databases. This type of vulnerability is categorized under the OWASP Top 10 as A05:2021 – Security Misconfiguration. Furthermore, raw exception messages embedded in stack traces might inadvertently leak sensitive environment details, such as database connection strings, internal file paths, or private hostnames. To mitigate these risks, this project implements a GlobalExceptionMapper that intercept all unexpected Throwable instances. Instead of propagating the raw exception to the client, the mapper logs the full error details on the server using java.util.logging.Logger for internal debugging while returning only a generic, non-descriptive JSON error message to the user. This ensures that the application remains secure against reconnaissance attempts without sacrificing the developer's ability to troubleshoot production issues.
