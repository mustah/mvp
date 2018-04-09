FROM sgrio/java-oracle:server_jre_8
RUN mkdir -p /app
ARG MVP_APPVER
COPY ./backend/configuration/build/distributions/mvp-$MVP_APPVER.tar /app/mvp.tar
RUN cd /app/ && tar -xf mvp.tar --strip-components=1
RUN apt-get -y update && apt-get -y install curl
HEALTHCHECK CMD curl -v --fail http://localhost:80/ || exit 1
CMD ["/app/bin/mvp"]
