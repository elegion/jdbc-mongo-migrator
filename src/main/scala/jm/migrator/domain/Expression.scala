package jm.migrator.domain

import util.matching.Regex

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 2:36 PM

 * String expression with ${placeholders} which could be replaced with actual values
 */
class Expression(val expression: String) {
  def render(params: Map[String, Any]): String = {
    Expression.paramRegex.replaceAllIn(expression, { m: Regex.Match =>
      ///
      params.get(m.group(1)).getOrElse("").toString
    })
  }
}


object Expression {
  val paramRegex = """\$\{(\w+)\}""".r

  def apply(expression: String) = new Expression(expression)
}

