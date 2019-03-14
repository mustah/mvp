FROM openjdk:10-jre
RUN mkdir -p /app
RUN apt-get -y update && apt-get -y install curl
ARG MVP_APPVER
ARG APM_VERSION=1.4.0
ARG APM_SERVER=localhost
RUN groupadd -g 4000 mvp && \
	useradd -r -u 4000 -g mvp mvp
COPY ./backend/configuration/build/distributions/mvp-$MVP_APPVER.tar /app/mvp.tar
RUN cd /app/ && tar -m -xf mvp.tar --strip-components=1 && rm mvp.tar
ADD http://artifactory.elvaco.se/artifactory/Elvaco/elastic/apm/elastic-apm-agent-$APM_VERSION.jar /app/elastic-apm-agent.jar
RUN chown -R mvp:mvp /app
ENV JAVA_OPTS="$JAVA_OPTS -javaagent:/app/elastic-apm-agent.jar"
ENV JAVA_OPTS="$JAVA_OPTS -Delastic.apm.service_name=EvoMvp"
ENV JAVA_OPTS="$JAVA_OPTS -Delastic.apm.service_version=$MVP_APPVER"
ENV JAVA_OPTS="$JAVA_OPTS -Delastic.apm.application_packages=com.elvaco"
ENV JAVA_OPTS="$JAVA_OPTS -Delastic.apm.server_urls=http://$APM_SERVER"
WORKDIR /app
HEALTHCHECK CMD curl -v --fail http://localhost:8080/ || exit 1
CMD ["/app/bin/mvp"]
