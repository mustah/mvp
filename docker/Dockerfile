FROM openjdk:10-jre
RUN mkdir -p /app
RUN apt-get -y update && apt-get -y install curl
ARG MVP_APPVER
RUN groupadd -g 4000 mvp && \
	useradd -r -u 4000 -g mvp mvp
COPY ./backend/configuration/build/distributions/mvp-$MVP_APPVER.tar /app/mvp.tar
RUN cd /app/ && tar -m -xf mvp.tar --strip-components=1 && rm mvp.tar
RUN chown -R mvp:mvp /app
WORKDIR /app
HEALTHCHECK CMD curl -v --fail http://localhost:8080/ || exit 1
CMD ["/app/bin/mvp"]
