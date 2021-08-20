### Starting

 - clone this project
 - configure 'graylogPort', 'graylogHost', 'facility' in logback.xml

### Starting postgres for local testing

```
docker-compose -f docker/postgres.yml up -d
docker-compose -f docker/postgres.yml down
```
