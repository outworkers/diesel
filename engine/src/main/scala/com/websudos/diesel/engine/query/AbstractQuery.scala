package com.websudos.diesel.engine.query

abstract class AbstractQuery[QT <: AbstractQuery](queryString: String) {

  def create(st: String): QT

  def copy: QT = create(queryString)

  def nonEmpty: Boolean = queryString.nonEmpty

  def append(st: String): QT = create(queryString + st)
  def append(st: AbstractQuery): QT = append(st.queryString)
  def append[T](list: T, sep: String = ", ")(implicit ev1: T => TraversableOnce[String]): QT = {
    create(queryString + list.mkString(sep))
  }

  def appendEscape(st: String): QT = append(escape(st))
  def appendEscape(st: AbstractQuery): QT = appendEscape(st.queryString)

  def terminate(): AbstractQuery = appendIfAbsent(";")

  def appendSingleQuote(st: String): QT = append(singleQuote(st))
  def appendSingleQuote(st: AbstractQuery): QT = append(singleQuote(st.queryString))

  def appendIfAbsent(st: String): QT = if (queryString.endsWith(st)) create(queryString) else append(st)
  def appendIfAbsent(st: AbstractQuery): QT = appendIfAbsent(st.queryString)

  def prepend(st: String): QT = create(st + queryString)
  def prepend(st: AbstractQuery): QT = prepend(st.queryString)

  def prependIfAbsent(st: String): QT = if (queryString.startsWith(st)) create(queryString) else prepend(st)
  def prependIfAbsent(st: AbstractQuery): QT = prependIfAbsent(st.queryString)

  def escape(st: String): String = "`" + st + "`"
  def singleQuote(st: String): String = "'" + st.replaceAll("'", "''") + "'"

  def spaced: Boolean = queryString.endsWith(" ")
  def pad: QT = if (spaced) copy else create(queryString + " ")
  def bpad: QT = prependIfAbsent(" ")

  def forcePad: AbstractQuery = create(queryString + " ")
  def trim: AbstractQuery = create(queryString.trim)

  def wrapn(str: String): AbstractQuery = append(CQLSyntax.`(`).append(str).append(CQLSyntax.`)`)
  def wrapn(query: AbstractQuery): AbstractQuery = wrapn(query.queryString)
  def wrap(str: String): AbstractQuery = pad.append(CQLSyntax.`(`).append(str).append(CQLSyntax.`)`)
  def wrap(query: AbstractQuery): AbstractQuery = wrap(query.queryString)

  def wrapn[T](list: T)(implicit ev1: T => TraversableOnce[String]): AbstractQuery = wrapn(list.mkString(", "))
  def wrap[T](list: T)(implicit ev1: T => TraversableOnce[String]): AbstractQuery = wrap(list.mkString(", "))
  def wrapEscape(list: List[String]): AbstractQuery = wrap(list.map(escape).mkString(", "))

}

object AbstractQuery {
  def empty: AbstractQuery = AbstractQuery("")

  def escape(str: String): String = "'" + str.replaceAll("'", "''") + "'"

  def apply(collection: TraversableOnce[String]): AbstractQuery = AbstractQuery(collection.mkString(", "))
}
