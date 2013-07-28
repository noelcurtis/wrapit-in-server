package apiv1.unit

import org.specs2.mutable._

import play.api.libs.json.{JsResult, Json}
import models.{GiftList, User}

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

  "Gift List" should {

    "Be able to be Serialized to JSON" in {

      val giftListJson = Json.obj(
        "name" -> "Something"
      )

      val res: JsResult[GiftList] = giftListJson.validate[GiftList]
      res.fold(
        valid = { c => c.name shouldEqual(Some("Something")) },
        invalid = { e => println( e ); failure("Could not parse GiftList from JSON") }
      )

    }

  }

}
