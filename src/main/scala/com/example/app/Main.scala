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
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalatra._

import scala.collection.mutable
import scala.math.BigDecimal


//import org.scalatra.scalate.ScalateSupport



trait SlickRoutes extends ScalatraBase with FutureSupport {

  def db: Database
  import Tables._


get("/categorias") {
   
  val categoriasFuture: Future[Seq[(Int, String)]] = db.run(Tables.categorías.result)

  categoriasFuture.map { cats =>
    Ok(views.html.categorias(cats))
  }(scala.concurrent.ExecutionContext.Implicits.global)

  /*val tablaCat = db.run(Tables.categorías.result)
  tablaCat.map { xs =>
    response.setContentType("text/plain") // Establecer el tipo de contenido de manera explícita
    xs.map { case (s1, s2) => f"$s1 | $s2 |" }.mkString("\n")
  }(scala.concurrent.ExecutionContext.Implicits.global)*/
}

get("/marcas") {
    val marcasFuture: Future[Seq[(Int, String, String, String, String)]] = db.run(Tables.marcas.result)

  marcasFuture.map { marc =>
    Ok(views.html.marcas(marc))
  }(scala.concurrent.ExecutionContext.Implicits.global)
  /*val tablaMarca = db.run(Tables.marcas.result)
  tablaMarca.map { xs =>
    response.setContentType("text/plain") // Establecer el tipo de contenido de manera explícita
    xs.map { case (s1, s2, s3, s4, s5) => f"|$s1|$s2|$s3|$s4|$s5|" }.mkString("\n")
  }(scala.concurrent.ExecutionContext.Implicits.global)*/
}

get("/productos") {
//   val tablaProd = db.run(Tables.productos.result)
//  tablaProd.map { xs =>
//   response.setContentType("text/html") // Set the content type to HTML
//   val tableRows = xs.map { case (s1, s2, s3, s4, s5, s6, s7) =>
//     <tr>
//       <td>{s1}</td>
//       <td>{s2}</td>
//       <td>{s3}</td>
//       <td>{s4}</td>
//       <td>{s5}</td>
//       <td>{s6}</td>
//       <td>{s7}</td>
//     </tr>
//   }
  
//   val table = <table>
//     <thead>
//       <tr>
//         <th>ID</th>
//         <th>Producto</th>
//         <th>Marca</th>
//         <th>Categoria</th>
//         <th>Tamaño</th>
//         <th>Descripcion</th>
//         <th>Cantidad</th>
//       </tr>
//     </thead>
//     <tbody>
//       {tableRows}
//     </tbody>
//   </table>
  
//   table.toString()
//   }(scala.concurrent.ExecutionContext.Implicits.global)
    /*val productosQuery = Tables.productos.result
    val productosFuture: Future[Seq[(Int, String, String, String, Double, String, Int)]] = db.run(productosQuery)
    val productos: Seq[(Int, String, String, String, Double, String, Int)] = Await.result(productosFuture, 2.seconds)
    //cambiar a vista admin productos
    println(productos)
   Ok(views.html.products(productos))*/

  val opciones: Future[Seq[String]] = db.run(Tables.categorías.map(_.nombre).result)
  val categorías: Seq[String] = Await.result(opciones,2.seconds)

  val productosFuture: Future[Seq[(Int, String, String, String, Double, String, Int)]] = db.run(Tables.productos.result)

  productosFuture.map { productos =>
    Ok(views.html.products(productos, categorías))
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

  post("/del-cont-form"){
    val nombreProducto = params("nombre")
    val deleteAction = productos.filter(_.nombre === nombreProducto).delete
    val deleteFuture = db.run(deleteAction)
    deleteFuture.map{numRowsDeleted=>
      if(numRowsDeleted > 0){
        println("Registro eliminado correctamente")
      }else{
        println("No se encontró el producto: $nombre")
      }
      redirect("/productos")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }

  post("/up-cont-form") {
    /*val nombre = params("nombre")
    val marca = params.get("marca").map(_.trim).filter(_.nonEmpty).getOrElse(Tables.productos.filter(_.nombre === nombre).map(_.marca).result.head)
    val categoría = params.get("categoría").map(_.trim)
    val precio = params.get("precio").map(_.trim).filter(_.nonEmpty).map(_.toDouble).getOrElse(Tables.productos.filter(_.nombre === nombre).map(_.precio).result.head)
    val descripción = params.get("descripción").map(_.trim)
    val cantidad_restante = params.get("cantidad_restante").map(_.trim).filter(_.nonEmpty).map(_.toInt).getOrElse(Tables.productos.filter(_.nombre === nombre).map(_.cantidad_restante).result.head)

    val updateQuery = productos
      .filter(_.nombre === nombre)
      .map(p => (p.marca, p.categoría, p.precio, p.descripción, p.cantidad_restante))
      .update((marca, categoría, precio, descripción, cantidad_restante))

    val updateFuture = db.run(updateQuery)

    updateFuture.map { _ =>
      redirect("/addproduct")
    }(scala.concurrent.ExecutionContext.Implicits.global)*/
  }


  post("/update"){
    val id = params("id").toInt
    val nombre = params("nombre")
    val marca = params("marca")
    val categoría = params("categoría")
    val precio = params("precio").toDouble
    val descripción = params("descripción")
    val cantidad_restante = params("cantidad_restante").toInt
    
    val updateQuery = productos
    .filter(_.id === id)
    .map(p => (p.nombre, p.marca, p.categoría, p.precio, p.descripción, p.cantidad_restante))
    .update((nombre, marca, categoría, precio, descripción, cantidad_restante))

    val updateFuture = db.run(updateQuery)

    updateFuture.map { _ =>
      redirect("/productos")
    }(scala.concurrent.ExecutionContext.Implicits.global)
    
  }

  post("/filtrar"){
    val categoría = params("categoría")
    if(categoría == "Todo")
    {
      redirect("/productos")
    }
    val filterquery = productos.filter(_.categoría === categoría)


    val productosQuery = Tables.productos.filter(_.categoría === categoría).result
    val productosFuture: Future[Seq[(Int, String, String, String, Double, String, Int)]] = db.run(productosQuery)

    val opciones: Future[Seq[String]] = db.run(Tables.categorías.map(_.nombre).result)
    val categorías: Seq[String] = Await.result(opciones,2.seconds)

    productosFuture.map { productos =>
      Ok(views.html.products(productos, categorías))
    }(scala.concurrent.ExecutionContext.Implicits.global)

  }

  post("/filtrar-cliente"){
    val categoría = params("categoría")
    if(categoría == "Todo")
    {
      redirect("/")
    }
    val filterquery = productos.filter(_.categoría === categoría)


    val productosQuery = Tables.productos.filter(_.categoría === categoría).result
    val productosFuture: Future[Seq[(Int, String, String, String, Double, String, Int)]] = db.run(productosQuery)

    val opciones: Future[Seq[String]] = db.run(Tables.categorías.map(_.nombre).result)
    val categorías: Seq[String] = Await.result(opciones,2.seconds)

    productosFuture.map { productos =>
      Ok(views.html.client(productos, categorías))
    }(scala.concurrent.ExecutionContext.Implicits.global)

  }

   get("/carrito") {
    val productosQuery = Tables.carritos.result
    val productosFuture: Future[Seq[(Int, String,  Double)]] = db.run(productosQuery)
    val productos: Seq[(Int, String,  Double)] = Await.result(productosFuture, 2.seconds)

    val sumAction = Tables.carritos.map(_.precio).sum
    val sumFuture: Future[Option[Double]] = db.run(sumAction.result)
    val sumOption: Option[Double] = Await.result(sumFuture, 2.seconds)
    val sum: Double = sumOption.getOrElse(0.0) // Valor predeterminado en caso de que el resultado sea None
    val roundSum = BigDecimal(sum).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

    val seqAction = carritos.map(_.id).result.map(_.toSeq)(ExecutionContext.global)
    val seqFuture = db.run(seqAction)
    val seqResult: Seq[Int] = Await.result(seqFuture, 2.seconds)
    val comma: String = seqResult.mkString(",")


    
      Ok(views.html.carrito(productos, roundSum, comma))
    

    //val seq: Seq[Int] = Await.result(seqFuture, 2.seconds)
  }

  post("/add-carrito") {
    val nombre = params("nombre")
    val precio = params("precio").toDouble

    val carrito = Carrito(None, nombre, precio)
    val insertAction = (Tables.carritos.map(c=>(c.nombre,c.precio))
                        returning Tables.carritos.map(_.id))+=
                        (carrito.nombre,carrito.precio)
    val insertFuture = db.run(insertAction)
    insertFuture.map{carritoId =>
      println("registro exitoso")
      redirect("/carrito")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }

  post("/del-carrito") {
    val nombre = params("nombre")
    val deleteAction = carritos.filter(_.nombre === nombre).delete
    val deleteFuture = db.run(deleteAction)
    deleteFuture.map{numRowsDeleted=>
      if(numRowsDeleted > 0){
        println("Registro eliminado correctamente")
      }else{
        println("No se encontró el producto: $nombre")
      }
      redirect("/carrito")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }

post("/comprar") {
  val sum = params("total").toDouble
  val ids = params("ids")
  Ok(views.html.comprar(sum, ids))
}

post("/pagar") {
  val sum = params("total")
  val ids = params("ids")

  println(ids)
  val seqIds = ids.split(",").map(_.toInt).toSeq

    seqIds.foreach { id =>
    val cantAction = productos.filter(_.id === id).map(p => p.cantidad_restante).result.head
    val cantFuture: Future[Int] = db.run(cantAction)
    val cant: Int = Await.result(cantFuture, 2.seconds)

    val updateAction = productos
      .filter(_.id === id)
      .map(p => p.cantidad_restante)
      .update(cant - 1)
      val updateFuture = db.run(updateAction)

      val deleteAction = carritos.filter(_.id === id).delete
      val deleteFuture = db.run(deleteAction)
      }

      redirect("/")
  }


post("/update-cats"){
    val id = params("id").toInt
    val nombre = params("nombre")

    val updateQuery = categorías
    .filter(_.id === id)
    .map(p => (p.nombre))
    .update((nombre))

    val updateFuture = db.run(updateQuery)

    updateFuture.map { _ =>
      redirect("/categorias")
    }(scala.concurrent.ExecutionContext.Implicits.global)
}

post("/update-marcas"){
    val id = params("id").toInt
    val nombre = params("nombre")
    val dirección = params("dirección")
    val dirección_entrega = params("dirección_entrega")
    val contacto = params("contacto")

    val updateQuery = marcas
    .filter(_.id === id)
    .map(p => (p.nombre, p.dirección, p.dirección_entrega, p.contacto))
    .update((nombre, dirección, dirección_entrega, contacto))

    val updateFuture = db.run(updateQuery)

    updateFuture.map { _ =>
      redirect("/marcas")
    }(scala.concurrent.ExecutionContext.Implicits.global)
}

 post("/del-marcas-form"){
    val nombreMarca = params("nombre")
    val deleteAction = marcas.filter(_.nombre === nombreMarca).delete
    val deleteFuture = db.run(deleteAction)
    deleteFuture.map{numRowsDeleted=>
      if(numRowsDeleted > 0){
        println("Registro eliminado correctamente")
      }else{
        println("No se encontró el producto: $nombre")
      }
      redirect("/marcas")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }

   post("/del-cats-form"){
    val nombreCat = params("nombre")
    val deleteAction = categorías.filter(_.nombre === nombreCat).delete
    val deleteFuture = db.run(deleteAction)
    deleteFuture.map{numRowsDeleted=>
      if(numRowsDeleted > 0){
        println("Registro eliminado correctamente")
      }else{
        println("No se encontró el producto: $nombre")
      }
      redirect("/marcas")
    }(scala.concurrent.ExecutionContext.Implicits.global)
  }

  post("/add-marcas")
  {
    val nombre = params("nombre")
    val dirección = params("dirección")
    val dirección_entrega = params("dirección_entrega")
    val contacto = params("contacto")

    val marca = Marca(None, nombre, dirección, dirección_entrega, contacto)

    val insertAction = (Tables.marcas.map(m=>(m.nombre,m.dirección,m.dirección_entrega,m.contacto))
                        returning Tables.marcas.map(_.id))+=
                        (marca.nombre,marca.dirección,marca.dirección_entrega,marca.contacto)

    val insertFuture = db.run(insertAction)
    insertFuture.map{marcaId =>
      println("registro exitoso")
      redirect("/marcas")
    }(scala.concurrent.ExecutionContext.Implicits.global)             
  }

  post("/add-categ")
  {
    val nombre = params("nombre")


    val categ = Categoria(None, nombre)

    val insertAction = (Tables.categorías.map(m=>(m.nombre))
                        returning Tables.categorías.map(_.id))+=
                        (categ.nombre)

    val insertFuture = db.run(insertAction)
    insertFuture.map{categId =>
      println("registro exitoso")
      redirect("/categorias")
    }(scala.concurrent.ExecutionContext.Implicits.global)             
  }

}

case class Categoria(id:Option[Int], nombre:String)

case class Marca(id:Option[Int], nombre:String, dirección:String, dirección_entrega:String, contacto:String)

case class Producto(id: Option[Int], nombre: String, marca:String, categoría:String,
                precio:Double, descripción:String, cantidad_restante:Int)

case class Login(id: Option[Int], username:String, password:String, isAdmin:Boolean)

case class Carrito(id: Option[Int], nombre:String, precio:Double)

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
      //Vista cliente productos
        val productosQuery = Tables.productos.result
        val productosFuture: Future[Seq[(Int, String, String, String, Double, String, Int)]] = db.run(productosQuery)
        val productos: Seq[(Int, String, String, String, Double, String, Int)] = Await.result(productosFuture,2.seconds)

        val opciones: Future[Seq[String]] = db.run(Tables.categorías.map(_.nombre).result)
        val categorías: Seq[String] = Await.result(opciones,2.seconds)
        
        Ok(views.html.client(productos, categorías))
    }
  }
  post("/logout-client"){
    loginState = 0
    redirect("/")
  }
  post("/logout"){
    loginState = 0
    redirect("/")
  }
    
  
post("/login-form") {
  val usuario = params("usuario")
  val contrasena = params("contrasena")
  val query = logins.filter(login => login.username === usuario && login.password === contrasena)
  val result = db.run(query.result.headOption)
  result.map {
    case Some((id, _, _, true)) =>
      loginState = 1
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
      response.setHeader("Pragma", "no-cache")
      response.setHeader("Expires", "0")
      redirect("/")
    case Some((id, _, _, false)) =>
      loginState = 2
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
      response.setHeader("Pragma", "no-cache")
      response.setHeader("Expires", "0")
      redirect("/")
    case None =>
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
      response.setHeader("Pragma", "no-cache")
      response.setHeader("Expires", "0")
      redirect("/")
  }(scala.concurrent.ExecutionContext.Implicits.global)
}


  get("/addproduct"){
    val opciones: Future[Seq[String]] = db.run(Tables.categorías.map(_.nombre).result)
    val categorías: Seq[String] = Await.result(opciones,2.seconds)

    val opciones2: Future[Seq[String]] = db.run(Tables.marcas.map(_.nombre).result)
    val marcas: Seq[String] = Await.result(opciones2,2.seconds)

    val opciones3: Future[Seq[String]] = db.run(Tables.productos.map(_.nombre).result)
    val productos: Seq[String] = Await.result(opciones3,2.seconds)

    Ok(views.html.add_product(categorías, marcas, productos))
  }
}