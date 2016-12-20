package services

import model.User

import scala.concurrent.Future

trait UserService {

  def registerUser(user: User): Future[Unit]
}
