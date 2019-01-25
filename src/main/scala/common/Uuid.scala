package common

import java.util.UUID.randomUUID

trait Uuid {
  def uuid(): String = randomUUID().toString
}
