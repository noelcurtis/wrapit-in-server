package models

import anorm.{NotAssigned, Pk}

case class Item(id: Pk[Long] = NotAssigned, name: Option[String], price: Option[Double],
                isPurchased: Option[Boolean])
