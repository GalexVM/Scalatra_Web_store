import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.ExecutionContext



trait SlickRoutes extends ScalatraBase with FutureSupport {

  def db: Database

  get("/db/create-tables") {
    db.run(Tables.createSchemaAction)
  }

  get("/db/load-data") {
    db.run(Tables.insertSupplierAndCoffeeData)
  }

  get("/db/drop-tables") {
    db.run(Tables.dropSchemaAction)
  }

  get("/coffees") {
  // run the action and map the result to something more readable
  val resultFuture = db.run(Tables.findCoffeesWithSuppliers.result)

  // Convert the Future to a Scalatra action result
  resultFuture.map { xs =>
    contentType = "text/plain"
    xs.map { case (s1, s2) => f"  $s1 supplied by $s2" }.mkString("\n")
  }(scala.concurrent.ExecutionContext.Implicits.global)
}


}


class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes {

   protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

    get("/") {
    <html>
      <body>
        <h1>Hello World</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
        Go to <a href="pages/bacon-ipsum">bacon page</a>.
        Go to <a href="pages/veggie-ipsum">veggie page</a>.
      </body>
    </html>
    
  }
  get("/hello-scalate"){
    <p>Hello, World!</p>
  }
  get("/pages/:name"){
    contentType = "text/html"
    params("name") match{
      case "bacon-ipsum" => views.html.bacon_ipsum.render()
      case "veggie-ipsum" => views.html.veggie_ipsum.render()
      case other => halt(404,"not found")
    }
  }
}





