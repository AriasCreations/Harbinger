FROM gradle:jdk17 AS builder
WORKDIR /app

COPY server/build.gradle .
COPY server/gradle.properties .
COPY server/src ./src
COPY .git ./.git


RUN gradle build

FROM openjdk:17


WORKDIR /app
COPY --from=builder /app/build/libs/Harbinger-*.jar harbinger.jar

EXPOSE 7768/tcp
EXPOSE 7769/udp

VOLUME /data
ENV IN_DOCKER=true

ENTRYPOINT ["java", "-jar", "harbinger.jar"]