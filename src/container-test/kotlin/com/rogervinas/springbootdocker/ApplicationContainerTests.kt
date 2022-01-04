package com.rogervinas.springbootdocker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

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
    WebClient.builder()
      .baseUrl("http://localhost:${app.getMappedPort(appPort)}").build()
      .get().uri("/hello")
      .exchangeToMono { response ->
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        assertThat(response.bodyToMono(String::class.java)).isEqualTo("hello!")
        response.releaseBody()
      }
  }
}
