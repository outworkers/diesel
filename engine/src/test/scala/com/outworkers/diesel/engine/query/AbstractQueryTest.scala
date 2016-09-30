package com.outworkers.diesel.engine.query

import org.scalatest.{FlatSpec, Matchers}

case class DummyQuery(override val queryString: String) extends AbstractQuery[DummyQuery](queryString) {
  override def create(st: String): DummyQuery = DummyQuery(st)
}

object DummyQuery {
  def empty: DummyQuery = DummyQuery("")
}

class AbstractQueryTest extends FlatSpec with Matchers {

  it should "create an empty CQL query using the empty method on the companion object" in {
    DummyQuery.empty.queryString shouldEqual ""
  }
}
