# Resource Manager

Java assignment for shared resources (rooms, gym, desks) with capacity limits and hourly billing in INR.



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
