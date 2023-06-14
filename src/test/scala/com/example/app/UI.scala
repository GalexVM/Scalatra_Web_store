package userInterface

class UI {
  var loop: Boolean = true
  def MainMenu = {
    println(
        "---------------------------\n" +
        "1.- Test User class\n" +
        "2.- Test Product class\n" +
        "3.- Add tests\n" +
        "0.- Close" +
        "Choose option: "
    )
    var UserInput = scala.io.StdIn.readLine().toInt
    if (UserInput == 0){
      loop = false
    }

  }


}
