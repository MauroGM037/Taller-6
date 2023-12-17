FROM adoptopenjdk:17-jdk-hotspot AS build

WORKDIR /app

COPY build.gradle settings.gradle ./

COPY src/ src/

RUN ./gradlew shadowJar

FROM adoptopenjdk:17-jre-hotspot

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/conserjeria.jar

EXPOSE 7070
EXPOSE 50123

CMD ["java", "-jar", "conserjeria.jar"]
