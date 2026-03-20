# =========================================
# Node.js gRPC Notification Microservice
# =========================================
FROM node:24-alpine

WORKDIR /app

# Copy package files first for dependency caching
COPY package.json package-lock.json* ./

# Install production dependencies only
RUN npm ci --omit=dev

# Copy application source
COPY server.js notification.proto ./

# Create non-root user for security
RUN addgroup -S notifgroup && adduser -S notifuser -G notifgroup
USER notifuser

# gRPC standard port
EXPOSE 50051

# Start the gRPC server
CMD ["node", "server.js"]
