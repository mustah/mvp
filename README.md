# MVP

## Building & running

Acquire gradle:

	sudo apt install gradle

Run project:

	gradle bootRun

Visit GUI in browser:

	http://localhost:8080

## Using H2’s web console

We are using Spring Boot’s developer tools, and by adding the configuration `spring.h2.console.enabled=true` to 
`application.properties` file, we enable the web console for the in memory database of the application (h2).

The H2 console is only intended for use during development so make sure that `spring.h2.console.enabled=false` is set 
in production environment(s).

**View in browser** 

Navigate to `http://localhost:8080/h2-console` and use `Generic H2 (Server)` settings and the JDBC URL should be 
`jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`.
