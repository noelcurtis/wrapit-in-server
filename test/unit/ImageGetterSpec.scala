package unit

import org.specs2.mutable._


import engine.ImageGetter



class ImageGetterSpec extends Specification{

  "Image Getter" should {

    "should be able to create a URL" in {
      val url = "http://www.amazon.com/gp/product/B0083PWAPW/ref=gw_c1_fdt?ie=UTF8&nav_sdd=aps&pf_rd_m=ATVPDKIKX0DER&pf_rd_s=center-1&pf_rd_r=01JSJ4GNAFSY1VSED3ND&pf_rd_t=101&pf_rd_p=1561391222&pf_rd_i=507846"
      val images = ImageGetter.getImages(url)
      assert (images.isEmpty != true)
    }

  }

}
