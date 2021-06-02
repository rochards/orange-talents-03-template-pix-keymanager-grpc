FROM openjdk:11
LABEL MAINTAINER="Rodrigo"
ARG JAR_FILE=build/libs/*all.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]