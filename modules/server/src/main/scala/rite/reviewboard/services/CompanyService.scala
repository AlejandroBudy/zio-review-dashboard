package rite.reviewboard.services

import rite.reviewboard.domain.Company
import rite.reviewboard.http.requests.CreateCompanyRequest
import rite.reviewboard.repositories.CompanyRepository
import zio.*

trait CompanyService:
  def create(request: CreateCompanyRequest): Task[Company]
  def getAll(): Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]

final class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService:

  def create(request: CreateCompanyRequest): Task[Company] = repo.create(request.toCompany(1L))
  def getAll(): Task[List[Company]]                        = repo.get
  def getById(id: Long): Task[Option[Company]]             = repo.getById(id)
  def getBySlug(slug: String): Task[Option[Company]]       = repo.getBySlug(slug)

object CompanyServiceLive:
  val layer = ZLayer {
    ZIO
      .service[CompanyRepository]
      .map(new CompanyServiceLive(_))
  }
