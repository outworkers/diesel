package com.websudos.diesel.engine.query

import com.websudos.diesel.engine.syntax.DefaultSyntax

abstract class AbstractQuery[QT <: AbstractQuery[QT]](val queryString: String) {

  def create(st: String): QT

  def copy: QT = create(queryString)

  def nonEmpty: Boolean = queryString.nonEmpty

  def append(st: String): QT = create(queryString + st)
  def append(st: AbstractQuery[QT]): QT = append(st.queryString)
  def append[T](list: T, sep: String = ", ")(implicit ev1: T => TraversableOnce[String]): QT = {
    create(queryString + list.mkString(sep))
  }

  def appendEscape(st: String): QT = append(escape(st))
  def appendEscape(st: AbstractQuery[QT]): QT = appendEscape(st.queryString)

  def terminate(): QT = appendIfAbsent(";")

  def appendSingleQuote(st: String): QT = append(singleQuote(st))
  def appendSingleQuote(st: AbstractQuery[QT]): QT = append(singleQuote(st.queryString))

  def appendIfAbsent(st: String): QT = if (queryString.endsWith(st)) create(queryString) else append(st)
  def appendIfAbsent(st: AbstractQuery[QT]): QT = appendIfAbsent(st.queryString)

  def prepend(st: String): QT = create(st + queryString)
  def prepend(st: AbstractQuery[QT]): QT = prepend(st.queryString)

  def prependIfAbsent(st: String): QT = if (queryString.startsWith(st)) create(queryString) else prepend(st)
  def prependIfAbsent(st: AbstractQuery[QT]): QT = prependIfAbsent(st.queryString)

  def escape(st: String): String = "`" + st + "`"
  def singleQuote(st: String): String = "'" + st.replaceAll("'", "''") + "'"

  def spaced: Boolean = queryString.endsWith(" ")
  def pad: QT = if (spaced) copy else create(queryString + " ")
  def bpad: QT = prependIfAbsent(" ")

  def forcePad: QT = create(queryString + " ")
  def trim: QT = create(queryString.trim)

  def wrapn(str: String): QT = append(DefaultSyntax.Symbols.`(`).append(str).append(DefaultSyntax.Symbols.`)`)
  def wrapn(query: AbstractQuery[QT]): QT = wrapn(query.queryString)
  def wrap(str: String): QT = pad.append(DefaultSyntax.Symbols.`(`).append(str).append(DefaultSyntax.Symbols.`)`)
  def wrap(query: AbstractQuery[QT]): QT = wrap(query.queryString)

  def wrapn[T](list: T)(implicit ev1: T => TraversableOnce[String]): QT = wrapn(list.mkString(", "))
  def wrap[T](list: T)(implicit ev1: T => TraversableOnce[String]): QT = wrap(list.mkString(", "))
  def wrapEscape(list: List[String]): QT = wrap(list.map(escape).mkString(", "))

}