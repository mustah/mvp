# MVP

### Execution Modes
There are a number of different ways to start the application - which one you
choose to use depends entirely on what changes you are expecting to make, and
subsequently test.

#### full-stack with PostgreSQL (docker-compose)
This is the recommended way to verify your changes using a local setup as
similar to production as possible (disregarding actual production data, of
course). This execution mode is supported by
[docker-compose](https://docs.docker.com/compose/), which is a tool for running
multi-container Docker applications.

The [docker-compose.yml] file controls the setup of the containers (one for
running our application, and one for running a PostgreSQL instance).

The application container is started with the "compose" Spring profile (see
[backend/src/main/resources/application-compose.properties]), which makes sure
that the database is connected to at the correct URL, with the correct
credentials.

This mode is started by running

```
./gradlew composeUp
```

and stopped by running

```
./gradlew composeDown
```

When launched in this mode, the address on which the web server is available is
printed when everything is up and running. This URL may vary between runs and
host systems.

> Note: Currently, all data volumes (including the PostgreSQL data volume) is destroyed when you run the `composeDown` task.

#### backend only
If the changes you're working on are isolated to the backend, and do not rely
on any database changes, this is probably the mode you're looking for. It is
most conveniently executed through your IDE of choice(MvpApplication is the
main class). This mode also lets you take advantage of
[spring-boot-devtools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html),
which will give you the advantage of rapid automatic restarts on classpatch
changes (i.e, when you rebuild your code), without having to do a full cold
restart. This should allow you to trim your build-deploy-test cycle
significantly.

#### frontend only
For frontend-heavy work, you can make use of the infrastructure documented in
[frontend/README] to achive a rapid build-deploy-test cycle.

#### backend + frontend
Of course, most user-facing features will require both backend and frontend
work. By virtue of isolating the frontend from the backend we are able to
combine them to run both simultaneously. This works by letting the frontend run
its own development web server for serving the frontend resources
(hot-reloading them as necessary) and letting the backend simply serve the API
endpoints (applying rapid restarts on rebuild in response to classpath
changes). This differs a bit from how things are set up in production, where
the backend bundles the frontend and serves it with its internal Tomcat web
server.

When launched in this mode, the GUI is available at `http://localhost:4444`, and
the backend/API is available at `http://localhost:8080`

#### full-stack with H2
If you are not directly interested in database modeling, schema design or
similar, running a full-blown PostgreSQL server is probably overkill. Running
in full-stack mode with H2 might be a convenient compromise. This mode differs
from the 'backend+frontend' mode in that it runs the final bundled jar
directly, which means that it makes use of the internal Tomcat server to serve
the frontend. As a consequence, hot reloading is not possible in this mode for
either backend or frontend, meaning that to view your changes, you will have to
build the entire project and create a jar "from scratch" (ignoring whatever
incremental build functionality Gradle provides us with).

This mode is started by running:
```
./gradlew runFinalJar
```

> Note: This mode is probably not suitable for "regular" development work, but
> should rather be used either as a final check-off, or when changes has been
> made to the structure and/or infrastructure of the software.

> Note: As an alternative to running the application natively, one could also
> use the [Dockerfile] here, to build and run the application in a Docker
> container. This could be convenient if one needs several instances (of
> different versions perhaps) running simultaneously without having to worry
> about port conflicts etc,.

## Using H2’s web console

We are using Spring Boot’s developer tools, and by adding the configuration
`spring.h2.console.enabled=true` to `application.properties` file, we enable
the web console for the in memory database of the application (h2).

The H2 console is only intended for use during development so make sure that
`spring.h2.console.enabled=false` is set in production environment(s).

**View in browser** 

Navigate to `http://localhost:8080/h2-console` and use `Generic H2 (Server)`
settings and the JDBC URL should be `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`.
