### Description

This is a play framework demo project that contains:
 
 - interaction with underlying actor system
 - interaction with sql(+ testcontainers postgres tests)
 - interaction with rest-endpoints(+ server mocking using play)
 - kamon metrics(port 9095)
 - graylog logging

That project contains 2 endpoints:

- /counter/count - sql example
- /prices/btc - rest-client example

All service functionality was implemented in underlying actors.

### Starting

 - clone this project
 - configure 'graylogPort', 'graylogHost', 'facility' in logback.xml
 - rename 'name' and 'organization' in build.sbt
 - run 'sbt run'

### Starting postgres for local testing

```
docker-compose -f docker/postgres.yml up -d
docker-compose -f docker/postgres.yml down
```
