FROM gradle:jdk17 AS builder
WORKDIR /app

COPY server ./server
COPY build.gradle .
COPY settings.gradle .
COPY .git ./.git

ENV VERSION $INPUT_PATCH
ENV IN_DOCKER true


RUN gradle build

FROM openjdk:17


WORKDIR /app
COPY --from=builder /app/server/build/libs/Harbinger-*.jar harbinger.jar

EXPOSE 7767/tcp
EXPOSE 7768/tcp
EXPOSE 7769/udp

VOLUME /data
ENV IN_DOCKER true

ENTRYPOINT ["java", "-jar", "harbinger.jar"]