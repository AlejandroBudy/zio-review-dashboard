package rite.reviewboard.http.controllers

import rite.reviewboard.domain.Company
import rite.reviewboard.http.requests.CreateCompanyRequest
import rite.reviewboard.services.CompanyService
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.json.*
import zio.test.*

object CompanyControllerSpec extends ZIOSpecDefault:

  given MonadError[Task] = new RIOMonadError[Any]

  private val stubCompany = Company(
    id = 1,
    name = "Company 1",
    slug = "company-1",
    url = "http://company1.com"
  )

  private def serviceStub = new CompanyService:
    override def create(req: CreateCompanyRequest): Task[Company] =
      ZIO.succeed(stubCompany)

    override def getAll(): Task[List[Company]] =
      ZIO.succeed(List(stubCompany))

    override def getById(id: Long): Task[Option[Company]] =
      ZIO.succeed(if id == 1 then Some(stubCompany) else None)

    override def getBySlug(slug: String): Task[Option[Company]] =
      ZIO.succeed(if slug == "company-1" then Some(stubCompany) else None)

  private def backendStubZIO(endpointFun: CompanyController => ServerEndpoint[Any, Task]) =
    for
      controller <- CompanyController.makeZIO
      backendStub = TapirStubInterpreter[Task, Any](SttpBackendStub(MonadError[Task]))
        .whenServerEndpointRunLogic(
          endpointFun(controller)
        )
        .backend()
    yield backendStub

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("CompanyControllerSpec")(
    test("post company") {
      val program = for
        backendStub <- backendStubZIO(_.create)
        response <- basicRequest
          .post(uri"/companies")
          .body(
            CreateCompanyRequest(
              name = "Company 1",
              url = "http://company1.com"
            ).toJson
          )
          .send(backendStub)
      yield response.body

      assertZIO(program)(
        Assertion.assertion("inspect http response from create") { response =>
          response.toOption
            .flatMap(_.fromJson[Company].toOption) // Option company
            .contains(stubCompany)
        }
      )
    },
    test("Get all") {
      val program = for
        backendStub <- backendStubZIO(_.getAll)
        response <- basicRequest
          .get(uri"/companies")
          .send(backendStub)
      yield response.body

      assertZIO(program)(
        Assertion.assertion("inspect http response from get all") { response =>
          response.toOption
            .flatMap(_.fromJson[List[Company]].toOption) // Option company
            .contains(List(stubCompany))
        }
      )
    },
    test("Get by id") {
      val program = for
        backendStub <- backendStubZIO(_.getById)
        response <- basicRequest
          .get(uri"/companies/1")
          .send(backendStub)
      yield response.body

      assertZIO(program)(
        Assertion.assertion("inspect http response from get by id") { response =>
          response.toOption
            .flatMap(_.fromJson[Company].toOption) // Option company
            .contains(stubCompany)
        }
      )
    },
    test("Get by slug") {
      val program = for
        backendStub <- backendStubZIO(_.getById)
        response <- basicRequest
          .get(uri"/companies/company-1")
          .send(backendStub)
      yield response.body

      assertZIO(program)(
        Assertion.assertion("inspect http response from get by slug") { response =>
          response.toOption
            .flatMap(_.fromJson[Company].toOption) // Option company
            .contains(stubCompany)
        }
      )
    }
  ).provide(ZLayer.succeed(serviceStub)) // Provide the service stub to the test environment
end CompanyControllerSpec
