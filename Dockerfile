FROM gradle:jdk17 AS builder
WORKDIR /app

COPY server/build/libs/Harbinger-*.jar harbinger.jar

FROM openjdk:17


WORKDIR /app
COPY --from=builder /app/harbinger.jar harbinger.jar

EXPOSE 7768/tcp
EXPOSE 7769/udp

VOLUME /data
ENV IN_DOCKER=true

ENTRYPOINT ["java", "-jar", "harbinger.jar"]