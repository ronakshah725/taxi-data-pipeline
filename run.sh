#!/bin/bash

# Stop and remove existing containers
docker-compose down -v

# Start PostgreSQL
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until PGPASSWORD=postgres docker-compose exec -T postgres pg_isready -h localhost -U postgres -d taxidb; do
    echo -n "."
    sleep 1
done
echo "PostgreSQL is ready!"

# Build and run the application
./mvnw clean spring-boot:run 