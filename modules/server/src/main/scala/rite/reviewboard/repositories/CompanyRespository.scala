package rite.reviewboard.repositories

import io.getquill.*
import io.getquill.jdbczio.Quill
import rite.reviewboard.domain.Company
import zio.*

trait CompanyRepository:
  def create(company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def get: Task[List[Company]]

final class CompanyRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CompanyRepository:

  import quill.*

  inline given SchemaMeta[Company] = schemaMeta[Company]("companies")
  inline given InsertMeta[Company] = insertMeta[Company](_.id)
  inline given UpdateMeta[Company] = updateMeta[Company](_.id)

  override def create(company: Company): Task[Company] = run {
    query[Company]
      .insertValue(lift(company))
      .returning(r => r)
  }

  override def getById(id: Long): Task[Option[Company]] = run {
    query[Company]
      .filter(_.id == lift(id))

  }.map(_.headOption)

  override def getBySlug(slug: String): Task[Option[Company]] = run {
    query[Company]
      .filter(_.slug == lift(slug))
  }.map(_.headOption)

  override def get: Task[List[Company]] = run(query[Company])

  override def update(id: Long, op: Company => Company): Task[Company] = for
    current <- getById(id).someOrFail(new RuntimeException("Company not found"))
    updated <- run {
      query[Company]
        .filter(_.id == lift(id))
        .updateValue(lift(op(current)))
        .returning(r => r)
    }
  yield updated

  override def delete(id: Long): Task[Company] = run {
    query[Company]
      .filter(_.id == lift(id))
      .delete
      .returning(r => r)
  }
end CompanyRepositoryLive

object CompanyRepositoryLive:
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase]].map { quill =>
      new CompanyRepositoryLive(quill)
    }
  }

object CompanyRepositoryDemo extends ZIOAppDefault:
  val program = for
    repo <- ZIO.service[CompanyRepository]
    _    <- repo.create(Company(0, "test", "test", "test"))
  yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = program.provide(
    CompanyRepositoryLive.layer,
    Quill.Postgres.fromNamingStrategy(SnakeCase),
    Quill.DataSource.fromPrefix("db") // Assuming you have a Quill Postgres context configured
  )
