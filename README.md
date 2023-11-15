# TrackDev-spring
The REST API for the TrackDev project built with Spring Boot.

The TrackDev project is an application built for educational purposes with the goal to teach and help students to learn to work in teams using an agile metodology while working together to build an application. It works with the [TrackDev-react](https://github.com/trackdevel/trackdev-react) as a web client.

This is an ongoing project.

Current features:
* Courses, students and projects management
* Backlogs, tasks and sprints management

## About the API

This Spring Boot application is a multi-tier application. The main three tiers used are:

* REST tier (```@Controller```)
* Business tier (```@Service```)
* Persistence tier (JPA)

Other Java APIs used:

* Bean validation
* Exception Mappers
* Dependency injection

It uses [Spring Boot 2](https://spring.io/projects/spring-boot) to produce an jar file than can be executed standalone without an application server:

```
gradle bootRun
```

## Image uploading

In order to store files, the app uses a private object storage server: [minio](https://www.minio.io/). The minio configuration has to be passed to the application as properties via command line:

```
gradlew bootJar
java -Dswarm.project.minio.ulr=http://your-minio-host.com -Dswarm.project.minio.access-key=your-access-key -Dswarm.project.minio.secret-key=your-secret-key -Dswarm.project.minio.bucket=your-bucket -jar ./build/libs/todo-spring-0.1.0.jar
```



## Heroku

The app is ready to deploy into [Heroku](http://heroku.com) with the ```web``` profile. There is a file ```Procfile``` with the command line arguments to start the jar.
#### Collaborations are welcome!