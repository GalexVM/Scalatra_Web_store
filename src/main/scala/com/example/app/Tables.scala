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

// Definition of the Cart table
  case class Cart(tag: Tag) extends Table[(Int, String, Double)](tag, "CARTS") {
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def nombre: Rep[String] = column[String]("NAME")

    def precio: Rep[Double] = column[Double]("PRICE")

    def * = (id, nombre, precio)
  }
  val productos = TableQuery[Producto]
  val marcas = TableQuery[Marca]
  val categorías = TableQuery[Categoría]
  val logins = TableQuery[Login]
  val carritos = TableQuery[Cart]
  val crearTablas = ( marcas.schema ++ categorías.schema ++ productos.schema ++ logins.schema ++ carritos.schema).create

  //Valores iniciales de Categoría y Marca
val insertarValoresInicialesMarcaYCategoria = DBIO.seq(
  Tables.marcas.map(m => (m.nombre, m.dirección, m.dirección_entrega, m.contacto)) ++= Seq(
    ("Gloria", "Arequipa", "Arequipa", "963521789"),
    ("Laive", "Arequipa", "Arequipa", "953214768"),
    ("Rico Pollo", "Lima", "Arequipa", "842153679"),
    ("Frito Lay", "USA", "Lima", "513348962"),
    ("Coca-Cola company", "USA", "Arequipa", "512365874"),
    ("Ambrosoli", "Italia", "Lima", "421536987"),
    ("Nestlé", "Suiza", "Lima", "312579648"),
    ("Kellogg's", "USA", "Lima", "624897531"),
    ("Hershey's", "USA", "Arequipa", "719638254"),
    ("Danone", "Francia", "Arequipa", "836541927"),
    ("PepsiCo", "USA", "Lima", "427896513"),
    ("Mars, Incorporated", "USA", "Arequipa", "518973246"),
    ("Unilever", "Reino Unido", "Lima", "935214768"),
    ("Procter & Gamble", "USA", "Arequipa", "724689153"),
    ("Johnson & Johnson", "USA", "Lima", "639715428"),
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
    "Panadería",
    "Congelados",
    "Pastas y cereales",
    "Productos enlatados",
    "Dulces y chocolates",
    "Desayuno y cereales",
    "Condimentos y especias",
    "Productos orgánicos",
    "Artículos de limpieza",
    "Utensilios de cocina",
    "Cuidado personal",
    "Electrodomésticos",
    "Libros y revistas",
    "Juguetes y juegos",
    "Decoración del hogar",
    "Productos para mascotas",
    "Otros"
  ),
  Tables.productos.map(p=>(p.nombre, p.marca, p.categoría, p.precio, p.descripción, p.cantidad_restante)) ++=Seq(
    ("Sublime", "Ambrosoli", "Snacks",1.0,"Chocolate sublime", 50),
    ("Leche Gloria","Gloria", "Lácteos", 1.0, "Leche gloria", 50),
    ("Papas Lay","Frito Lay", "Snacks", 0.5, "Papas clásicas", 20),
    ("Coca-Cola","Coca-Cola company", "Bebidas", 0.8, "Coca cola original", 10),
    ("Pollo","Rico Pollo", "Carnes", 0.2, "Pollo entero", 30),
    ("Queso de mesa","Laive", "Lácteos", 0.9, "Queso para pan", 15),
    ("Globos azules","Otro", "Otros", 0.3, "Globos medianos", 5),
    ("Cuates","Ambrosoli", "Snacks", 0.7, "Snack cuates", 25),
    ("Yogur de fresa","Gloria", "Bebidas", 0.6, "Yogur de fresa", 8),
    ("Doritos","Frito Lay", "Snacks", 0.4, "Snack doritos", 12),
    ("Inca kola","Coca-Cola company", "Bebidas", 0.1, "Inka kola original", 18),
    ("Sublime", "Ambrosoli", "Snacks", 1.0, "Chocolate sublime", 50),
    ("Leche Gloria", "Gloria", "Lácteos", 1.0, "Leche Gloria", 50),
    ("Papas Lay", "Frito Lay", "Snacks", 0.5, "Papas clásicas", 20),
    ("Coca-Cola", "Coca-Cola company", "Bebidas", 0.8, "Coca cola original", 10),
    ("Pollo", "Rico Pollo", "Carnes", 0.2, "Pollo entero", 30),
    ("Queso de mesa", "Laive", "Lácteos", 0.9, "Queso para pan", 15),
    ("Globos azules", "Otro", "Otros", 0.3, "Globos medianos", 5),
    ("Cuates", "Ambrosoli", "Snacks", 0.7, "Snack cuates", 25),
    ("Yogur de fresa", "Gloria", "Bebidas", 0.6, "Yogur de fresa", 8),
    ("Doritos", "Frito Lay", "Snacks", 0.4, "Snack Doritos", 12),
    ("Inca Kola", "Coca-Cola company", "Bebidas", 0.1, "Inka Kola original", 18),
    ("Helado de vainilla", "Laive", "Lácteos", 0.8, "Helado de vainilla cremoso", 20),
    ("Leche evaporada", "Gloria", "Lácteos", 0.9, "Leche evaporada Gloria", 25),
    ("Chifles de plátano", "Otro", "Snacks", 0.5, "Chifles de plátano crujientes", 15),
    ("Agua mineral", "Coca-Cola company", "Bebidas", 0.3, "Agua mineral sin gas", 30),
    ("Jamón de pavo", "Rico Pollo", "Carnes", 0.6, "Jamón de pavo ahumado", 10),
    ("Yogur natural", "Gloria", "Lácteos", 0.7, "Yogur natural sin azúcar", 15),
    ("Globos rojos", "Otro", "Otros", 0.2, "Globos rojos para fiestas", 8),
    ("Snack de maní", "Ambrosoli", "Snacks", 0.4, "Snack de maní salado", 20),
    ("Gaseosa de limón", "Coca-Cola company", "Bebidas", 0.5, "Gaseosa de limón refrescante", 12),
    ("Filete de pollo", "Rico Pollo", "Carnes", 0.3, "Filete de pollo jugoso", 18),
    ("Sublime", "Ambrosoli", "Snacks", 1.0, "Chocolate sublime", 50),
    ("Leche Gloria", "Gloria", "Lácteos", 1.0, "Leche gloria", 50),
    ("Papas Lay", "Frito Lay", "Snacks", 0.5, "Papas clásicas", 20),
    ("Coca-Cola", "Coca-Cola company", "Bebidas", 0.8, "Coca cola original", 10),
    ("Pollo", "Rico Pollo", "Carnes", 0.2, "Pollo entero", 30),
    ("Queso de mesa", "Laive", "Lácteos", 0.9, "Queso para pan", 15),
    ("Globos azules", "Otro", "Otros", 0.3, "Globos medianos", 5),
    ("Cuates", "Ambrosoli", "Snacks", 0.7, "Snack cuates", 25),
    ("Yogur de fresa", "Gloria", "Bebidas", 0.6, "Yogur de fresa", 8),
    ("Doritos", "Frito Lay", "Snacks", 0.4, "Snack doritos", 12),
    ("Inca kola", "Coca-Cola company", "Bebidas", 0.1, "Inka kola original", 18),
    ("Helado de vainilla", "Laive", "Lácteos", 0.8, "Helado de vainilla cremoso", 20),
    ("Leche evaporada", "Gloria", "Lácteos", 0.9, "Leche evaporada Gloria", 25),
    ("Papas fritas", "Frito Lay", "Snacks", 0.6, "Papas fritas crujientes", 15),
    ("Sprite", "Coca-Cola company", "Bebidas", 0.3, "Bebida refrescante de lima-limón", 30),
    ("Jamón", "Rico Pollo", "Carnes", 0.5, "Jamón ahumado de cerdo", 10),
    ("Yogur natural", "Gloria", "Lácteos", 0.7, "Yogur natural sin azúcar", 15),
    ("Globos rojos", "Otro", "Otros", 0.2, "Globos rojos para fiestas", 8),
    ("Maní", "Ambrosoli", "Snacks", 0.4, "Maní tostado y salado", 20),
    ("Coca-Cola Zero", "Coca-Cola company", "Bebidas", 0.5, "Coca-Cola sin azúcar", 12),
    ("Filete de pollo", "Rico Pollo", "Carnes", 0.3, "Filete de pollo jugoso", 18)
    
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