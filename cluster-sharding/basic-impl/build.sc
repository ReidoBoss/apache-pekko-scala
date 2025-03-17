import mill.*
import $ivy.`com.lihaoyi::mill-contrib-playlib:`
import mill.playlib.*

object scalareactchatapp extends RootModule with PlayModule {

  def scalaVersion = "3.3.4"
  def playVersion  = "3.0.6"
  def twirlVersion = "2.0.1"

  object test extends PlayTests
}
