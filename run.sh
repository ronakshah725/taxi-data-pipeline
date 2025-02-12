#!/bin/bash

# Stop and remove existing containers
docker-compose down -v

# Start PostgreSQL
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to start..."
sleep 5

# Build and run the application
./mvnw clean spring-boot:run 