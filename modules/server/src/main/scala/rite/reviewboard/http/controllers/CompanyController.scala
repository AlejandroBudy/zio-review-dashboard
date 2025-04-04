package rite.reviewboard.http.controllers

import collection.mutable
import rite.reviewboard.domain.Company
import rite.reviewboard.http.endpoints.CompanyEndpoints
import rite.reviewboard.services.CompanyService
import sttp.tapir.server.ServerEndpoint
import zio.*

final class CompanyController private (service: CompanyService)
    extends BaseController
    with CompanyEndpoints:

  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .serverLogicSuccess { req => service.create(req) }

  val getAll: ServerEndpoint[Any, Task] =
    getEndpoint
      .serverLogicSuccess(_ => service.getAll())

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint
      .serverLogicSuccess { id =>
        ZIO
          .attempt(id.toLong)
          .flatMap(service.getById)
          .catchSome { case _: NumberFormatException =>
            service.getBySlug(id)
          }
      }

  val routes = List(create, getAll, getById)

object CompanyController:
  def makeZIO = for service <- ZIO.service[CompanyService]
  yield new CompanyController(service)
