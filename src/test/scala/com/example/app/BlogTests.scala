package com.example.app

import org.scalatra.test.scalatest._

class BlogTests extends ScalatraFunSuite {

  addServlet(classOf[Blog], "/*")

  test("GET / on Blog should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
