FROM node:20-alpine AS build
WORKDIR /app
COPY web/package*.json ./
RUN npm install
# Install Java (required by openapi-generator-cli)
RUN apk add --no-cache openjdk21-jre-headless
# Copy OpenAPI contract to parent dir to match the relative path ../openapi.yaml in the script
COPY openapi.yaml ../openapi.yaml
COPY web/ .
RUN npm run openapi:generate
RUN npm run build

FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY web/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
