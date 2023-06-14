package product

class Product (val id:String,var name:String, var price:double){
  val impuesto :double =2
  def calcularImpuesto:Int = price*0.18
}