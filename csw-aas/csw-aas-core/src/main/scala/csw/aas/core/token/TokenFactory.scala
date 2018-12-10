package csw.aas.core.token

import csw.aas.core.commons.AuthLogger
import csw.aas.core.deployment.AuthConfig._
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.authorization.client.AuthzClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TokenFactory(keycloakDeployment: KeycloakDeployment)(implicit ec: ExecutionContext) {

  private val logger = AuthLogger.getLogger
  import logger._

  private lazy val authzClient: AuthzClient = AuthzClient.create(keycloakDeployment)

  private lazy val rpt: RPT = RPT(authzClient)

  private[aas] def makeToken(token: String): Future[AccessToken] = {
    val result = rpt.create(token)
    result.onComplete {
      case Failure(e)  => error("token string could not be converted to RPT", ex = e)
      case Success(at) => debug(s"authentication succeeded for ${at.userOrClientName}")
    }
    result
  }
}