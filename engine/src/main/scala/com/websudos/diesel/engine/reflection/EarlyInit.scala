package com.websudos.diesel.engine.reflection

import scala.collection.mutable.{ ArrayBuffer => MutableArrayBuffer }
import scala.reflect.runtime.universe.{ Symbol, TypeTag }
import scala.reflect.runtime.{currentMirror => cm, universe => ru}


trait EarlyInit {

  type Initialized

  implicit def tag: TypeTag[Initialized]

  protected[this] lazy val _collection: MutableArrayBuffer[Initialized] = new MutableArrayBuffer[Initialized]

  val instanceMirror = cm.reflect(this)
  val selfType = instanceMirror.symbol.toType

  // Collect all column definitions starting from base class
  val columnMembers = MutableArrayBuffer.empty[Symbol]
  selfType.baseClasses.reverse.foreach {
    baseClass =>
      val baseClassMembers = baseClass.typeSignature.members.sorted
      val baseClassColumns = baseClassMembers.filter(_.typeSignature <:< ru.typeOf[Initialized])
      baseClassColumns.foreach(symbol => if (!columnMembers.contains(symbol)) columnMembers += symbol)
  }

  columnMembers.foreach {
    symbol =>
      val table =  if (symbol.isModule) {
        instanceMirror.reflectModule(symbol.asModule).instance
      } else {
        instanceMirror.reflectModule(symbol.asModule).symbol
      }
      _collection += table.asInstanceOf[Initialized]
  }
}
