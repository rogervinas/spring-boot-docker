package com.rogervinas.springbootdocker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

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
    val responseBody = WebClient.builder()
      .baseUrl("http://localhost:${app.getMappedPort(APP_PORT)}").build()
      .get().uri("/hello")
      .exchangeToMono { response ->
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        response.bodyToMono(String::class.java)
      }.block()
    assertThat(responseBody).isEqualTo("hello!")
  }
}
