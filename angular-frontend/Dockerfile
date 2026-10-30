# ---------- Stage 1: build Angular ----------
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# ---------- Stage 2: Nginx serves the app and forwards /api to the gateway ----------
FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/angular-frontend/browser/ /usr/share/nginx/html/
EXPOSE 80
