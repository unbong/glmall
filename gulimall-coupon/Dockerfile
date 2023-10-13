FROM java:8u111-alpine
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "-Xms228m" , "-Xmx300m",  "/app.jar", "--spring.profiles.active=prod"]
