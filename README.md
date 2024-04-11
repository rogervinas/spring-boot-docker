[![CI](https://github.com/rogervinas/spring-boot-docker/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/rogervinas/spring-boot-docker/actions/workflows/gradle.yml)
![Java](https://img.shields.io/badge/Java-21-blue?labelColor=black)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-blue?labelColor=black)
![SpringBoot](https://img.shields.io/badge/SpringBoot-3.2.4-blue?labelColor=black)

# Spring Boot with Docker

In this sample we will create a **🍀 Spring Boot Application** with a simple /hello endpoint and then distribute it as a **🐳 Docker image**

And of course we want to ensure the Docker image works, so we will test it using [Testcontainers](https://www.testcontainers.org/) 🤩

![Diagram](doc/diagram.png)

Ready? Let's go!

- [Develop](#develop)
- [Test this demo](#test-this-demo)
- [Run this demo](#run-this-demo)

You can browse older versions:
- [Spring Boot 2.x](https://github.com/rogervinas/spring-boot-docker/tree/spring-boot-2.x)

## Develop

### 1. We start at [Spring Initialzr](https://start.spring.io/#!type=gradle-project-kotlin&language=kotlin&packaging=jar&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&dependencies=webflux) and create an empty **Spring Boot** project with **Webflux** and **Kotlin**

### 2. Then we add this simple test

```kotlin
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApplicationTests {

  @LocalServerPort
  private var port: Int = 0

  @Test
  fun `should say hello`() {
    val responseBody = WebClient.builder()
      .baseUrl("http://localhost:$port").build()
      .get().uri("/hello")
      .exchangeToMono { response ->
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        response.bodyToMono(String::class.java)
      }.block()
    assertThat(responseBody).isEqualTo("hello!")
  }
}
```

### 3. And we add this simple implementation ...

```kotlin
@RestController
class HelloController {
  @GetMapping("/hello")
  fun hello() = "hello!"
}
```

... and now our test is 🟩👏

### 4. Next we need to generate a docker image ... 🤔

Some alternatives are documented in [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker) and [Topical Guide on Docker](https://spring.io/guides/topicals/spring-boot-docker)

And luckily for us, it is as easy as use the task `bootBuildImage` of the [Spring Boot's Gradle plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image):

```bash
./gradlew bootBuildImage
```

So far so good! 😁

Now we have to test the generated docker image ...

### 5. First we use [org.unbroken-dome.test-sets](https://plugins.gradle.org/plugin/org.unbroken-dome.test-sets) to create a new test source root named `container-test`:

```kotlin
plugins {
  id("org.unbroken-dome.test-sets") version "x.x.x"
}

testSets {
  "container-test"()
}

tasks["container-test"].dependsOn("bootBuildImage")

```

Note that `bootBuildImage` task is executed before `container-test` task, so we ensure we are always testing the docker image we've just built from the current source code

### 6. Then we create test using [Testcontainers](https://www.testcontainers.org/features/creating_container/#creating-a-generic-container-based-on-an-image) and [JUnit5](https://www.testcontainers.org/test_framework_integration/junit_5)

```kotlin
@Testcontainers
class ApplicationContainerTests {

  companion object {

    private const val APP_PORT = 8080

    @Container
    private val app = GenericContainer(System.getProperty("docker.image"))
      .withExposedPorts(APP_PORT)
  }

  @Test
  fun `should say hello`() {
    // ...
  }
}
```

### 7. Last thing we need to do is set the value of the system property `docker.image` before running any test:

```kotlin
tasks.withType<Test> {
  useJUnitPlatform()
  systemProperty("docker.image", "${project.name}:${project.version}")
}
```

As `${project.name}:${project.version}` is the default value used by `bootBuildImage` task

And that is all! Happy coding! 💙

## Test this demo

Test the application without assembling it:
```
./gradlew test
```

Build the application as a container and test it:
```
./gradlew container-test
```

## Run this demo

Run the application without assembling it:
```shell
./gradlew bootRun
```

Build the application as a fatjar and run it with java:
```shell
./gradlew bootJar
java -jar ./build/libs/spring-boot-docker-0.0.1-SNAPSHOT.jar
```

Build the application as a container and run it with docker:
```shell
./gradlew bootBuildImage
docker run -p 8080:8080 --rm spring-boot-docker:0.0.1-SNAPSHOT
```

In either case, call the /hello endpoint:
```shell
curl -i http://localhost:8080/hello
```
