# postgres-testcontainers

## How to setup local database using docker
```
docker run -d \
  --name timescaledb \
  -e POSTGRES_PASSWORD=test-user \
  -e POSTGRES_USER=test-user \
  -e POSTGRES_DB=test-database \
  -p 5432:5432 \
  timescale/timescaledb:2.15.2-pg14
```