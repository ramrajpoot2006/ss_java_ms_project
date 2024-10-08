FROM registry.tools.3stripes.net/base-images/alpine_java-17:week_2023-52 as dev_dependencies
USER root
RUN --mount=type=cache,target=/var/cache/apk apk add maven
USER domain_name
WORKDIR /var/app
COPY --chown=domain_name:domain_name . ./
ADD --chown=domain_name:domain_name https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.22.1/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
RUN --mount=type=cache,target=/home/domain_name/.m2,uid=1000 mvn dependency:resolve

FROM dev_dependencies as test
RUN --mount=type=cache,target=/home/domain_name/.m2,uid=1000 mvn test && mvn jacoco:report

FROM scratch as test-results
COPY --from=test /var/app/target/site/jacoco ./

FROM dev_dependencies as builder
COPY --chown=domain_name:domain_name --from=test /var/app/target/site/jacoco /var/app/target/site/jacoco
COPY --chown=domain_name:domain_name --from=test /var/app/target/jacoco.exec /var/app/target/jacoco.exec
RUN --mount=type=cache,target=/home/domain_name/.m2,uid=1000 mvn package -DskipTests

FROM scratch as java-build
COPY --from=builder /var/app/target ./

FROM builder as java-libraries-copier
RUN --mount=type=cache,target=/home/domain_name/.m2,uid=1000 cp -R /home/domain_name/.m2/* .m2/

FROM scratch as java-libraries
COPY --from=java-libraries-copier /var/app/.m2 ./

FROM registry.tools.3stripes.net/base-images/alpine_java-17:week_2023-52 as app
WORKDIR /var/app
COPY --chown=domain_name:domain_name --from=builder /var/app/target/*.jar app.jar
COPY --chown=domain_name:domain_name --from=builder /var/app/opentelemetry-javaagent.jar ./
EXPOSE 8080
ENTRYPOINT [ \
  "sh", \
  "-c", \
  "java \
  -Dsun.net.inetaddr.ttl=60 \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=${ENVIRONMENT} \
  ${JVM_DEBUG_PORT:+-agentlib:jdwp=transport=dt_socket,address=$JVM_DEBUG_PORT,server=y,suspend=n} \
  -javaagent:opentelemetry-javaagent.jar \
  -jar /var/app/app.jar" \
  ]
