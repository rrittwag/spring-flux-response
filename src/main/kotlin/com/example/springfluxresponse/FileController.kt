package com.example.springfluxresponse

import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

typealias FileId = ObjectId

data class FileMetadata(
  val contentType: String,
  val fileName: String
)

@RestController
class FileController(val repo: FileRepository) {

  @GetMapping("file/{id:[0-9a-f]+}")
  suspend fun download(
    @PathVariable id: FileId,
  ): ResponseEntity<Flux<DataBuffer>> {
    val gridfsResource = repo.findById(id)
      ?: throw IllegalArgumentException("file $id not found")

    val contentType = parseMetadata(gridfsResource.options.metadata)?.contentType
      ?: throw IllegalArgumentException("file metadata corrupted")

    return ResponseEntity.ok()
      .contentType(MediaType.valueOf(contentType))
      .body(gridfsResource.downloadStream)
  }
}

private fun parseMetadata(document: Document): FileMetadata? {
  val contentType = document.get("contentType", String::class.java) ?: return null
  val fileName = document.get("fileName", String::class.java) ?: return null
  return FileMetadata(contentType, fileName)
}
