package com.websudos.diesel.reflection

import com.websudos.diesel.engine.reflection.EarlyInit
import org.scalatest.{FlatSpec, Matchers}

import scala.reflect.runtime.{ currentMirror => cm }

class ColumnHolder[V] {
  lazy val name: String = {
    cm.reflect(this).symbol.name.toTypeName.decodedName.toString
  }
}

class TypeHolder[T <: TypeHolder[T, R], R] extends EarlyInit[ColumnHolder[_]]

class Holder extends TypeHolder[Holder, String] {
  object test extends ColumnHolder[String]
  object test2 extends ColumnHolder[Int]
}

class Holder2 extends TypeHolder[Holder2, String] {
  object test extends ColumnHolder[String]
  object test2 extends ColumnHolder[Int]
  object test3 extends ColumnHolder[String]
  object test4 extends ColumnHolder[Int]
  object test5 extends ColumnHolder[String]
}

class ReflectionTests extends FlatSpec with Matchers {

  it should "initialise the members of a companion object and emit a TypeTag automatically" in {
    val holder = new Holder
    val initList = holder.initialize()

    initList.size shouldEqual 2
  }

  it should "initialise all five members of the companion object using the emitted typetag" in {
    val holder = new Holder2
    val initList = holder.initialize()

    initList.size shouldEqual 5
  }


  it should "capture the names as they appear written by the user using the TypeTag" in {
    val holder = new Holder
    val initList = holder.initialize()

    initList.map(_.name).toList shouldEqual List("test", "test2")
  }

  it should "capture the names as they appear written by the user using the TypeTag from the larger object" in {
    val holder = new Holder2
    val initList = holder.initialize()

    initList.map(_.name).toList shouldEqual List("test", "test2", "test3", "test4", "test5")
  }


}
