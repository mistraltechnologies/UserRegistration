package model

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads, Writes}

case class User(id: Option[Long], firstName: String, email: String)

object User {

  implicit val userWrites: Writes[User] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "firstName").write[String] and
      (JsPath \ "email").write[String]
    ) ((u: User) => (u.id.get, u.firstName, u.email))

  implicit val userReads: Reads[User] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "firstName").read[String] and
      (JsPath \ "email").read[String]
    ) ((id, firstName, email) => User(Some(id), firstName, email))
}


