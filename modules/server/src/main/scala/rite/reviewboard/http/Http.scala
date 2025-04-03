package rite.reviewboard.http

import rite.reviewboard.http.controllers.BaseController
import rite.reviewboard.http.controllers.CompanyController
import rite.reviewboard.http.controllers.HealthController

object Http:

  private def make =
    for
      health    <- HealthController.makeZIO
      companies <- CompanyController.makeZIO
    yield List(health, companies)

  extension (controllers: List[BaseController])
    private def routes =
      controllers.flatMap(_.routes)

  val endpointsZIO = make.map(_.routes)
