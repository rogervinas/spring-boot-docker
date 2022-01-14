[![CI](https://github.com/rogervinas/spring-boot-docker/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/rogervinas/spring-boot-docker/actions/workflows/gradle.yml)

# Spring Boot with Docker

In this sample we will create a **🍀 Spring Boot Application** with a simple /hello endpoint and then distribute it as a **🐳 Docker image**

And of course we want to ensure the Docker image works, so we will test it using [Testcontainers](https://www.testcontainers.org/) 🤩

Ready? Let's go!

1. We start at [Spring Initialzr](https://start.spring.io/#!type=gradle-project&language=kotlin&platformVersion=2.6.2&packaging=jar&jvmVersion=11&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&dependencies=webflux) and create an empty **Spring Boot** project with **Webflux** and **Kotlin**

2. Then we add this simple test

```kotlin
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApplicationTests {

  @LocalServerPort
  private var port: Int = 0

  @Test
  fun `should say hello`() {
    WebClient.builder()
      .baseUrl("http://localhost:$port").build()
      .get().uri("/hello")
      .exchangeToMono { response ->
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        assertThat(response.bodyToMono(String::class.java)).isEqualTo("hello!")
        response.releaseBody()
      }
  }
}
```

3. And we add this simple implementation ...

```kotlin
@RestController
class HelloController {

    @GetMapping("/hello")
    fun hello() = "hello!"
}
```

... and now our test is 🟩👏

4. Next we need to generate a docker image ... 🤔

Some alternatives are documented in [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker) and [Topical Guide on Docker](https://spring.io/guides/topicals/spring-boot-docker)

And luckily for us, it is as easy as use the task `bootBuildImage` of the [Spring Boot's Gradle plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image):

```bash
./gradlew bootBuildImage
```

So far so good! 😁

Now we have to test the generated docker image ...

5. First we use [org.unbroken-dome.test-sets](https://plugins.gradle.org/plugin/org.unbroken-dome.test-sets) to create a new test source root named `container-test`:

```kotlin
plugins {
  id("org.unbroken-dome.test-sets") version "4.0.0"
}

testSets {
    "container-test"()
}

tasks.get("container-test").dependsOn("bootBuildImage")

```

Note that `bootBuildImage` task is executed before `container-test` task, so we ensure we are always testing the docker image we've just built from the current source code

6. Then we create test using [Testcontainers](https://www.testcontainers.org/features/creating_container/#creating-a-generic-container-based-on-an-image) and [JUnit5](https://www.testcontainers.org/test_framework_integration/junit_5)

```kotlin
@Testcontainers
class ApplicationContainerTests {

  companion object {

    private const val appPort = 8080

    @Container
    private val app = KGenericContainer(System.getProperty("docker.image"))
      .withExposedPorts(appPort)
  }

  @Test
  fun `should say hello`() {
    // ...
  }
}
```

7. Last thing we need to do is set the value of the system property `docker.image` before running any test:

```
tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("docker.image", "${project.name}:${project.version}")
}
```

As `${project.name}:${project.version}` is the default value used by `bootBuildImage` task

And that is all! Happy coding! 💙
