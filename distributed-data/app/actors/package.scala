import scala.concurrent.duration.*
import scala.collection.Seq

import play.api.libs.json.*

import org.apache.pekko.util.Timeout

package object actors {
  given DEFAULT_TIMEOUT: Timeout = 30.seconds

  object util {
    def validationErrorsJson(
        error: Seq[(JsPath, Seq[JsonValidationError])]
    ): JsValue = Json.obj(
      "errors" -> error.map(e =>
        Json.obj(
          e._1.toString() -> e._2.map(_.message)
        )
      )
    )
  }
}
