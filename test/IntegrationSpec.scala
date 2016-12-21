import java.util.UUID

import model.User
import org.scalatestplus.play._
import play.api.libs.json.{JsResult, Json}
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

/**
  * add your integration spec here.
  * An integration test will fire up a whole play application in a real (or headless) browser
  */
class IntegrationSpec extends PlaySpec with OneServerPerTest with OneBrowserPerTest with HtmlUnitFactory {

  "Application" should {

    "work from within a browser" in {

      go to ("http://localhost:" + port)

      pageSource must include("Your new application is ready.")
    }

    "Register a user" in {
      val uniqueEmail = UUID.randomUUID().toString + "@uuid.com"

      val newUserAsJson = Json.parse(
        s"""
           |{
           |"firstName": "Dave",
           |"email": "$uniqueEmail"
           |}
          """.stripMargin)

      val futureStatus = wsUrl("/users")
        .post(newUserAsJson)
        .map { response => response.status }

      val status = Await.result(futureStatus, 1.second)

      status mustEqual NO_CONTENT

      val futureUser = wsUrl(s"/users?email=$uniqueEmail")
        .get
          .map { response => response.json.validate[User] }

      val user = Await.result(futureUser, 1.second).get

      user must matchPattern { case User(_, "Dave", email) if email == uniqueEmail => }
    }
  }
}
