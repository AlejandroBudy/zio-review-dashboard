package rite.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoint:

  val healthEndpoint = sttp.tapir.endpoint
    .tag("Health")
    .name("health")
    .get
    .in("health")
    .out(plainBody[String])
