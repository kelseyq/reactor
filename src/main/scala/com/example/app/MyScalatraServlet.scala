package com.example.app

import org.scalatra._
import org.scalatra.ActionResult
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read, write}

class MyScalatraServlet extends ScalatraServlet  {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
      </body>
    </html>
  }
  
  post("/artwork/:art_id/reaction/") {
  val json = ("reaction1" -> (("url" -> ("/artwork/" + params("art_id") + "/reaction/2")) ~ 
      				  ("type" -> "string") ~
      				  ("content" -> "I LIKE THIS ART"))) ~
      		 ("reaction2" -> (("url" -> ("/artwork/" + params("art_id") + "/reaction/3")) ~ 
      				  ("type" -> "string") ~
      				  ("content" -> "I, AS WELL, LIKE THIS ART")))
    params("art_id") match {
      case "1" => pretty(render(json))
    }
  }
  
  get("/artwork/:art_id/reaction/:reaction_id") {
  val json = ("type" -> "string") ~
      				  ("content" -> "THIS IS A REACTION THAT WAS REACTED BY A PATRON")
    params("reaction_id") match {
      case "1337" => pretty(render(json))
    }
  }
  
  post("/artwork/:art_id/reaction/:reaction_id/upvote/") {
   	Ok()
  }
  
  post("/artwork/:art_id/reaction/:reaction_id/downvote/") {
   	Ok()
  }
  
  post("/artwork/:art_id/reaction/:reaction_id/flag/") {
   	Ok()
  }

}

/* (("url" -> ("/artwork/" + params("art_id")) + "/reaction/2") ~ 
      				  ("type" -> "string") ~
      				  ("content" -> "I LIKE THIS ART")) */