# IDEA Setup

Most of us use the IDEA editor. These instructions are written to work for IDEA 2017.3.3.

## Annotation processing

We use Lombok, which requires annotation processing.

Enable: Settings > Build, Execution, Deployment > Compiler > Annotation Processors. "[x] Enable annotation processing"

Set "Production sources directory" to "../../generated".

## Javascript processing

We use modern Javascript, which needs a modern Javascript parser/runtime.

"Languages & Frameworks" > "JavaScript"

Set "JavaScript language version" to "ECMAScript 6"
