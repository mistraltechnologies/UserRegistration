package services

import com.google.inject.ImplementedBy
import model.User

import scala.concurrent.Future

@ImplementedBy(classOf[UserServiceImpl])
trait UserService {

  def registerUser(user: User): Future[Unit]

  def getUserByEmail(email: String) : Future[Option[User]]
}
