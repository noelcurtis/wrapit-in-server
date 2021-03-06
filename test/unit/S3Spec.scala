package unit


import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

import org.specs2.mutable.Specification

import fly.play.s3.{BucketFile, S3}
import play.api.test.WithApplication
import play.api.libs.ws.WS

class S3Spec extends Specification {

  val bucketName = "wi-dev"
  val cacheTime = "864000"

  "S3" should {

    "Allow to get an Image and upload to Amazon S3" in new WithApplication {
      val imgResult = Await.result(WS.url("http://g-ecx.images-amazon.com/images/G/01/kindle/dp/2012/KT/KT-slate-01-lg._V395919237_.jpg").get, FiniteDuration(20, "seconds"))
      val bytes = imgResult.getAHCResponse.getResponseBodyAsBytes
      val cType = imgResult.getAHCResponse.getContentType

      val bucket = S3(bucketName)
      val awsUpload = bucket + BucketFile("test/testimage.jpg", cType, bytes, None, Some(Map("Cache-Control" -> s"max-age=$cacheTime, must-revalidate")))
      val value = Await.result(awsUpload, FiniteDuration(10, "seconds"))
      value.fold({ e => failure(e.toString) }, { s => success })
    }
  }

}
