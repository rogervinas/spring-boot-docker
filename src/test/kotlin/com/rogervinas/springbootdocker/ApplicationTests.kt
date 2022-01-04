package com.rogervinas.springbootdocker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient

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
