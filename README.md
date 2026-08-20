# Resource Manager

Java assignment for shared resources (rooms, gym, desks) with capacity limits and hourly billing in INR.
# Tech stack
Language
Java (plain JDK)
HTTP server
com.sun.net.httpserver.HttpServer (built into the JDK)
API
Custom REST JSON endpoints
JSON
Hand-rolled parser/encoder (Json.java) 
Storage
In-memory (ConcurrentHashMap) 
Frontend
HTML + CSS + vanilla JavaScript (web/)
Build/run
javac / java only



## Data structures
Build using java

All data lives in memory in `InMemoryStore` . No database tables.

### Core storage: `ConcurrentHashMap`

Four maps hold everything, keyed by UUID string:

| Map | Key → Value | Purpose |
|-----|-------------|---------|
| `resources` | id → `Resource` | Meeting rooms, gym equipment, workstations |
| `services` | id → `Service` | Pricing plans linked to a resource |
| `sessions` | id → `UsageSession` | Active and completed usage sessions |
| `bills` | id → `Bill` | Bills created when a session stops |

`ConcurrentHashMap` is used so lookups by id are O(1) and the HTTP server can handle requests safely from multiple threads.

### Domain models (plain objects)

- **`Resource`** — name, type (`MEETING_ROOM` / `GYM_EQUIPMENT` / `WORKSTATION`), capacity
- **`Service`** — belongs to one resource; first-hour and extra-hour prices (INR)
- **`UsageSession`** — links a user to one resource + one service; tracks start/end time and status (`ACTIVE` / `COMPLETED`)
- **`Bill`** — snapshot of duration, billable hours, rates, and total amount

Relationships are by id (string references), not nested objects in the maps. Example: a `Service` stores `resourceId`; a session stores `resourceId` and `serviceId`.

### Supporting structures

- **`ArrayList` / `List`** — temporary lists when filtering (active sessions for a resource), sorting bills, or building API responses
- **`LinkedHashMap`** — ordered key/value maps for JSON request/response bodies (field order stays stable)
- **`enum`** — `ResourceType` and `SessionStatus` for fixed sets of values

### Capacity check

Availability is not stored separately. Active sessions for a resource are counted from the sessions map; if `activeCount < capacity`, a new session can start.

## Run

```
javac -d out $(find src -name "*.java")
java -cp out com.resourcehub.Main
```

Open http://localhost:8080/



## Pricing

Each resource has one or more services.
- first hour = one price
- extra hours = another price
- leftover minutes round up to next hour

Example: first hour Rs 30, extra Rs 10
- 20 min -> Rs 30
- 1 hr 5 min -> Rs 40

## API

- GET /api/resources
- POST /api/resources
- POST /api/usage/start
- POST /api/usage/stop
- GET /api/usage/active
- GET /api/bills
