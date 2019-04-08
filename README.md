# MVP

## Execution Modes
There are a number of different ways to start the application - which one you
choose to use depends entirely on what changes you are expecting to make, and
subsequently test.

Some of them requires Docker, see the bottom of this file for instructions on
that.

### Build and generate jooq java classes
To create the generated classes needed for jooq, just run the gradle task:

`gradle generateProdJooqSchemaSource`

This will create all the classes needed to build the sql queries with jooq in java.

### Only PostgreSQL in docker with docker-compose on localhost
This execution mode is supported by
[docker-compose](https://docs.docker.com/compose/), which is a tool for running
multi-container Docker applications.

The [docker-compose.yml] file controls the setup of the containers (one for
running our application, and one for running a PostgreSQL instance).

It's very easy to just start postgresql docker container and pull it down when finished, with the 
command:

`docker-compose up postgresql && docker-compose down -v`

And just start the MvpApplication.java with postgresql profile in your idea. Here are the program
arguments for the configuration in your idea:

`--server.port=8080 --spring.datasource.url=jdbc:postgresql://localhost:5432/mvp`

and active profile, set to `postgresql`.

#### Run system tests with postgresql
All the classes that are run as integration tests can also be run as system tests with postgresql
database. Just run the `systemTest` task and a postgresql instance will be started and used as
the database for the session.

`./gradlew clean systemTest`

> NOTE! If you already have a postgresql instance running, that instance will be used. Also beware
> that if that instance was launched using docker-compose, it will be destroyed along with all its
> data at the end of the system tests.

To debug a failing test case, it is sometimes useful to keep the postgresql instance running after its conclusion. This is currently supported by setting the environment variable 'MVP_SYSTEM_TEST_DO_CLEANUP' to false;

`MVP_SYSTEM_TEST_DO_CLEANUP=false ./gradlew systemTest`

Keep in mind that this requires you to clean up the remaining containers yourself when you're done with them.

A handy tip: If you're only troubleshooting one, or a collection of, failing test cases you can specify to run only those tests by conveniently specifying which tests to run using the `--tests` option, like so:

`./gradlew systemTest --tests com.elvaco.mvp.repository.LogicalMeterJpaRepositoryTest`

or

`./gradlew systemTest --tests com.elvaco.mvp.repository.LogicalMeterJpaRepositoryTest.containsInPropertyCollection`

Look [here](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_test) for more examples, and a more thorough explanation of test filters.

#### Run single system test against postgresql from within IDEA

> Caveat regarding losing all your data from previous section applies here too

Run your test with the following VM arguments:

    -ea -Dspring.profiles.active=postgresql -Dserver.port=8080 -Dspring.datasource.url="jdbc:postgresql://localhost:5432/mvp"

### Backend only
If the changes you're working on are isolated to the backend, and do not rely
on any database changes, this is probably the mode you're looking for. It is
most conveniently executed through your IDE of choice(MvpApplication is the
main class). This mode also lets you take advantage of
[spring-boot-devtools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html),
which will give you the advantage of rapid automatic restarts on classpatch
changes (i.e, when you rebuild your code), without having to do a full cold
restart. This should allow you to trim your build-deploy-test cycle
significantly.

### Frontend only
For frontend-heavy work, you can make use of the infrastructure documented in
[frontend/README] to achive a rapid build-deploy-test cycle.

### Backend + frontend
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

## Using H2’s web console

We are using Spring Boot’s developer tools, and by adding the configuration
`spring.h2.console.enabled=true` to `application.properties` file, we enable
the web console for the in memory database of the application (h2).

The H2 console is only intended for use during development so make sure that
`spring.h2.console.enabled=false` is set in production environment(s).

**View in browser** 

Navigate to <http://localhost:8080/h2-console> and use `Generic H2 (Server)`
settings and the JDBC URL should be `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`.

## Addendum

### Check for outdated gradle versions

Run the gradle command `gradle dependencyUpdates` and the outdated plugins and the available 
versions will be displayed in your terminal. 

For further reading: https://github.com/ben-manes/gradle-versions-plugin

### Installing Docker

You want "Docker Community Edition". See
https://store.docker.com/search?type=edition&offering=community for links for
your desktop OS.

This is how you do it on Ubuntu:
https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/#install-using-the-repository

Keeping it updated should be as simple as (assuming Ubuntu):

    apt update
    apt upgrade

Don't forget to add yourself to the docker group, this would do for Ubuntu:

    sudo usermod -aG docker your_username_here

To let this take effect, logout of your session/reboot your computer.
Afterwards, confirm that you are a member of the docker group:

    groups
    your_username_here some_other_groups docker

### Installing Docker compose

Docker compose is distributed through pip, which is the package manager for
Python programs.

To install compose:

    sudo pip install docker-compose

Keeping compose up to date by executing the previous command again.

### Profiling and debugging PostgreSQL in Docker compose

If you're interested in seeing the actual statements run against the database
as you're running you can set the `command` parameter for the database container, like so:

    command: -c log_statement=all

See https://www.postgresql.org/docs/10/static/runtime-config-logging.html#GUC-LOG-STATEMENT
for more information on the `log_statement` parameter.

Another neat set of configuration options to set is:

    command: -c shared_preload_libraries='pg_stat_statements' -c pg_stat_statements.max=1000 -c pg_stat_statements.track=all

This causes PostgreSQL to load the pg_stat_statements libraraies and enable tracking of statements. This is great for
troubleshooting general application performance issues. Note that you need to also create the pg_stat_statements
extension in order for things to actually be collected properly.

    create extension pg_stat_statements;

Once that's done and you've exercised the application a bit, you'll be able to run various queries against the created
tables. One that's served me well is the following:

    SELECT query, calls, total_time, rows,
    100.0 * shared_blks_hit / nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent
    FROM pg_stat_statements ORDER BY total_time DESC LIMIT 5;


See https://www.postgresql.org/docs/10/static/pgstatstatements.html for more information on the `pg_stat_statements`
module.


### Running with APM
First, start the APM stack (run `docker-compose up` in `./docker/apm`)

Second, get a copy of the elastic-agent jar. Unless you're doing something Extra Cool, like testing a new version, get the latest version from here: http://artifactory.elvaco.se/artifactory/webapp/#/artifacts/browse/tree/General/Elvaco/elastic/apm

Third, start the application with the following VM options:

`-javaagent:${path-to-elastic-agent.jar} -Delastic.apm.service_name=EvoMvp -Delastic.apm.service_version=unreleased -Delastic.apm.application_packages=com.elvaco -Delastic.apm.server_urls=http://{your-ip-or-hostname}:8201`

Kibana should now be running at http://localhost:5601/app/kibana. Select APM in the left-hand menu. Push the "Setup instructions" button - at the bottom of the page, first press "Load kibana objects", then press "Launch APM" (you could also check agent & server status, to make sure you've set everything up properly).

Make a few requests to the application and you should be able to eventually see some transactions show up.
