FROM eclipse-temurin:17-alpine
RUN apk update &&\
    apk upgrade
WORKDIR /WORKDIR
COPY build/libs/SCMT.jar .
EXPOSE 9090
ENTRYPOINT java -jar SCMT.jar
