import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox
import scala.tools.reflect.Eval

object Test extends dotty.runtime.LegacyApp {
  object C {
    type T = Int
    val code = reify {
      List[T](2)
    }
    println(code.eval)
  }

  C
}
