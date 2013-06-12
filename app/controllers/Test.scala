package controllers

import play.api.mvc.{Action, Controller}
import models.{GiftListRole, User, GiftList}
import anorm.NotAssigned
import java.util.Date
import play.Logger
import engine.ImageGetter


object Test extends Controller {

  def testroles = Action {
    GiftListRole.find(56)
    Ok
  }

  def testimages = Action {
    //val url = "http://www.amazon.com/gp/product/B0083PWAPW/ref=gw_c1_fdt?ie=UTF8&nav_sdd=aps&pf_rd_m=ATVPDKIKX0DER&pf_rd_s=center-1&pf_rd_r=01JSJ4GNAFSY1VSED3ND&pf_rd_t=101&pf_rd_p=1561391222&pf_rd_i=507846"
    //val url = "http://uncrate.com/stuff/shovel/"
    val url = "http://www.bestbuy.com/site/Samsung+-+Ultrabook+13.3%26%2334%3B+Geek+Squad+Certified+Refurbished+Touch-Screen+Laptop+-+4GB+Memory/8815915.p?id=1218902578275&skuId=8815915"
    val images = ImageGetter.getImages(url)
    Ok(views.html.test.testimages(images))
  }

}
