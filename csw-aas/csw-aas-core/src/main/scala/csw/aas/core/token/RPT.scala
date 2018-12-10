package csw.aas.core.token
import csw.aas.core.commons.AuthLogger
import org.keycloak.authorization.client.AuthzClient
import pdi.jwt.{JwtJson, JwtOptions}

import scala.concurrent.{blocking, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

private[aas] class RPT(authzClient: AuthzClient)(implicit ec: ExecutionContext) {
  private val logger = AuthLogger.getLogger
  import logger._

  def create(token: String): Future[AccessToken] = {
    debug("fetching RPT from keycloak")

    val tokenF = getAuthorizationResponse(token)
      .flatMap(rptString ⇒ Future.fromTry(decodeRPT(rptString)))

    tokenF.onComplete {
      case Success(at) => debug(s"successfully fetched RPT from keycloak for ${at.userOrClientName}")
      case Failure(e)  => error("error while fetching RPT from keycloak", ex = e)
    }

    tokenF
  }

  private def decodeRPT(rptString: String): Try[AccessToken] =
    JwtJson
      .decodeJson(rptString, JwtOptions(signature = false, expiration = false, notBefore = false))
      .map(_.as[AccessToken])

  private def getAuthorizationResponse(token: String): Future[String] = Future {
    blocking { authzClient.authorization(token).authorize().getToken }
  }
}

object RPT {
  def apply(authzClient: AuthzClient)(implicit ec: ExecutionContext): RPT = new RPT(authzClient)
}