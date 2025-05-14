#!/bin/bash
set -e

# Run backend in background
cd /app/zwinne-pilkarzyki-backend/target
java -jar soccer-1.0.0.jar --server.port=8080 &

# Run frontend (Vite) on port 5173
cd /app/zwinne-pilkarzyki-frontend
npm run dev

# Keeping container alive (for example if npm run dev will stop)
tail -f /dev/null