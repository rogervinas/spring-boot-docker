package com.rogervinas.springbootdocker

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
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
        private val app = KGenericContainer("spring-boot-docker:0.0.1-SNAPSHOT")
                .withExposedPorts(appPort)
    }

    @Test
    fun `should say hello`() {
        val body = WebClient.builder()
                .baseUrl("http://localhost:${app.getMappedPort(appPort)}").build()
                .get().uri("/hello")
                .exchangeToMono {
                    it.bodyToMono(String::class.java)
                }.block()

        Assertions.assertThat(body).isEqualTo("hello!")
    }
}