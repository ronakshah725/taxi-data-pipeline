#!/bin/bash

echo "Tearing down and starting containers..."
docker-compose down -v && docker-compose up -d

echo "Waiting for containers..."

wait_for_services() {
    PGPASSWORD=postgres docker-compose exec -T postgres pg_isready -h localhost -U postgres -d taxidb > /dev/null 2>&1 && \
    curl -s http://localhost:9200/_cluster/health | grep -q 'status.*\(green\|yellow\)' > /dev/null 2>&1 && \
    curl -s http://localhost:5601/api/status | grep -q '"overall":{"level":"available"' > /dev/null 2>&1
}

count=0
until wait_for_services || [ $count -eq 150 ]; do
    echo -n "."
    sleep 2
    ((count++))
done

if [ $count -eq 150 ]; then
    echo "containers failed to start within 5 minutes"
    exit 1
fi

echo -e "\nAll containers ready! Starting application..."