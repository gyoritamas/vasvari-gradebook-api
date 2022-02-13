# Gradebook API 

## Description

The *Gradebook API* application's main purpose is to keep gradebook entries in a database and to provide a way to access them through HTTP requests.


## Database schema

The database schema looks as follows:

<img src="https://github.com/gyoritamas/gradebook-api/blob/development/docs/images/db-schema.png" alt="schema"></a>

- The student table keeps the data of the students
- The class table keeps the name of the course
- The class_students table stores which student has enrolled to which class
- The assignment table keeps information about the assignments like their name, type, an optional description and when were they created
- The gradebook_entry table connects the student, class and assignment entities together with a grade

## How to run
#### Running the application locally

To build the app, use the command below in the project root folder.

```
mvnw package -DskipTests
```

After building the app, use the following command to run it.

```
java -jar target/gradebookapi-0.0.1-SNAPSHOT.jar
```

#### Running the application in Docker container

To build the application using docker-compose, execute the command below.

```
docker-compose -f docker-compose.dev.yml up --build
```
## API documentation
The API documentation created with Swagger and available at the following url:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Technology
- Spring Boot
- MariaDB
- Docker
- Swagger

## Known issues
- assignments 'createdAt' field doesn't use the correct time zone

