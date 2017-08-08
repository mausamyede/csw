package csw.param.models

import csw.param.generics.{Parameter, ParameterSetType}
import spray.json.JsonFormat

import scala.annotation.varargs
import scala.collection.JavaConverters.iterableAsScalaIterableConverter

object Struct {
  import spray.json.DefaultJsonProtocol._
  implicit val format: JsonFormat[Struct] = jsonFormat1(Struct.apply)
}

case class Struct(paramSet: Set[Parameter[_]] = Set.empty[Parameter[_]]) extends ParameterSetType[Struct] {

  /**
   * This is here for Java to construct with String
   */
  def this() = this(Set.empty[Parameter[_]])

  override def create(data: Set[Parameter[_]]) = Struct(data)

  def dataToString1 = paramSet.mkString(", ")

  override def toString = dataToString1
}

object JStruct {

  @varargs
  def create(data: Parameter[_]*): Struct = Struct(data.toSet)

  def create(data: java.util.Set[Parameter[_]]): Struct = Struct(data.asScala.toSet)
}
