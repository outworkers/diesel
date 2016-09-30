package com.outworkers.diesel.engine.query

import com.outworkers.diesel.engine.syntax.DefaultSyntax._

abstract class AbstractQuery[QT <: AbstractQuery[QT]](val queryString: String) {

  def create(st: String): QT

  def copy: QT = create(queryString)

  def nonEmpty: Boolean = queryString.nonEmpty

  def append(st: String): QT = create(queryString + st)
  def append(st: QT): QT = append(st.queryString)
  def append[T](list: T, sep: String = ", ")(implicit ev1: T => TraversableOnce[String]): QT = {
    create(queryString + list.mkString(sep))
  }

  def appendEscape(st: String): QT = append(escape(st))
  def appendEscape(st: QT): QT = appendEscape(st.queryString)

  def terminate(): QT = appendIfAbsent(";")

  def appendSingleQuote(st: String): QT = append(singleQuote(st))
  def appendSingleQuote(st: QT): QT = append(singleQuote(st.queryString))

  def appendIfAbsent(st: String): QT = if (queryString.endsWith(st)) create(queryString) else append(st)
  def appendIfAbsent(st: QT): QT = appendIfAbsent(st.queryString)

  def prepend(st: String): QT = create(st + queryString)
  def prepend(st: QT): QT = prepend(st.queryString)

  def prependIfAbsent(st: String): QT = if (queryString.startsWith(st)) create(queryString) else prepend(st)
  def prependIfAbsent(st: QT): QT = prependIfAbsent(st.queryString)

  def escape(st: String): String = "`" + st + "`"
  def singleQuote(st: String): String = "'" + st.replaceAll("'", "''") + "'"

  def spaced: Boolean = queryString.endsWith(" ")
  def pad: QT = if (spaced) copy else create(queryString + " ")
  def bpad: QT = prependIfAbsent(" ")

  def forcePad: QT = create(queryString + " ")
  def trim: QT = create(queryString.trim)

  def wrapn(str: String): QT = append(Symbols.`(`).append(str).append(Symbols.`)`)
  def wrapn(query: QT): QT = wrapn(query.queryString)
  def wrap(str: String): QT = pad.append(Symbols.`(`).append(str).append(Symbols.`)`)
  def wrap(query: QT): QT = wrap(query.queryString)

  def wrapn[T](list: T)(implicit ev1: T => TraversableOnce[String]): QT = wrapn(list.mkString(", "))
  def wrap[T](list: T)(implicit ev1: T => TraversableOnce[String]): QT = wrap(list.mkString(", "))
  def wrapEscape(list: List[String]): QT = wrap(list.map(escape).mkString(", "))

}