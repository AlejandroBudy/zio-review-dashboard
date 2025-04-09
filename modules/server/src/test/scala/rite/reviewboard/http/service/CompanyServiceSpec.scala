package rite.reviewboard.services

import rite.reviewboard.domain.Company
import rite.reviewboard.http.requests.CreateCompanyRequest
import rite.reviewboard.repositories.CompanyRepository
import zio.*
import zio.test.*

object CompanyServiceSpec extends ZIOSpecDefault:

  val service = ZIO.serviceWithZIO[CompanyService]
  val stubRepository = ZLayer.succeed {
    new CompanyRepository:
      val db = collection.mutable.Map[Long, Company]()

      override def create(company: Company): Task[Company] =
        ZIO.succeed {
          val nextId     = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = company.copy(id = nextId)
          db += (nextId -> newCompany)
          newCompany
        }

      override def update(id: Long, op: Company => Company): Task[Company] =
        ZIO.attempt {
          val company = db(id) // can crash
          db += (id -> op(company))
          company
        }

      override def delete(id: Long): Task[Company] =
        ZIO.attempt {
          val company = db(id)
          db -= id
          company
        }

      override def getById(id: Long): Task[Option[Company]] =
        ZIO.succeed(db.get(id))

      override def getBySlug(slug: String): Task[Option[Company]] =
        ZIO.succeed(db.values.find(_.slug == slug))

      override def get: Task[List[Company]] =
        ZIO.succeed(db.values.toList)
  }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyServiceTest")(
      test("Create company") {
        val program = service(_.create(CreateCompanyRequest("Company 1", "http://company1.com")))

        assertZIO(program)(
          Assertion.assertion("Created company") { company =>
            company.name == "Company 1" &&
            company.slug == "company-1" &&
            company.url == "http://company1.com"
          }
        )
      },
      test("Get by id") {
        val program = for
          company <- service(_.create(CreateCompanyRequest("Company 1", "http://company1.com")))
          result  <- service(_.getById(company.id))
        yield (company, result)

        assertZIO(program)(
          Assertion.assertion("Get by id") { case (company, result) =>
            result.contains(company)
          }
        )
      },
      test("Get by slug") {
        val program = for
          company <- service(_.create(CreateCompanyRequest("Company 1", "http://company1.com")))
          result  <- service(_.getBySlug(company.slug))
        yield (company, result)

        assertZIO(program)(
          Assertion.assertion("Get by slug") { case (company, result) =>
            result.contains(company)
          }
        )
      },
      test("Get all") {
        val program = for
          company1 <- service(_.create(CreateCompanyRequest("Company 1", "http://company1.com")))
          company2 <- service(_.create(CreateCompanyRequest("Company 2", "http://company2.com")))
          results  <- service(_.getAll())
        yield (results, company1, company2)

        assertZIO(program)(
          Assertion.assertion("Get all") { case (results, company1, company2) =>
            results.toSet == Set(company1, company2)
          }
        )
      }
    ).provide(CompanyServiceLive.layer, stubRepository)
end CompanyServiceSpec
