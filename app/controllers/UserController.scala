package controllers

import javax.inject._

import model.User
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(userService: UserService) extends Controller {

  def registerUser = Action.async(parse.json) { request =>
    val firstName = (request.body \ "firstName").as[String]
    val email = (request.body \ "email").as[String]

    val user = User(None, firstName, email)

    userService.registerUser(user).map(_ => NoContent).recover { case e => BadRequest(e.getMessage) }
  }

  def getUserByEmail(email: String) = Action.async {
    userService.getUserByEmail(email).map {
      case Some(u) => Ok(Json.toJson(u))
      case None => NotFound(email)
    }
  }

}
