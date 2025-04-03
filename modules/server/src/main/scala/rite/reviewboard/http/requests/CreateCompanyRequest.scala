package rite.reviewboard.http.requests

import rite.reviewboard.domain.Company

final case class CreateCompanyRequest(
    name: String,
    url: String,
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: Option[List[String]] = None
):
  def toCompany(id: Long) = Company.make(
    id = id,
    name = name,
    url = url,
    location = location,
    country = country,
    industry = industry,
    image = image,
    tags = tags.getOrElse(List.empty)
  )

object CreateCompanyRequest:
  import zio.json.DeriveJsonCodec
  import zio.json.JsonCodec

  given codec: JsonCodec[CreateCompanyRequest] = DeriveJsonCodec.gen[CreateCompanyRequest]
