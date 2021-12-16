package com.example.springfluxresponse

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.stereotype.Repository

@Repository
class FileRepository(val db: ReactiveGridFsTemplate) {

  suspend fun findById(id: FileId): ReactiveGridFsResource? {
    val gridfsFile = db.findFirst(queryById(id)).awaitFirstOrNull()
      ?: return null
    return db.getResource(gridfsFile).awaitFirstOrNull()
  }

  private fun queryById(id: FileId) = query(where("_id").isEqualTo(id))
}
