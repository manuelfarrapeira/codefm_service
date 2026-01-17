#!/bin/bash
set -e

mkdir -p /app/logs
mkdir -p /app/data/student-photos
chmod 755 /app/logs
chmod 755 /app/data
chmod 755 /app/data/student-photos

echo "Directories initialized: /app/logs and /app/data/student-photos"

exec "$@"

