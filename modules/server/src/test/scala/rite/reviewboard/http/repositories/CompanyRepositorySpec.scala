package rite.reviewboard.repositories

import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import rite.reviewboard.domain.Company
import zio.*
import zio.test.*

object CompanyRepositorySpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyRepositorySpec")(
      test("Create company") {
        val program = for
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(
            Company(0, name = "Company 1", slug = "company-1", "http://company1.com")
          )
        yield company

        assertZIO(program)(
          Assertion.assertion("Created company") { company =>
            company.name == "Company 1" &&
            company.slug == "company-1" &&
            company.url == "http://company1.com"
          }
        )
      }
    ).provide(CompanyRepositoryLive.layer, dataSourceLayer, Repository.quillLayer, Scope.default)

  private def createContainer =
    val container: PostgreSQLContainer[Nothing] =
      PostgreSQLContainer("postgres").withInitScript("sql/companies.sql")

    container.start()
    container

  private def createDatasource(container: PostgreSQLContainer[Nothing]): DataSource =
    val datasource = new PGSimpleDataSource()
    datasource.setUrl(container.getJdbcUrl)
    datasource.setUser(container.getUsername)
    datasource.setPassword(container.getPassword)
    datasource

  val dataSourceLayer: ZLayer[Scope, Throwable, DataSource] = ZLayer {
    for
      container <- ZIO.acquireRelease(ZIO.attempt(createContainer))(container =>
        ZIO.attempt(container.stop()).ignoreLogged
      )
      dataSource <- ZIO.attempt(createDatasource(container))
    yield dataSource
  }
end CompanyRepositorySpec
