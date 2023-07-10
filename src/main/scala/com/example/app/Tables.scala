import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.ExecutionContext

//Database: 
object Tables {

  //Productos
  class Producto(tag: Tag) extends Table[(Int,String,String,String,Double,String,Int)](tag,"PRODUCTOS"){
    def id = column[Int]("PROD_ID", O.PrimaryKey, O.AutoInc) //PK
    def nombre = column[String]("PROD_NAME")
    def marca = column[String]("MARCA")//FK
    def categoría = column[String]("CATEGORÍA")//FK
    def precio = column[Double]("PROD_COST")
    def descripción = column[String]("PROD_DESCRIP")
    def cantidad_restante = column[Int]("CANTIDAD_RESTANTE")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, nombre, marca, categoría, precio, descripción, cantidad_restante)
    def marcaNombre = foreignKey("MARCA_FK ", marca , marcas)(_.nombre)
    def categoríaNombre = foreignKey("CATEG_FK", categoría, categorías)(_.nombre)
  }

  // Login
  class Login(tag: Tag) extends Table[(Int, String, String, Boolean)](tag, "USERS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME")
    def password = column[String]("PASSWORD")
    def isAdmin = column[Boolean]("ISADMIN")

    def * = (id, username, password, isAdmin)
  }

  // Definition of the Marca table
  class Marca(tag: Tag) extends Table[(Int, String, String, String, String)](tag, "MARCAS") {
    def id = column[Int]("MARCA_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def nombre = column[String]("NOMBRE")
    def dirección = column[String]("DIRECCIÓN")
    def dirección_entrega = column[String]("DIR_ENTR")
    def contacto = column[String]("CONTACTO")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, nombre, dirección, dirección_entrega, contacto)
  }

  // Definition of the Marca categoria
  class Categoría(tag: Tag) extends Table[(Int, String)](tag, "CATEGORIA") {
    def id = column[Int]("CAT_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def nombre = column[String]("NOMBRE")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, nombre)
  }

  val productos = TableQuery[Producto]
  val marcas = TableQuery[Marca]
  val categorías = TableQuery[Categoría]
  val logins = TableQuery[Login]

  val crearTablas = ( marcas.schema ++ categorías.schema ++ productos.schema ++ logins.schema).create

  //Valores iniciales de Categoría y Marca
val insertarValoresInicialesMarcaYCategoria = DBIO.seq(
  Tables.marcas.map(m => (m.nombre, m.dirección, m.dirección_entrega, m.contacto)) ++= Seq(
    ("Gloria", "Arequipa", "Arequipa", "963521789"),
    ("Laive", "Arequipa", "Arequipa", "953214768"),
    ("Rico Pollo", "Lima", "Arequipa", "842153679"),
    ("Frito Lay", "USA", "Lima", "513348962"),
    ("Coca-Cola company", "USA", "Arequipa", "512365874"),
    ("Ambrosoli", "Italia", "Lima", "421536987"),
    ("Otro", "", "", "")
  ),
  Tables.categorías.map(c => c.nombre) ++= Seq(
    "Bebidas",
    "Carnes",
    "Pescados y mariscos",
    "Frutas",
    "Verduras",
    "Comida rápida",
    "Embutidos",
    "Lácteos",
    "Snacks",
    "Otros"
  ),
  Tables.logins.map(l=>(l.username,l.password,l.isAdmin))++=Seq(
    ("Carlitos","1234",true),
    ("Admin","1234",true),
    ("Junior","1234",false)
  )
)


  //EJEMPLOS:

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