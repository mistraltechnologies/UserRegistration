package services

import javax.inject.{Inject, Singleton}

import model.User
import repositories.UserRepository

import scala.concurrent.Future

@Singleton
class UserServiceImpl @Inject()(userRepository: UserRepository) extends UserService {

  override def registerUser(user: User): Future[Unit] = {
    userRepository.add(user)
  }

  override def getUserByEmail(email: String): Future[Option[User]] = {
    userRepository.getByEmail(email)
  }
}


