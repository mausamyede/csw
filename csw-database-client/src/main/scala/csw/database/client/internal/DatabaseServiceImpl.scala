package csw.database.client.internal

import java.sql.{Connection, DatabaseMetaData, ResultSet, Statement}

import csw.database.api.scaladsl.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class DatabaseServiceImpl(connectionF: Future[Connection])(implicit ec: ExecutionContext) extends DatabaseService {
  private val statementF: Future[Statement] = connectionF.map(_.createStatement())

  override def execute(sql: String): Future[Unit]              = statementF.map(_.execute(sql))
  override def executeQuery(sql: String): Future[ResultSet]    = statementF.map(_.executeQuery(sql))
  override def getConnectionMetaData: Future[DatabaseMetaData] = connectionF.map(_.getMetaData)
  override def closeConnection(): Future[Unit]                 = connectionF.map(_.close())
}
