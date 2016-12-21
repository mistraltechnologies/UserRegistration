package controllers

import akka.stream.Materializer
import model.User
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

    "return NoContent when succeeds" in new WithFixture {
      val controller = new UserController(mockUserService)

      when(mockUserService.registerUser(user)).thenReturn(Future {})

      val result: Future[Result] = call(controller.registerUser(), request)

      status(result) mustEqual NO_CONTENT
      verify(mockUserService).registerUser(user)
    }

    "return BadRequest and error message when fails" in new WithFixture {
      val controller = new UserController(mockUserService)

      when(mockUserService.registerUser(user)).thenReturn(Future {
        throw new Exception("error message")
      })

      val result: Future[Result] = call(controller.registerUser(), request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual "error message"
      verify(mockUserService).registerUser(user)
    }
  }

  "UserController#getUserByEmail" should {

    trait WithFixture {
      val mockUserService = mock[UserService]
      val user = User(Some(1), "Bob", "bob@bob.com")
    }

    "return Ok with user in content body when succeeds" in new WithFixture {

      val controller = new UserController(mockUserService)

      when(mockUserService.getUserByEmail("bob@bob.com")).thenReturn( Future { Some(user) } )

      val result = call(controller.getUserByEmail("bob@bob.com"), FakeRequest(GET, "/"))

      status(result) mustEqual OK
      contentAsJson(result) mustEqual Json.parse(
        """
          |{
          |"id": 1,
          |"firstName": "Bob",
          |"email": "bob@bob.com"
          |}
        """.stripMargin
      )

    }

    "return NotFound when user does not exist" in new WithFixture {

      val controller = new UserController(mockUserService)

      when(mockUserService.getUserByEmail("bob@bob.com")).thenReturn( Future { None })

      val result = call(controller.getUserByEmail("bob@bob.com"), FakeRequest(GET, "/"))

      status(result) mustEqual NOT_FOUND
    }

  }

}
