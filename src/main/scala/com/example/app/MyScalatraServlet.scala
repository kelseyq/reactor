package com.example.app

import org.scalatra._
import org.scalatra.ActionResult
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read, write}
import com.mongodb.casbah.Imports._

class MyScalatraServlet extends ScalatraServlet  {

val mongoConn: MongoConnection = MongoConnection() 
val mongoColl = mongoConn("reactor")("test_data")

implicit val formats = DefaultFormats


  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
      </body>
    </html>
  }
  
  post("/artwork/:art_id/reaction/") {
    case class Reaction(user_id: String, reaction_type: String, content: String)

   val theReaction = parse(request.body).extract[Reaction]

      val builder = MongoDBObject.newBuilder
      builder += "user_id" -> theReaction.user_id
      builder += "artwork_id" -> params("art_id")
      builder += "type" -> theReaction.reaction_type
      builder += "reaction" -> theReaction.content
      builder += "upvotes" -> 0
      builder += "upvoters" -> MongoDBList.newBuilder.result
      builder += "downvotes" -> 0
      builder += "downvoters" -> MongoDBList.newBuilder.result
      builder += "flags" -> 0
      builder += "flaggers" -> MongoDBList.newBuilder.result
      val newReaction = builder.result
      mongoColl += newReaction


      val json = ("reaction1" -> (("url" -> ("/artwork/" + params("art_id") + "/reaction/2")) ~ 
                ("id" -> "2") ~ 
                ("reaction_type" -> "string") ~
                ("content" -> "I LIKE THIS ART"))) ~
           ("reaction2" -> (("url" -> ("/artwork/" + params("art_id") + "/reaction/3")) ~ 
                ("id" -> "3") ~ 
                ("reaction_type" -> "string") ~
                ("content" -> "I, AS WELL, LIKE THIS ART"))) 

      pretty(render(json))
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