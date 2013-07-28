package apiv1.integration

import org.specs2.mutable._

import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.ws.WS

class GiftListSpec extends Specification{

  val endpoint = "http://localhost:"+ testServerPort +"/apiv1/"

  val giftList1 = Json.obj(
    "name" -> "Some List for Bob",
    "dueDate" -> 1378351031636L
  )

  "API GiftList" should {

    "Allows to show Lists for a User" in new WithCleanDb {

      val request = WS.url(endpoint + "lists").get()

      val result = await(request)
      result.status shouldEqual(OK)

      val roles = Json.parse(result.getAHCResponse.getResponseBody)
      play.Logger.debug(roles.toString);
    }


    "Allows to create Lists for a User" in new WithCleanDb {

      val request = WS.url(endpoint + "lists").withHeaders(("Auth-Token", "a")).post(giftList1)

      val result = await(request)
      result.status shouldEqual(CREATED)

      val createdList = Json.parse(result.getAHCResponse.getResponseBody)
      (createdList \ "role").as[Int] shouldEqual(1)

    }

  }

}
