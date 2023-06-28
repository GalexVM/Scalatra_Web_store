import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure
import org.scalatra.Ok
import scala.concurrent.Await
import scala.concurrent.duration._
import play.twirl.api.HtmlFormat


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
  val tablaCat = db.run(Tables.categorias.result)
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

get("/productos") {
  val tablaProd = db.run(Tables.productos.result)
  tablaProd.map { xs =>
    response.setContentType("text/plain") // Establecer el tipo de contenido de manera explícita
    xs.map { case (s1, s2, s3, s4, s5, s6, s7) => f"|$s1|$s2|$s3|$s4|$s5|$s6|$s7|" }.mkString("\n")
  }(scala.concurrent.ExecutionContext.Implicits.global)
}

  post("/form"){
    val nombre = params("nombre")
    val marca = params("marca")
    val categoria = params("categoria")
    val precio = params("precio").toDouble
    val descripción = params("descripción")
    val cantidad_restante = params("cantidad_restante").toInt

    val prod = Producto(None, nombre, marca, categoria, precio, descripción, cantidad_restante)

    val insertAction = (productos.map(p => (p.nombre, p.marca, p.categoria, p.precio, p.descripción, p.cantidad_restante))
                        returning productos.map(_.id)) +=
                        (prod.nombre, prod.marca, prod.categoria, prod.precio, prod.descripción, prod.cantidad_restante)

    val insertFuture = db.run(insertAction)

    insertFuture.map{productId =>
      redirect("/")
    }(scala.concurrent.ExecutionContext.Implicits.global)

  }

  


}

case class Producto(id: Option[Int], nombre: String, marca:String, categoria:String,
                precio:Double, descripción:String, cantidad_restante:Int)


class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes {

  import Tables._
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
  private var tablesCreated = false;

  /*def add_product(categorias: Seq[String])(implicit assetsFinder: AssetsFinder): HtmlFormat.Appendable = {
    implicit val assetsFinder: AssetsFinder = assetsFinder

    val html = views.html.add_product(categorias)
    html
  }*/

  get("/") {
    
    if(!tablesCreated){
        db.run(Tables.crearTablas).onComplete {
        case Success(_) =>
        tablesCreated = true;
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
    val opciones: Future[Seq[String]] = db.run(Tables.categorias.map(_.nombre).result)
    val categorias: Seq[String] = Await.result(opciones,2.seconds)
    Ok(views.html.add_product(categorias))
  }
  
  

}





