import com._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import scala.util.Success

import scala.util.Failure

import org.scalatra._
import javax.servlet.ServletContext

import slick.jdbc.H2Profile.api._

class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  override def init(context: ServletContext) = {
    val db = Database.forDataSource(cpds, None)   // create the Database object
    implicit val executor: ExecutionContext = global
    //
    
    //
    context.mount(new SlickApp(db) with SlickRoutes, "/*")   // create and mount the Scalatra application
  }

  private def closeDbConnection() = {
    logger.info("Closing c3po connection pool")
    cpds.close
  }

  override def destroy(context: ServletContext) = {
    super.destroy(context)
    closeDbConnection()
  }
}
