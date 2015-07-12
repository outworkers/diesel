package com.websudos.diesel.engine.reflection

import com.websudos.phantom.CassandraTable

import scala.collection.mutable.{ ArrayBuffer => MutableArrayBuffer }
import scala.reflect.runtime._

trait EarlyInit {

  abstract type Initialized

  private[this] lazy val _columns: MutableArrayBuffer[Initialized] = new MutableArrayBuffer[Initialized]

  val instanceMirror = cm.reflect(this)
  val selfType = instanceMirror.symbol.toType

  // Collect all column definitions starting from base class
  val columnMembers = MutableArrayBuffer.empty[Symbol]
  selfType.baseClasses.reverse.foreach {
    baseClass =>
      val baseClassMembers = baseClass.typeSignature.members.sorted
      val baseClassColumns = baseClassMembers.filter(_.typeSignature <:< ru.typeOf[Initialized[_, _]])
      baseClassColumns.foreach(symbol => if (!columnMembers.contains(symbol)) columnMembers += symbol)
  }

  columnMembers.foreach {
    symbol =>
      val table =  if (symbol.isModule) {
        instanceMirror.reflectModule(symbol.asModule).instance
      } else {
        instanceMirror.reflectModule(symbol.asModule).symbol
      }
      _tables += table.asInstanceOf[CassandraTable[_, _]]
  }
}

}
