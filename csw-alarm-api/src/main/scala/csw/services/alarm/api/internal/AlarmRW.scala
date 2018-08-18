package csw.services.alarm.api.internal

import csw.messages.params.models.Subsystem
import csw.services.alarm.api.models.Key.{AlarmKey, ComponentKey, SubsystemKey}
import csw.services.alarm.api.models._
import upickle.default.{readwriter, ReadWriter => RW, _}

private[alarm] trait AlarmRW {
  implicit val keyRW: RW[Key] = macroRW

  implicit val alarmKeyRW: RW[AlarmKey]         = macroRW
  implicit val componentKeyRW: RW[ComponentKey] = macroRW
  implicit val subsystemKeyRW: RW[SubsystemKey] = macroRW

  implicit val metadataKeyRW: RW[MetadataKey] = readwriter[String].bimap(_.value, MetadataKey.apply)
  implicit val statusKeyRW: RW[StatusKey]     = readwriter[String].bimap(_.value, StatusKey.apply)
  implicit val severityKeyRW: RW[SeverityKey] = readwriter[String].bimap(_.value, SeverityKey.apply)
  implicit val alarmTimeRW: RW[AlarmTime]     = readwriter[String].bimap(_.value, AlarmTime.apply)

  implicit val alarmStatusRW: RW[AlarmStatus]                     = macroRW
  implicit val subsystemRW: RW[Subsystem]                         = EnumUpickleSupport.enumFormat
  implicit val alarmSeverityRW: RW[FullAlarmSeverity]             = EnumUpickleSupport.enumFormat
  implicit val acknowledgementStatusRW: RW[AcknowledgementStatus] = EnumUpickleSupport.enumFormat
  implicit val activationStatusRW: RW[ActivationStatus]           = EnumUpickleSupport.enumFormat
  implicit val alarmTypeRW: RW[AlarmType]                         = EnumUpickleSupport.enumFormat
  implicit val latchStatusRW: RW[LatchStatus]                     = EnumUpickleSupport.enumFormat
  implicit val shelveStatusRW: RW[ShelveStatus]                   = EnumUpickleSupport.enumFormat
  implicit val alarmHealthRW: RW[AlarmHealth]                     = EnumUpickleSupport.enumFormat
}
