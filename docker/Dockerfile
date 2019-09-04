FROM adoptopenjdk/openjdk11:alpine
RUN mkdir -p /app
RUN apk add --update \
	curl \
	&& rm -rf /var/cache/apk/*

ARG MVP_APPVER
ARG APM_VERSION=1.9.0
ARG APM_SERVER=localhost
RUN addgroup -g 4000 -S  mvp && \
	adduser -S -u 4000 -g mvp mvp
COPY ./backend/configuration/build/distributions/mvp-$MVP_APPVER.tar /app/mvp.tar
ADD http://artifactory.elvaco.se/artifactory/Elvaco/elastic/apm/elastic-apm-agent-$APM_VERSION.jar /app/elastic-apm-agent.jar

RUN cd /app/ \
	&& tar -m -xf mvp.tar --strip-components=1 \
	&& rm mvp.tar \
	&& chown -R mvp:mvp /app

ENV JAVA_OPTS="$JAVA_OPTS -javaagent:/app/elastic-apm-agent.jar"
ENV JAVA_OPTS="$JAVA_OPTS -Delastic.apm.service_name=EvoMvp"
ENV JAVA_OPTS="$JAVA_OPTS -Delastic.apm.service_version=$MVP_APPVER"
ENV JAVA_OPTS="$JAVA_OPTS -Delastic.apm.application_packages=com.elvaco"
ENV JAVA_OPTS="$JAVA_OPTS -Delastic.apm.server_urls=http://$APM_SERVER"
USER mvp:mvp
WORKDIR /app
HEALTHCHECK CMD curl -v --fail http://localhost:8080/ || exit 1
CMD ["/app/bin/mvp"]
