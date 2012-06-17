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

val collName = "reactor_data" // <= set your collection name.
val MongoSetting(db) = Properties.envOrNone("MONGOHQ_URL")
val mongoColl: MongoCollection = db(collName)

implicit val formats = DefaultFormats


  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
      </body>
    </html>
  }
  
  case class Reaction(user_id: String, reaction_type: String, content: String)

  post("/artwork/:art_id/reaction/") {
      val theReaction = parse(request.body).extract[Reaction]

      if (validateReaction(theReaction)) {
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
      }

      val otherReactions = getReactions(theReaction.user_id, params("art_id"))

      if (!otherReactions.isEmpty) {
          val json =
            ("reaction1" -> getReactionJson(otherReactions(0))) ~
            ("reaction2" -> getReactionJson(otherReactions(1)))
          pretty(render(json))
      } else Ok()
  }

  private def validateReaction(reaction: Reaction): Boolean = {
    !(reaction.content.isEmpty)
  }

  private def getReactionJson(dbObj: MongoDBObject) = {
                (("url" -> ("/artwork/" + params("art_id") + "/reaction/" + (dbObj.getAs[String]("_id") getOrElse("00000")))) ~ 
                ("reaction_id" -> (dbObj.getAs[ObjectId]("_id").map(_.toString) getOrElse("00000"))) ~ 
                ("reaction_type" -> (dbObj.getAs[String]("reaction_type") getOrElse("string"))) ~
           //  ("user_id" -> (dbObj.getAs[String]("user_id") getOrElse("no id"))) ~
                ("content" -> (dbObj.getAs[String]("content") getOrElse("00000")))) 
  }

  def artworkFilter(artwork_id: String) = MongoDBObject("artwork_id" -> artwork_id)

  private def getReactions(reacting_user: String, artwork_id: String): List[MongoDBObject] = {
      //hideously inefficient--puttin the "hack" in "hackathon"
      //TODO: filter out flagged reactions & reactions with too many downvotes
      //      bubble up higher voted reactions? 

      val filter = ("user_id" $ne 0) //("user_id" $ne reacting_user)

      val otherReactions = mongoColl.find(filter)
      val totalReactions = otherReactions.count.toInt
      if (totalReactions > 1) {
        val random1 = scala.util.Random.nextInt(totalReactions)
        val random2 = scala.util.Random.nextInt(totalReactions)

        val reaction1 = mongoColl.find(filter).limit(-1).skip(random1).next()
        val reaction2 = mongoColl.find(filter).limit(-1).skip(random2).next()
        List(reaction1, reaction2)
      } else {
        List()
      }
  }
  
  get("/artwork/:art_id/reaction/:reaction_id") {
    val o : DBObject = MongoDBObject("_id" -> new ObjectId(params("reaction_id")))
    val u = mongoColl.findOne(o)
    pretty(render(u.map(getReactionJson(_)).getOrElse("")))
  }

  post("/admin/clear/") {
    mongoColl.dropCollection
    Ok()
  }

  //todo: validate users, make sure they can only vote once

  post("/artwork/:art_id/reaction/:reaction_id/upvote") {
    val oid : DBObject = MongoDBObject("_id" -> new ObjectId(params("reaction_id")))
    mongoColl.update(oid, $inc("upvotes" -> 1))
    mongoColl.update(oid, $push("upvoters" -> params("user_id")))
    Ok()
  }
  
  post("/artwork/:art_id/reaction/:reaction_id/downvote") {
    val oid : DBObject = MongoDBObject("_id" -> new ObjectId(params("reaction_id")))
    mongoColl.update(oid, $inc("downvotes" -> 1))
    mongoColl.update(oid, $push("downvoters" -> params("user_id")))
    Ok()
  }
  
  post("/artwork/:art_id/reaction/:reaction_id/flag") {
    val oid : DBObject = MongoDBObject("_id" -> new ObjectId(params("reaction_id")))
    mongoColl.update(oid, $inc("flags" -> 1))
    mongoColl.update(oid, $push("flaggers" -> params("user_id")))
    Ok()
  }

  get("/artwork/:art_id/reactions/") {
    val reactions = mongoColl.find(artworkFilter(params("art_id"))).toList.map(getReactionJson(_))

    pretty(render(reactions))
  }

  get("/artwork/:art_id/top") {
    val o : DBObject = MongoDBObject("artwork_id" -> params("art_id"))

    val winners = mongoColl.find(o).sort(MongoDBObject("upvotes" -> -1)).limit(params.get("number").getOrElse("1").toInt).toList.map(getReactionJson(_))

    pretty(render(winners))
  }

}

object MongoSetting {
  def unapply(url: Option[String]): Option[MongoDB] = {
    val regex = """mongodb://(\w+):(\w+)@([\w|\.]+):(\d+)/(\w+)""".r
    url match {
      case Some(regex(u, p, host, port, dbName)) =>
        val db = MongoConnection(host, port.toInt)(dbName)
        db.authenticate(u,p)
        Some(db)
      case None =>
        Some(MongoConnection("localhost", 27017)("reactor_test"))
    }
  }
}