import java.util.UUID

import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HomeController" should {

    "render the index page" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Your new application is ready.")
    }

  }

  "CountController" should {

    "return an increasing count" in {
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "0"
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "1"
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "2"
    }

  }

  "UserController" should {

    trait WithFixture {
      val uniqueEmail = UUID.randomUUID().toString + "@uuid.com"

      val newUserAsJson = Json.parse(
        s"""
           |{
           |"firstName": "Dave",
           |"email": "$uniqueEmail"
           |}
          """.stripMargin)

      val newUserPostRequest = FakeRequest(POST, "/users")
        .withHeaders((CONTENT_TYPE, JSON))
        .withJsonBody(newUserAsJson)

    }

    "register user and return 204 (no content)" in new WithFixture {

      val response = route(app, newUserPostRequest).get

      status(response) mustBe NO_CONTENT
    }

    "return 400 response when registering user with duplicate email" in new WithFixture {

      val response1 = route(app, newUserPostRequest).get

      status(response1) mustBe NO_CONTENT

      val response2 = route(app, newUserPostRequest).get

      status(response2) mustBe BAD_REQUEST
      contentAsString(response2) mustBe "Cannot add user - duplicate email"
    }

  }

}
