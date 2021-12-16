package com.example.springfluxresponse

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.bson.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.data.mongodb.gridfs.GridFsObject
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Flux
import java.nio.charset.StandardCharsets

@WebFluxTest(FileController::class)
class FileControllerTest(
  @Autowired val webTestClient: WebTestClient
) {

  @MockkBean
  lateinit var repo: FileRepository

  @BeforeEach
  fun before() {
    clearAllMocks()
  }

  @Test
  fun `download is ok`() {
    val gridfsResource = mockk<ReactiveGridFsResource>()
    val gridfsOptions = mockk<GridFsObject.Options>()
    coEvery { gridfsResource.options } returns gridfsOptions
    coEvery { gridfsResource.downloadStream } returns asFluxDataBuffer(fileData)
    coEvery { gridfsOptions.metadata } returns Document(mapOf("contentType" to fileType, "fileName" to fileName))
    coEvery { repo.findById(fileId) } returns gridfsResource

    webTestClient.get()
      .uri("/file/$fileId")
      .exchange()
      .expectStatus().isOk
      .expectBody<String>().isEqualTo(fileData)
  }

  // --- mocks'n'stuff ---

  val fileId = FileId()
  val fileType = MediaType.TEXT_PLAIN_VALUE
  val fileData = "testdata"
  val fileName = "testfile.txt"
}

fun asFluxDataBuffer(text: String): Flux<DataBuffer> {
  val buffer = DefaultDataBufferFactory().allocateBuffer()
  val dataBuffer = buffer.write(text.toByteArray(StandardCharsets.UTF_8))
  return Flux.just(dataBuffer)
}
