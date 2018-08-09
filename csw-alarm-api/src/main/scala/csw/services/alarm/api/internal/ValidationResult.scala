package csw.services.alarm.api.internal

sealed trait ValidationResult

object ValidationResult {
  case object Success                       extends ValidationResult
  case class Failure(reasons: List[String]) extends ValidationResult
}