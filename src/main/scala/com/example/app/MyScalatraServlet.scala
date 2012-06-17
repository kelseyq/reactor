package com.example.app

import org.scalatra._
import org.scalatra.ActionResult
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read, write}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoURI
import scala.util.Properties

class MyScalatraServlet extends ScalatraServlet  {

val mongoConn: MongoConnection = MongoConnection(Properties.envOrElse("MONGOHQ_URL", "localhost:27017")) 
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
      builder += "reaction_type" -> theReaction.reaction_type
      builder += "content" -> theReaction.content
      builder += "upvotes" -> 0
      builder += "upvoters" -> MongoDBList.newBuilder.result
      builder += "downvotes" -> 0
      builder += "downvoters" -> MongoDBList.newBuilder.result
      builder += "flags" -> 0
      builder += "flaggers" -> MongoDBList.newBuilder.result
      val newReaction = builder.result
      mongoColl += newReaction
      
      val totalReactions = mongoColl.count.toInt
      val random1 = scala.util.Random.nextInt(totalReactions)
      val random2 = scala.util.Random.nextInt(totalReactions)

      //hideously inefficient--puttin the "hack" in "hackathon"
      val reaction1 = mongoColl.find.limit(-1).skip(random1).next()
      val reaction2 = mongoColl.find.limit(-1).skip(random2).next()


      val json =
           ("reaction1" -> getReactionJson(reaction1)) ~
           ("reaction2" -> getReactionJson(reaction2))

      pretty(render(json))
  }

  private def getReactionJson(dbObj: MongoDBObject) = {
                (("url" -> ("/artwork/" + params("art_id") + "/reaction/" + (dbObj.getAs[String]("user_id") getOrElse("00000")))) ~ 
                ("id" -> (dbObj.getAs[String]("artwork_id") getOrElse("00000"))) ~ 
                ("reaction_type" -> (dbObj.getAs[String]("reaction_type") getOrElse("string"))) ~
                ("content" -> (dbObj.getAs[String]("content") getOrElse("00000"))))
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