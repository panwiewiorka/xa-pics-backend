FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /app
RUN mkdir -p /volume-v1/db
COPY --from=build /home/gradle/src/build/libs/*.jar /app/ktor-xa-pics.jar
ENTRYPOINT ["java","-jar","/app/ktor-xa-pics.jar"]