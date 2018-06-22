package csw.apps.clusterseed.internal

import com.typesafe.config.Config

class Settings(config: Config) {

  private val clusterSeedConfig = config.getConfig("csw-cluster-seed")

  def clusterPort: Int      = clusterSeedConfig.getInt("cluster-port")
  def adminPort: Int        = clusterSeedConfig.getInt("admin-port")
  def httpLocationPort: Int = clusterSeedConfig.getInt("http-location-port")
}
