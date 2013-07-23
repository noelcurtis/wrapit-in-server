package apiv1.unit

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.{JsResult, JsNull, Json}
import models.User

class ApiModelSpec extends Specification{

  "User Model" should {

    "Be able to be Serialized from JSON" in {

      val userJson = Json.obj(
        "email" -> "foobar@gmail.com",
        "password" -> "hello"
      )

      val res: JsResult[User] = userJson.validate[User]
      res.fold(
        valid = { c => c.email shouldEqual(Some("foobar@gmail.com")) },
        invalid = { e => println( e ); failure("Could not parse User from JSON") }
      )

    }

  }

}
