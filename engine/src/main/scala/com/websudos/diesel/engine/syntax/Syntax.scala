package com.websudos.diesel.engine.syntax

sealed abstract class Symbols {
  val `*` = "*"
  val `{` = "{"
  val `}` = "}"
  val `[` = "["
  val `]` = "]"

  val `.` = "."
  val `:` = ":"
  val `;` = ";"
  val `(` = "("
  val `)` = ")"
  val `,` = ","
  val `<` = "<"
  val `>` = ">"
  val `=` = "="
  val + = "+"
  val - = "-"
}

class Syntax {
  object Symbols extends Symbols
}

object DefaultSyntax extends Syntax