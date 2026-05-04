FROM node:24-alpine AS build

ARG BUILD_CONFIGURATION=local
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration "${BUILD_CONFIGURATION}"

FROM nginx:1.27-alpine

COPY --from=build /app/dist/mnss-nova-alianca/browser /usr/share/nginx/html

EXPOSE 80
