package com.websudos.diesel.engine.reflection

import scala.collection.mutable.{ ArrayBuffer => MutableArrayBuffer }
import scala.reflect.runtime.universe.{ Symbol, TypeTag }
import scala.reflect.runtime.{currentMirror => cm, universe => ru}


trait EarlyInit[T] {

  protected[this] lazy val _collection: MutableArrayBuffer[T] = new MutableArrayBuffer[T]

  def initialize()(implicit typeTag: TypeTag[T]) = {
    val instanceMirror = cm.reflect(this)
    val selfType = instanceMirror.symbol.toType

    // Collect all column definitions starting from base class
    val columnMembers = MutableArrayBuffer.empty[Symbol]
    selfType.baseClasses.reverse.foreach {
      baseClass =>
        val baseClassMembers = baseClass.typeSignature.members.sorted
        val baseClassColumns = baseClassMembers.filter(_.typeSignature <:< ru.typeOf[T])
        baseClassColumns.foreach(symbol => if (!columnMembers.contains(symbol)) columnMembers += symbol)
    }

    columnMembers.foreach {
      symbol =>
        val member =  if (symbol.isModule) {
          instanceMirror.reflectModule(symbol.asModule).instance
        } else {
          instanceMirror.reflectModule(symbol.asModule).symbol
        }
        _collection += member.asInstanceOf[T]
    }
  }
}
