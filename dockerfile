FROM node:20-bullseye

# Install dependencies
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk maven git && \
    apt-get clean

# Copy backend & frontend to image
WORKDIR /app
COPY zwinne-pilkarzyki-backend ./zwinne-pilkarzyki-backend
COPY zwinne-pilkarzyki-frontend ./zwinne-pilkarzyki-frontend

# Build backend
WORKDIR /app/zwinne-pilkarzyki-backend
RUN mvn clean package -DskipTests

# Build frontend
WORKDIR /app/zwinne-pilkarzyki-frontend
RUN npm install

# Starting script
WORKDIR /app
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080 5173

ENTRYPOINT ["/entrypoint.sh"]