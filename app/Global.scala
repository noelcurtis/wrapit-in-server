import play.api._

import models._
import anorm._
import java.util.Date
import play.api.db.DB
import play.Logger
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.debug("Application start")
    if (play.Play.isDev) // seed data in dev mode
    {
      InitialData.cleanDb
      InitialData.insert
    }
  }

  override def onStop(app: Application) {

  }

}

/**
 * Initial set of data to be imported
 * in the sample application.
 */
object InitialData {

  def insert = {
    Logger.debug("Creating test data.");
    // create some users
    val foobar = User.create(User(NotAssigned, Some("foobar@gmail.com"), Some("foobar"), Some(new Date())))
    val foobar1 = User.create(User(NotAssigned, Some("foobar1@gmail.com"), Some("foobar1"), Some(new Date())))

    // create some gift lists for users
    val foobarList = GiftList(NotAssigned, Some("foobar"), Some("by foobar"), Some(new Date()))
    val foobar1List = GiftList(NotAssigned, Some("foobar1"), Some("by foobar1"), Some(new Date()))

    GiftList.create(foobarList, foobar.get.id.get)
    GiftList.create(foobar1List, foobar1.get.id.get)
  }

  def cleanDb = {
    DB.withConnection {
      implicit connection => {
        Logger.debug("Clearing database.");
        SQL("truncate gift_list_role, users, gift_list").execute()
      }
    }
  }

}