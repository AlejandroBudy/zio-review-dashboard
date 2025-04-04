package rite.reviewboard.services

import rite.reviewboard.domain.Company
import rite.reviewboard.http.requests.CreateCompanyRequest
import zio.*

trait CompanyService:
  def create(request: CreateCompanyRequest): Task[Company]
  def getAll(): Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]

import scala.collection.mutable

final class CompanyServiceInMemory extends CompanyService:
  val db = mutable.Map[Long, Company]()

  override def create(request: CreateCompanyRequest): Task[Company] =
    ZIO.succeed {
      val id         = db.keys.maxOption.getOrElse(0L) + 1
      val newCompany = request.toCompany(id)
      db += (id -> newCompany)
      newCompany
    }

  override def getAll(): Task[List[Company]]            = ZIO.succeed(db.values.toList)
  override def getById(id: Long): Task[Option[Company]] = ZIO.succeed(db.get(id))

  override def getBySlug(slug: String): Task[Option[Company]] =
    ZIO.succeed(db.values.find(_.slug == slug))

object CompanyService:
  val layer = ZLayer.succeed(new CompanyServiceInMemory)
