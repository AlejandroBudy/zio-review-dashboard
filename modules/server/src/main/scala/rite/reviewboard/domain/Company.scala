package rite.reviewboard.domain

final case class Company(
    id: Long,
    slug: String,
    name: String,
    url: String,
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: List[String] = List.empty
)

object Company:
  import zio.json.DeriveJsonCodec
  import zio.json.JsonCodec

  given codec: JsonCodec[Company] = DeriveJsonCodec.gen[Company]

  def make(
      id: Long,
      name: String,
      url: String,
      location: Option[String] = None,
      country: Option[String] = None,
      industry: Option[String] = None,
      image: Option[String] = None,
      tags: List[String] = List.empty
  ): Company =
    Company(
      id = id,
      slug = makeSlug(name),
      name = name,
      url = url,
      location = location,
      country = country,
      industry = industry,
      image = image,
      tags = tags
    )

  private def makeSlug(name: String) = name
    .replaceAll(" +", " ")
    .split(" ")
    .map(_.toLowerCase)
    .mkString("-")
end Company
