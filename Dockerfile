FROM clojure:openjdk-14-lein-slim-buster as builder

WORKDIR /app

# add deps, change sometimes
COPY project.clj /app/project.clj

RUN lein deps

# add sources files, change often
COPY src/ /app/src

# config and other static resources
COPY resources /app/resources

# build uberjar
RUN lein uberjar

##
## Clean base image for distribution
##

FROM openjdk:14-slim-buster

WORKDIR /app

# copy java artifact, changes every time
COPY --from=builder /app/target/uberjar/ov-movies.jar /app/app.jar

# set the command, with proper container support
CMD ["java","-XX:+UseContainerSupport","-XX:+UnlockExperimentalVMOptions","-jar","/app/app.jar"]