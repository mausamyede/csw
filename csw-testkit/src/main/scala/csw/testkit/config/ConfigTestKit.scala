package csw.testkit.config

import java.util.Optional

import akka.Done
import akka.http.scaladsl.Http
import akka.util.Timeout
import com.typesafe.config.Config
import csw.config.server.ServerWiring
import csw.testkit.TestKitSettings
import csw.testkit.internal.TestKitUtils

import scala.compat.java8.OptionConverters.RichOptionalGeneric

final class ConfigTestKit private (
    configOpt: Option[Config],
    serverPortOpt: Option[Int],
    settingsOpt: Option[TestKitSettings]
) {

  private val configWiring = (configOpt, serverPortOpt) match {
    case (Some(config), _) ⇒ ServerWiring.make(config)
    case (_, serverPort)   ⇒ ServerWiring.make(serverPort)
  }
  import configWiring.actorRuntime._

  implicit def testKitSettings: TestKitSettings = settingsOpt.getOrElse(TestKitSettings(actorSystem))
  implicit val timeout: Timeout                 = testKitSettings.DefaultTimeout

  /**
   * Start HTTP Config server on provided port in constructor or configuration
   *
   * If your test's requires accessing/creating configurations from configuration service, then using this method you can start configuration service.
   * Configuration service can be accessed via [[csw.config.api.scaladsl.ConfigClientService]] or [[csw.config.api.scaladsl.ConfigService]]
   * which can be created via [[csw.config.client.scaladsl.ConfigClientFactory]]
   *
   */
  def startConfigServer(): Http.ServerBinding = TestKitUtils.await(configWiring.httpService.registeredLazyBinding _, timeout)._1

  /**
   * Shutdown HTTP Config server
   *
   * When the test has completed, make sure you shutdown config server.
   */
  def shutdownConfigServer: Done = TestKitUtils.coordShutdown(shutdown, timeout)

}

object ConfigTestKit {

  /**
   * Create a ConfigTestKit
   *
   * When the test has completed you should shutdown the config server
   * with [[ConfigTestKit#shutdownConfigServer]].
   *
   * @return handle to ConfigTestKit which can be used to start and stop config server
   */
  def apply(): ConfigTestKit = new ConfigTestKit(None, None, None)

  /**
   * Scala API for creating ConfigTestKit
   *
   * @param config custom configuration with which to start config server
   * @param testKitSettings custom testKitSettings
   * @return handle to ConfigTestKit which can be used to start and stop config server
   */
  def apply(config: Config, testKitSettings: Option[TestKitSettings]): ConfigTestKit =
    new ConfigTestKit(Some(config), None, testKitSettings)

  /**
   * Scala API for creating ConfigTestKit
   *
   * @param serverPort port on which HTTP config server to be started
   * @param testKitSettings custom testKitSettings
   * @return handle to ConfigTestKit which can be used to start and stop config server
   */
  def apply(serverPort: Int, testKitSettings: Option[TestKitSettings]): ConfigTestKit =
    new ConfigTestKit(None, Some(serverPort), testKitSettings)

  /**
   * Java API for creating ConfigTestKit
   *
   * @param config custom configuration with which to start config server
   * @param testKitSettings custom testKitSettings
   * @return handle to ConfigTestKit which can be used to start and stop config server
   */
  def create(config: Config, testKitSettings: Optional[TestKitSettings]): ConfigTestKit =
    apply(config, testKitSettings.asScala)

  /**
   * Java API for creating ConfigTestKit
   *
   * @param serverPort port on which akka cluster to be started (backend of config service)
   * @param testKitSettings custom testKitSettings
   * @return handle to ConfigTestKit which can be used to start and stop config server
   */
  def create(serverPort: Int, testKitSettings: Optional[TestKitSettings]): ConfigTestKit =
    apply(serverPort, testKitSettings.asScala)

}
