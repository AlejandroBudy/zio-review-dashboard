package rite.reviewboard

import rite.reviewboard.http.controllers.*
import rite.reviewboard.http.Http
import rite.reviewboard.repositories.CompanyRepositoryLive
import rite.reviewboard.repositories.Repository
import rite.reviewboard.services.*
import sttp.tapir.*
import sttp.tapir.server.ziohttp.*
import zio.*
import zio.http.Server

object Application extends ZIOAppDefault:

  val serverProgram =
    for
      controllers <- Http.endpointsZIO
      _ <- Server.serve(
        ZioHttpInterpreter(
          ZioHttpServerOptions.default
        ).toHttp(controllers)
      )
      _ <- Console.printLine("### Server started")
    yield ()

  override def run = serverProgram.provide(
    Server.default,
    CompanyServiceLive.layer,
    CompanyRepositoryLive.layer,
    Repository.layer
  )
