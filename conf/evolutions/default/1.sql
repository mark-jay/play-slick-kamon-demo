-- Users schema

-- !Ups

CREATE TABLE COUNTER (
                      id serial NOT NULL PRIMARY KEY,
                      count bigint NOT NULL
);

-- !Downs

-- DROP TABLE COUNTER;
