import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.ExecutionContext


//Database: 
object Tables {

  // Definition of the SUPPLIERS table
  class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
    def id = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
    def name = column[String]("SUP_NAME")
    def street = column[String]("STREET")
    def city = column[String]("CITY")
    def state = column[String]("STATE")
    def zip = column[String]("ZIP")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, street, city, state, zip)
  }

  // Definition of the COFFEES table
  class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)
    def supID = column[Int]("SUP_ID")
    def price = column[Double]("PRICE")
    def sales = column[Int]("SALES")
    def total = column[Int]("TOTAL")
    def * = (name, supID, price, sales, total)

    // A reified foreign key relation that can be navigated to create a join
    def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
  }

  // Table query for the SUPPLIERS table, represents all tuples of that table
  val suppliers = TableQuery[Suppliers]

  // Table query for the COFFEES table
  val coffees = TableQuery[Coffees]

  // Other queries and actions ...
  // Query, implicit inner join coffees and suppliers, return their names
  val findCoffeesWithSuppliers = {
    for {
      c <- coffees
      s <- c.supplier
    } yield (c.name, s.name)
  }
  // DBIO Action which runs several queries inserting sample data
  val insertSupplierAndCoffeeData = DBIO.seq(
    Tables.suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
    Tables.suppliers += (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
    Tables.suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966"),
    Tables.coffees ++= Seq(
      ("Colombian", 101, 7.99, 0, 0),
      ("French_Roast", 49, 8.99, 0, 0),
      ("Espresso", 150, 9.99, 0, 0),
      ("Colombian_Decaf", 101, 8.99, 0, 0),
      ("French_Roast_Decaf", 49, 9.99, 0, 0)
    )
  )
  // DBIO Action which creates the schema
  val createSchemaAction = (suppliers.schema ++ coffees.schema).create

  // DBIO Action which drops the schema
  val dropSchemaAction = (suppliers.schema ++ coffees.schema).drop

  // Create database, composing create schema and insert sample data actions
  val createDatabase = DBIO.seq(createSchemaAction, insertSupplierAndCoffeeData)
}


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
  get("/pages/:slug"){
    contentType = "text/html"
    PageDao.pages find(_.slug == params("slug")) match{
      case Some(page) if page.title == "bacon title" => views.html.bacon_ipsum.render()
      case Some(page) if page.title == "veggie title" => views.html.veggie_ipsum.render()
      case None => halt(404, "not found")
      case other => halt(404, "not found")
    }
  }
}


case class Page(slug:String, title:String, summary:String, body: String)

object PageDao{
  val page1 = Page("bacon-ipsum",
    "bacon title",
    "summary",
    "body"
  )
  val page2 = Page("veggie-ipsum",
    "veggie title",
    "summary bu veggie",
    "body but veggie"
  )
  val pages = List(page1,page2)
}




