package rite.reviewboard.http.controllers

import rite.reviewboard.http.endpoints.HealthEndpoint
import zio.*

class HealthController private extends HealthEndpoint with BaseController:

  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good!"))

  val routes = List(health)

object HealthController:
  val makeZIO = ZIO.succeed(new HealthController)
