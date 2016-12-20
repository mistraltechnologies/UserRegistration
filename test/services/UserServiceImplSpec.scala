package services

import model.User
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import repositories.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class UserServiceImplSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  "UserService#registerUser" should {

    trait WithFixture {
      val mockUserRepository = mock[UserRepository]
      val user = User(None, "Bob", "bob@bob.com")
    }

    "call UserRepository passing user and return future that succeeds when repository future succeeds" in new WithFixture() {
      val userService = new UserServiceImpl(mockUserRepository)

      when(mockUserRepository.add(user)).thenReturn(Future {})

      val result = userService.registerUser(user)

      verify(mockUserRepository).add(user)
      result.value.get must matchPattern { case Success(_) => }
    }

    "call UserRepository passing user and return future that fails when repository future fails" in new WithFixture() {
      val userService = new UserServiceImpl(mockUserRepository)

      when(mockUserRepository.add(user)).thenReturn(Future {
        throw new Exception()
      })

      val result = userService.registerUser(user)

      verify(mockUserRepository).add(user)
      whenReady(result.failed) { e =>
        e mustBe an[Exception]
      }
    }
  }
}
