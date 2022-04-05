FROM gradle:jdk16 as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist

FROM openjdk:16-slim
COPY --from=builder /home/gradle/src/build/install/ketabs/ /app/
EXPOSE 8046
CMD ["/app/bin/ketabs"]
