package csw.auth.adapters.akka.http
import akka.http.javadsl.server.{AuthenticationFailedRejection, AuthorizationFailedRejection}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.directives.Credentials.Provided
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.testkit._
import csw.auth.adapters.akka.http.AuthorizationPolicy.RealmRolePolicy
import csw.auth.core.token.AccessToken
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

class RealmRolePolicyTest extends FunSuite with MockitoSugar with Directives with ScalatestRouteTest with Matchers {

  test("realmRole policy should return AuthenticationFailedRejection when token is invalid") {
    val authentication: Authentication = mock[Authentication]
    val securityDirectives             = new SecurityDirectives(authentication)

    val invalidTokenStr    = "invalid"
    val invalidTokenHeader = Authorization(OAuth2BearerToken(invalidTokenStr))

    val authenticator: Authenticator[AccessToken] = {
      case Provided(`invalidTokenStr`) ⇒ None
      case _                           ⇒ None
    }

    when(authentication.authenticator).thenReturn(authenticator)

    val route: Route = securityDirectives.authenticate { implicit at ⇒
      get {
        securityDirectives.authorize(RealmRolePolicy("admin"), at) {
          complete("OK")
        }
      }
    }

    Get("/").addHeader(invalidTokenHeader) ~> route ~> check {
      rejection shouldBe a[AuthenticationFailedRejection]
    }
  }

  test("realmRole policy should return AuthenticationFailedRejection when token is not present") {
    val authentication: Authentication = mock[Authentication]
    val securityDirectives             = new SecurityDirectives(authentication)

    val authenticator: Authenticator[AccessToken] = _ ⇒ None

    when(authentication.authenticator).thenReturn(authenticator)

    val route: Route = securityDirectives.authenticate { implicit at ⇒
      get {
        securityDirectives.authorize(RealmRolePolicy("admin"), at) {
          complete("OK")
        }
      }
    }

    Get("/") ~> route ~> check {
      rejection shouldBe a[AuthenticationFailedRejection]
    }
  }

  test("realmRole policy should return AuthorizationFailedRejection when token does not have realmRole") {
    val authentication: Authentication = mock[Authentication]
    val securityDirectives             = new SecurityDirectives(authentication)

    val validTokenWithoutRealmRoleStr = "validTokenWithoutRealmRoleStr"

    val validTokenWithoutRealmRole = mock[AccessToken]

    val validTokenWithoutRealmRoleHeader = Authorization(OAuth2BearerToken(validTokenWithoutRealmRoleStr))

    when(validTokenWithoutRealmRole.hasRealmRole("admin"))
      .thenReturn(false)

    val authenticator: Authenticator[AccessToken] = {
      case Provided(`validTokenWithoutRealmRoleStr`) ⇒ Some(validTokenWithoutRealmRole)
      case _                                         ⇒ None
    }

    when(authentication.authenticator).thenReturn(authenticator)

    val route: Route = securityDirectives.authenticate { implicit at ⇒
      get {
        securityDirectives.authorize(RealmRolePolicy("admin"), at) {
          complete("OK")
        }
      }
    }

    Get("/").addHeader(validTokenWithoutRealmRoleHeader) ~> route ~> check {
      rejection shouldBe a[AuthorizationFailedRejection]
    }
  }

  test("realmRole policy should return 200 OK when token is valid & has realmRole") {
    val authentication: Authentication = mock[Authentication]
    val securityDirectives             = new SecurityDirectives(authentication)

    val validTokenWithRealmRoleStr    = "validTokenWithRealmRoleStr"
    val validTokenWithRealmRole       = mock[AccessToken]
    val validTokenWithRealmRoleHeader = Authorization(OAuth2BearerToken(validTokenWithRealmRoleStr))
    when(validTokenWithRealmRole.hasRealmRole("admin"))
      .thenReturn(true)

    val authenticator: Authenticator[AccessToken] = {
      case Provided(`validTokenWithRealmRoleStr`) ⇒ Some(validTokenWithRealmRole)
      case _                                      ⇒ None
    }

    when(authentication.authenticator).thenReturn(authenticator)

    val route: Route = securityDirectives.authenticate { implicit at ⇒
      get {
        securityDirectives.authorize(RealmRolePolicy("admin"), at) {
          complete("OK")
        }
      }
    }

    Get("/").addHeader(validTokenWithRealmRoleHeader) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }
  }
}