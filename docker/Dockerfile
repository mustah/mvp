FROM sgrio/java-oracle:server_jre_8
RUN mkdir -p /app
RUN apt-get -y update && apt-get -y install curl
ARG MVP_APPVER
COPY ./backend/configuration/build/distributions/mvp-$MVP_APPVER.tar /app/mvp.tar
RUN cd /app/ && tar -m -xf mvp.tar --strip-components=1
HEALTHCHECK CMD curl -v --fail http://localhost:80/ || exit 1
CMD ["/app/bin/mvp"]
