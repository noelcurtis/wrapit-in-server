package apiv1.integration

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.ws.WS
import play.api.libs.json.{JsResult, Json}
import models.User

class UserSpec extends Specification{

  val endpoint = "http://localhost:"+ testServerPort +"/apiv1/"

  val user1 = Json.obj(
    "email" -> "foobarapi@gmail.com",
    "password" -> "foobar"
  )

  "API User" should {

    "Allow to create new User" in new WithServer {

      val request = WS.url(endpoint + "user").post(user1)

      val result = await(request)
      result.status shouldEqual(OK)

      val createdUser = Json.parse(result.getAHCResponse.getResponseBody)

      val res: JsResult[User] = createdUser.validate[User]
      res.fold(
        valid = { c => c.email shouldEqual(Some("foobarapi@gmail.com")) },
        invalid = { e => println( e ); failure("Could not parse User from JSON") }
      )

    }

  }

}
