package repositories

import javax.inject.{Inject, Singleton}

import model.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserRepositoryImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends UserRepository with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  class UserTable(tag: Tag) extends Table[User](tag, "USER") {

    def id: Rep[Option[Long]] = column[Long]("ID", O.PrimaryKey, O.AutoInc).?

    def firstName: Rep[String] = column[String]("FIRST_NAME")

    def email: Rep[String] = column[String]("EMAIL")

    override def * = (id, firstName, email) <> (User.tupled, User.unapply)
  }

  private val Users = TableQuery[UserTable]

  override def add(user: User): Future[Unit] = {

    val insertIfNotDuplicateEmail = Users.forceInsertQuery {
      val emailExists = (for (u <- Users if u.email === user.email.bind) yield u).exists
      val insert = (None, user.firstName.bind, user.email.bind) <> (User.apply _ tupled, User.unapply)
      for (u <- Query(insert) if !emailExists) yield u
    }

    db.run(insertIfNotDuplicateEmail)
      .map {
        case 0 => throw new Exception("Cannot add user - duplicate email")
        case _ => ()
      }
  }

  override def getById(id: Long): Future[Option[User]] = {
    db.run(Users.filter(_.id === id).result.headOption)
  }

  override def getByEmail(email: String): Future[Option[User]] = {
    db.run(Users.filter(_.email === email).result.headOption)
  }

  override def getAll(): Future[Seq[User]] = {
    db.run(Users.result)
  }
}
