package rite.reviewboard.http.controllers

import collection.mutable
import rite.reviewboard.domain.Company
import rite.reviewboard.http.endpoints.CompanyEndpoints
import sttp.tapir.server.ServerEndpoint
import zio.*

final class CompanyController private extends BaseController with CompanyEndpoints:

  val db = mutable.Map[Long, Company]()

  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .serverLogicSuccess { req =>
        ZIO.succeed {
          val id         = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = req.toCompany(id)
          db += (id -> newCompany)
          newCompany
        }
      }

  val getAll: ServerEndpoint[Any, Task] =
    getEndpoint
      .serverLogicSuccess(_ => ZIO.succeed(db.values.toList))

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint
      .serverLogicSuccess { id =>
        ZIO.attempt(id.toLong).map(db.get)
      }

  val routes = List(create, getAll, getById)

object CompanyController:
  def makeZIO = ZIO.succeed(new CompanyController)
