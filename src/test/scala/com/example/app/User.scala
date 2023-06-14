package user

class User(var x:String){

  var id: Int = 0
  var name: String = x

  def hello(){
    println("Name: " + name)
  }

}
