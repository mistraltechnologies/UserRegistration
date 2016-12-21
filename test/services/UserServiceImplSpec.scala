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
      result.value.get mustEqual Success(())
    }

    "call UserRepository passing user and return future that fails when repository future fails" in new WithFixture() {
      val userService = new UserServiceImpl(mockUserRepository)

      when(mockUserRepository.add(user)).thenReturn(Future {
        throw new Exception()
      })

      val result = userService.registerUser(user)

      whenReady(result.failed) { e =>
        e mustBe an[Exception]
      }
    }
  }


  "UserService#getUserByEmail" should {

    trait WithFixture {
      val mockUserRepository = mock[UserRepository]
      val user = User(Some(1), "Bob", "bob@bob.com")
    }

    "call UserRepository passing email and return future that succeeds when repository future succeeds" in new WithFixture {
      val userService = new UserServiceImpl(mockUserRepository)

      when(mockUserRepository.getByEmail("bob@bob.com")).thenReturn(Future { Some(user) })

      val result = userService.getUserByEmail("bob@bob.com")

      result.value.get mustEqual Success(Some(user))
    }

    "call UserRepository passing email and return future that fails when repository future fails" in new WithFixture {
      val userService = new UserServiceImpl(mockUserRepository)

      when(mockUserRepository.getByEmail("bob@bob.com")).thenReturn(Future {
        throw new Exception()
      })

      val result = userService.getUserByEmail("bob@bob.com")

      whenReady(result.failed) { e =>
        e mustBe an[Exception]
      }
    }
  }
}
