package repositories

import model.User
import org.scalatest.TestData
import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Mode}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.Failure

class UserRepositoryImplSpec extends PlaySpec with OneAppPerTest {

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .configure(inMemoryDatabase())
      .bindings(bind[UserRepository].to[UserRepositoryImpl])
      .in(Mode.Test)
      .build()

  def inMemoryDatabase(name: String = "default"): Map[String, String] = {
    Map(
      ("slick.dbs." + name + ".driver") -> "slick.driver.H2Driver$",
      ("slick.dbs." + name + ".db.driver") -> "org.h2.Driver",
      ("slick.dbs." + name + ".db.url") -> ("jdbc:h2:mem:play-test-" + scala.util.Random.nextInt)
    )
  }

  trait WithUserRepository {
    private val app2userRepository = Application.instanceCache[UserRepositoryImpl]
    val userRepository: UserRepositoryImpl = app2userRepository(app)

    val user1 = User(None, "Bob", "bob@bob.com")
    val user2 = User(None, "Bill", "bill@bill.com")
    val user3 = User(None, "Ben", "ben@ben.com")

    val user1WithId = user1.copy(id = Some(1))
    val user2WithId = user2.copy(id = Some(2))
    val user3WithId = user3.copy(id = Some(3))
  }

  "UserRepository" should {

    "add users and retrieve all" in new WithUserRepository() {

      val futureUsers = for {
        _ <- userRepository.add(user1)
        _ <- userRepository.add(user2)
        _ <- userRepository.add(user3)
        users <- userRepository.getAll()
      } yield users

      val users = Await.result(futureUsers, 1 second)

      users.size must equal(3)
      users.toSet must equal(Set(user1WithId, user2WithId, user3WithId))
    }


    "retrieve a matching user by id" in new WithUserRepository() {

      val futureMaybeUser = for {
        _ <- userRepository.add(user1)
        _ <- userRepository.add(user2)
        _ <- userRepository.add(user3)
        user <- userRepository.getById(2)
      } yield user

      val maybeUser = Await.result(futureMaybeUser, 1 second)

      maybeUser must equal(Some(user2WithId))
    }


    "retrieve no user by id when not found" in new WithUserRepository() {
      val futureMaybeUser = userRepository.getById(1)

      val maybeUser = Await.result(futureMaybeUser, 1 second)

      maybeUser must equal(Option.empty)
    }


    "retrieve a matching user by email" in new WithUserRepository() {

      val futureMaybeUser = for {
        _ <- userRepository.add(user1)
        _ <- userRepository.add(user2)
        _ <- userRepository.add(user3)
        user <- userRepository.getByEmail("ben@ben.com")
      } yield user

      val maybeUser = Await.result(futureMaybeUser, 1 second)

      maybeUser must equal(Some(user3WithId))
    }


    "retrieve no user by email when not found" in new WithUserRepository() {
      val futureMaybeUser = userRepository.getByEmail("ben@ben.com")

      val maybeUser = Await.result(futureMaybeUser, 1 second)

      maybeUser must equal(Option.empty)
    }


    "not add user with duplicate email" in new WithUserRepository() {
      val futureAdded = for {
        _ <- userRepository.add(user1)
        user <- userRepository.add(User(None, "bobby", "bob@bob.com"))
      } yield user

      val added = Await.ready(futureAdded, 1 second)

      added.value.get must matchPattern { case Failure(_) => }
    }
  }

}
