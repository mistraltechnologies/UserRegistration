package repositories

import com.google.inject.ImplementedBy
import model.User

import scala.concurrent.Future

@ImplementedBy(classOf[UserRepositoryImpl])
trait UserRepository {

  def add(user: User) : Future[Unit]
  def getById(id: Long) : Future[Option[User]]
  def getByEmail(email: String) : Future[Option[User]]
  def getAll() : Future[Seq[User]]

}
