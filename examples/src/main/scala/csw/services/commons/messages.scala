package csw.services.commons

import csw.messages.RunningMessage.DomainMessage

//#message-hierarchy
sealed trait TopLevelActorDomainMessage        extends DomainMessage
case class TopLevelActorStatistics(value: Int) extends TopLevelActorDomainMessage
//#message-hierarchy
