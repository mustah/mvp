FROM sgrio/java-oracle:server_jre_8
RUN mkdir -p /app
ARG MVP_APPVER
COPY ./backend/build/distributions/mvp-$MVP_APPVER.tar /app/mvp.tar
RUN cd /app/ && tar -xf mvp.tar --strip-components=1
ENTRYPOINT /app/bin/mvp
