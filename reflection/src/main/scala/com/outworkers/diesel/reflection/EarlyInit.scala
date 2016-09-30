package com.outworkers.diesel.reflection

import scala.reflect.runtime.universe.TypeTag
import scala.reflect.runtime.{currentMirror => cm, universe => ru}

private object Lock

trait EarlyInit[T] {

  private[this] val instanceMirror = cm.reflect(this)

  def initialize()(implicit typeTag: TypeTag[T]): Seq[T] = {
    Lock.synchronized {
      val selfType = instanceMirror.symbol.toType

      val members: Seq[ru.Symbol] = (for {
        baseClass <- selfType.baseClasses.reverse
        symbol <- baseClass.typeSignature.members.sorted
        if symbol.typeSignature <:< ru.typeOf[T]
      } yield symbol)(collection.breakOut)

      for {
        symbol <- members.distinct
        field = if (symbol.isModule) {
          instanceMirror.reflectModule(symbol.asModule).instance
        } else if (symbol.isTerm && symbol.asTerm.isVal) {
          instanceMirror.reflectField(symbol.asTerm).get
        }
      } yield field.asInstanceOf[T]
    }
  }
}
