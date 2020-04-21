# hello

This project is meant to illustrate some issues encountered while using Kamon instrumentation with a lagom application that has both GRPC and regular HTTP 1 endpoints.

It comes bundled up with a docker-compose setup which can be started by running the command `sbt clean 'dockerComposeUp -useStaticPorts'`. This should compile and start the service in a docker container together with a cassandra and jaeger. Executing the above command will print out the ports to be used to access each of the services.

## Current state

- GRPC endpoint are instrumented but only report traces when we make the call to `currentSpan.makeSamplingDecision()`
- HTTP endpoints seem to be missing instrumentation but do send out traces when we start a custom span. 

