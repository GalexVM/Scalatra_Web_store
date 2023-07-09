import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.ExecutionContext

import scala.util.Success

import scala.util.Failure



trait SlickRoutes extends ScalatraBase with FutureSupport {

  def db: Database
  import Tables._

  get("/init"){
    db.run(Tables.crearTablas).onComplete {
      case Success(_) =>
      // Aquí puedes realizar la inserción de los valores iniciales
      db.run(Tables.insertarValoresInicialesMarcaYCategoria).onComplete {
        case Success(_) =>
          println("Insercion exitosa")
        case Failure(error) =>
          println(s"Error al insertar: ${error.getMessage}")
      }(scala.concurrent.ExecutionContext.Implicits.global)
    case Failure(error) =>
      println(s"Error al crear las tablas: ${error.getMessage}")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }

 
get("/categorias") {
  val tablaCat = db.run(Tables.categorías.result)
  tablaCat.map { xs =>
    response.setContentType("text/plain") // Establecer el tipo de contenido de manera explícita
    xs.map { case (s1, s2) => f"$s1 | $s2 |" }.mkString("\n")
  }(scala.concurrent.ExecutionContext.Implicits.global)
}

get("/marcas") {
  val tablaMarca = db.run(Tables.marcas.result)
  tablaMarca.map { xs =>
    response.setContentType("text/plain") // Establecer el tipo de contenido de manera explícita
    xs.map { case (s1, s2, s3, s4, s5) => f"|$s1|$s2|$s3|$s4|$s5|" }.mkString("\n")
  }(scala.concurrent.ExecutionContext.Implicits.global)
}

  
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

  post("/form"){
    val nombre = params("nombre")
    val marca = params("marca")
    val categoría = params("categoría")
    val precio = params("precio").toDouble
    val descripción = params("descripción")
    val cantidad_restante = params("cantidad_restante").toInt

    val prod = Producto(None, nombre, marca, categoría, precio, descripción, cantidad_restante)

    val insertAction = (productos.map(p => (p.nombre, p.marca, p.categoría, p.precio, p.descripción, p.cantidad_restante))
                        returning productos.map(_.id)) +=
                        (prod.nombre, prod.marca, prod.categoría, prod.precio, prod.descripción, prod.cantidad_restante)

    val insertFuture = db.run(insertAction)

    insertFuture.map{productId =>
      redirect("/success")
    }(scala.concurrent.ExecutionContext.Implicits.global)

  }

  post("/login"){
    val user = params("username")
    val password = params("password")
  }


}

case class Producto(id: Option[Int], nombre: String, marca:String, categoría:String,
                precio:Double, descripción:String, cantidad_restante:Int)


class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes {


  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global


  get("/") {
    views.html.index.render()
  }
  get("/pages/:name"){
    contentType = "text/html"
    params("name") match{
      case "bacon-ipsum" => views.html.bacon_ipsum.render()
      case "veggie-ipsum" => views.html.veggie_ipsum.render()
      case other => halt(404,"not found")
    }
  }
  get("/addproduct"){
    views.html.add_product.render();
  }
  

}





