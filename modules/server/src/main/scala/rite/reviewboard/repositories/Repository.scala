package rite.reviewboard.repositories

import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres
import zio.ZLayer

object Repository:

  private def quillLayer =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  private def dataSourceLayer =
    Quill.DataSource.fromPrefix("db")

  val layer: ZLayer[Any, Throwable, Postgres[SnakeCase.type]] = dataSourceLayer >>> quillLayer
