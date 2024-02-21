FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/ktor-xa-pics.jar
ENV DOMAIN=http://192.168.0.87:8080
ENTRYPOINT ["java","-jar","/app/ktor-xa-pics.jar"]