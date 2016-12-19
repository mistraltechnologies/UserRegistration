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

class UserRepositoryImplSpec extends PlaySpec with OneAppPerTest {

  implicit override def newAppForTest(testData: TestData): Application =
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

  class WithUserRepository {
    private val app2userRepository = Application.instanceCache[UserRepositoryImpl]
    val userRepository: UserRepositoryImpl = app2userRepository(app)

    val user1 = User(1, "Bob", "bob@bob.com")
    val user2 = User(2, "Bill", "bill@bill.com")
    val user3 = User(3, "Ben", "ben@ben.com")
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
      users.toSet must equal(Set(user1, user2, user3))
    }


    "retrieve a matching user by id" in new WithUserRepository() {

      val futureMaybeUser = for {
        _ <- userRepository.add(user1)
        _ <- userRepository.add(user2)
        _ <- userRepository.add(user3)
        user <- userRepository.getById(2)
      } yield user

      val maybeUser = Await.result(futureMaybeUser, 1 second)

      maybeUser must equal(Some(user2))
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

      maybeUser must equal(Some(user3))
    }


    "retrieve no user by email when not found" in new WithUserRepository() {
      val futureMaybeUser = userRepository.getByEmail("ben@ben.com")

      val maybeUser = Await.result(futureMaybeUser, 1 second)

      maybeUser must equal(Option.empty)
    }
  }

}
