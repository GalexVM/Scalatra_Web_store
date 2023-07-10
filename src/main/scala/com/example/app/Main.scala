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
      redirect("/")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }

  post("/registerform"){
    val usuario = params("usuario")
    val contrasena = params("contrasena")
    val contrasena2  = params("contrasena2")
    if(contrasena != contrasena2){
      redirect("/register")
    }
    //Ya funciona, en el html habia puesto type=post en lugar de method=post xd

    val log = Login(None, usuario, contrasena, false)
    val insertAction = (logins.map(login=>(login.username,login.password,login.isAdmin))
                        returning logins.map(_.id))+=
                        (log.username,log.password,log.isAdmin)
    val insertFuture = db.run(insertAction)
    insertFuture.map{logId =>
      println("registro exitoso")
      redirect("/")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }
}

case class Producto(id: Option[Int], nombre: String, marca:String, categoría:String,
                precio:Double, descripción:String, cantidad_restante:Int)

case class Login(id: Option[Int], username:String, password:String, isAdmin:Boolean)


class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes {

  import Tables._
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
  private var tablesCreated = false;
  private var loginState = 0; //0 = not logged in, 1 = admin, 2 = user

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
    if(loginState == 0){
      views.html.login.render()

    }else if(loginState == 1){
      views.html.admin.render()

    }else if(loginState == 2){
      views.html.client.render()
    }
    
  }

  post("/login-form"){
    val usuario = params("usuario")
    val contrasena = params("contrasena")
    val query = logins.filter(login => login.username === usuario && login.password === contrasena)
    val result = db.run(query.result.headOption)
    result.map {
    case Some((id, _, _,true)) =>
      loginState = 1
      redirect("/")
    case Some((id, _, _,false)) =>
      loginState = 2
      redirect("/")
    case None =>
      redirect("/")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }
  get("/register"){
    views.html.register.render()
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
    val opciones: Future[Seq[String]] = db.run(Tables.categorías.map(_.nombre).result)
    val categorías: Seq[String] = Await.result(opciones,2.seconds)

    val opciones2: Future[Seq[String]] = db.run(Tables.marcas.map(_.nombre).result)
    val marcas: Seq[String] = Await.result(opciones2,2.seconds)
    Ok(views.html.add_product(categorías, marcas))
  }
  
}
