FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/ps_user-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
