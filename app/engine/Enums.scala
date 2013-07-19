package engine

/**
 * ItemRelationType: Creator, Purchaser
 * @param value
 */
abstract sealed case class ItemRelationType(value: Int)

object Creator extends ItemRelationType(1)
object Purchaser extends ItemRelationType(2)

object ItemRelationType {

  def fromInt(value: Int) : ItemRelationType = {
    if (value == 1) Creator
    else if (value == 2) Purchaser
    else Creator
  }

}

/**
 * AWSFolder: Items, Temp
 * @param value
 */
abstract sealed case class AWSFolder(value: String)

object ItemsFolder extends AWSFolder("items")
object TempFolder extends AWSFolder("tmp")

object AWSFolder{

  def fromString(value: String) : AWSFolder = {
    value match {
      case "items" => ItemsFolder
      case _ => ItemsFolder
    }
  }

}

