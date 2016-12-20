package controllers

import javax.inject._

import model.User
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

}
