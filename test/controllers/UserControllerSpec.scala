package controllers

import akka.stream.Materializer
import model.User
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserService
import org.mockito.Mockito._
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UserControllerSpec extends PlaySpec with MockitoSugar with Results with OneAppPerSuite {

  implicit lazy val materializer: Materializer = app.materializer

  "UserController#registerUser" should {

    trait WithFixture {
      val mockUserService = mock[UserService]
      val user = User(None, "Bob", "bob@bob.com")

      val request = FakeRequest(POST, "/")
        .withJsonBody(Json.parse(
          """
            |{
            |"firstName": "Bob",
            |"email": "bob@bob.com"
            |}
          """.stripMargin))

    }

    "call UserService passing user returning no content when succeeds" in new WithFixture {
      val controller = new UserController(mockUserService)

      when(mockUserService.registerUser(user)).thenReturn(Future {})

      val result: Future[Result] = call(controller.registerUser(), request)

      status(result) mustEqual NO_CONTENT
      verify(mockUserService).registerUser(user)
    }

    "call UserService passing user returning bad request and error message when fails" in new WithFixture {
      val controller = new UserController(mockUserService)

      when(mockUserService.registerUser(user)).thenReturn(Future { throw new Exception("error message") })

      val result: Future[Result] = call(controller.registerUser(), request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual "error message"
      verify(mockUserService).registerUser(user)
    }
  }

}
