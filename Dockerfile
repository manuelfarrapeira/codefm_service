FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-slim

#esto instala el cliente de infisical para poder acceder al server
RUN apt-get update && apt-get install -y bash curl && curl -1sLf \
'https://dl.cloudsmith.io/public/infisical/infisical-cli/setup.deb.sh' | bash \
&& apt-get update && apt-get install -y infisical


WORKDIR /app
RUN mkdir -p /app/logs
COPY --from=build /app/codefm-boot/target/codefm-boot-*.jar app.jar
EXPOSE 8080

ENV INFISICAL_TOKEN=st.378ff8e3-fc74-44a2-9867-3c27af45736c.d276c6a534321848e88fff5743c4036e.dafd369933b4f999dc6cd48105e3c87e
ENV INFISICAL_API_URL=http://infisical-server:8080

ENTRYPOINT ["infisical", "run", "--projectId", "491fe430-7bf6-40b0-aa05-58d7f5268b28", "--env", "prod", "--"]
CMD ["java", "-jar", "app.jar"]

#ENTRYPOINT ["java", "-jar", "app.jar"]