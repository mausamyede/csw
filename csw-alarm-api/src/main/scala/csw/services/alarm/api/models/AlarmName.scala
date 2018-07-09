package csw.services.alarm.api.models

import play.api.libs.json._

/**
 * A wrapper class representing the name of an Alarm e.g. tromboneAxisLowLimitAlarm, tromboneAxisHighLimitAlarm
 */
case class AlarmName(name: String) {
  override def toString: String = name
}

object AlarmName {
  implicit val format: Format[AlarmName] = new Format[AlarmName] {
    override def writes(obj: AlarmName): JsValue           = JsString(obj.name)
    override def reads(json: JsValue): JsResult[AlarmName] = JsSuccess(AlarmName(json.as[String]))
  }
}