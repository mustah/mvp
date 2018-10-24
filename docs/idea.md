# IDEA Setup

Most of us use the IDEA editor. These instructions are written to work for IDEA 2017.3.3.

## Annotation processing

We use Lombok, which requires annotation processing.

- Enable: Settings > Build, Execution, Deployment > Compiler > Annotation Processors. "[x] Enable annotation processing"
- Set "Store generated sources relative to:" to "Module content root"
- Set "Production sources directory" to "../generated".
- Set "Test sources directory" to "../generated_tests".

## Javascript processing

We use modern Javascript, which needs a modern Javascript parser/runtime.

- "Languages & Frameworks" > "JavaScript"
- Set "JavaScript language version" to "ECMAScript 6"

## Checkstyle configuration

We have a custom checkstyle configuration file that needs to be configured in order to run it from within IDEA.

Installation:

- Install the Checkstyle plugin
- Configure Checkstyle (Other Settings -> Checkstyle)
- "Scan Scope" should be "Only Java sources (including tests)"
- Under "Configuration File", press the "+" and import MVP's own configuration file at ./backend/config/checkstyle/checkstyle.xml
- Make sure its checkbox representing "Active" is checked

Usage:

- I use it by searching for checkstyle in the "action/option menu" (ctrl+shift+a by default, I think)
