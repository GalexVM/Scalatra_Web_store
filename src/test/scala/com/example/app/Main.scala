package runtime

// import
import user.User
import userInterface.UI
import product.Product

object main extends App{
  val Menu = new UI()
  while (Menu.loop){
    Menu.MainMenu

  }
}