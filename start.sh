#!/bin/bash
# Railway start script for payment-service

# Find the JAR file (exclude plain JAR)
JAR_FILE=$(find build/libs -name 'payment-service-*.jar' ! -name '*-plain.jar' | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "Error: JAR file not found in build/libs/"
    echo "Available files:"
    ls -la build/libs/ 2>/dev/null || echo "build/libs/ directory not found"
    exit 1
fi

echo "Starting application with JAR: $JAR_FILE"
java -jar "$JAR_FILE"

