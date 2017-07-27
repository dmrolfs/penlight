package penlight.model.data

import java.nio.charset.{Charset, CharsetEncoder}
import java.nio.{ByteBuffer, CharBuffer}
import java.security.MessageDigest

import bloomfilter.CanGenerateHashFrom
import bloomfilter.CanGenerateHashFrom.CanGenerateHashFromString
import omnibus.commons._
import shapeless.the

/**
  * Created by rolfsd on 7/4/17.
  */
case class Topic( label: String, tags: Map[String, String] ) {

  lazy val id: TopicId = Topic computeId this

  override lazy val hashCode: Int = scala.util.hashing.MurmurHash3.stringHash( label )

  override val toString: String = label
}

object Topic {
  val NameTag: String = "name"

  implicit object CanGenerateTopicHash extends CanGenerateHashFrom[Topic] {
    override def generateHash( from: Topic ): Long = CanGenerateHashFromString generateHash from.label
  }

  type TP[_] = TopicParser
  implicit def fromString[_: TP]( topic: String ): AllIssuesOr[Topic] = the[TopicParser] apply topic

  def findAncestor( topics: Topic* ): Topic = {
    def trim( topic: String ): String = {
      val Delim = """[._+\:\-\/\\]+""".r
      Delim.findPrefixOf( topic.reverse ) map { t ⇒ topic.slice( 0, topic.length - t.length ) } getOrElse topic
    }

    trim(
      if ( topics.isEmpty ) ""
      else {
        var i = 0
        topics( 0 ).label.takeWhile( ch ⇒ topics.forall( _.label( i ) == ch ) && { i += 1; true } ).mkString
      }
    ).toTopic
  }

  type Pair = (String, String)

  private implicit val keyOrdering = new Ordering[Pair] {
    override def compare( lhs: Pair, rhs: Pair ): Int = lhs._1 compareTo rhs._1
  }

  private def computeId( topic: Topic ): TopicId = if ( topic.tags.isEmpty) TopicId.empty else TopicId( tagsDigest(topic.tags) )

  private val commaByte: Byte = ','.asInstanceOf[Byte]
  private val equalsByte: Byte = '='.asInstanceOf[Byte]

  private def tagsDigest( tags: Map[String, String] ): Array[Byte] = {
    val (pairs, maxLength) = tags.foldLeft( (Seq.empty[Pair], 0) ) { case ((acc, accMaxLength), (k, v)) =>
      val candidateMax = scala.math.max( k.length, v.length )
      val newMaxLength = scala.math.max( candidateMax, accMaxLength )
      ( acc :+ (k,v), newMaxLength )
    }

    val sorted = pairs.sorted
    val md = java.security.MessageDigest.getInstance( "SHA-1" )
    val enc = Charset.forName( "UTF-8" ).newEncoder
    val cbuf = CharBuffer allocate maxLength
    val buf = ByteBuffer allocate (maxLength * 2)
    val write = writePairIntoDigest( cbuf, buf, enc )_

    write( sorted.head, md )
    val msgDigest = sorted.foldLeft( md ){ (m, p) =>
      m update commaByte
      write( p, m )
      m
    }

    msgDigest.digest
  }

  private def writePairIntoDigest( cbuf: CharBuffer, buf: ByteBuffer, enc: CharsetEncoder )( p: Pair, md: MessageDigest ): Unit = {
    cbuf.clear
    cbuf put p._1
    cbuf.flip
    enc.encode( cbuf, buf, true )
    buf.flip
    md update buf
    buf.clear

    md update equalsByte

    cbuf.clear
    cbuf put p._2
    cbuf.flip
    enc.encode( cbuf, buf, true )
    buf.flip
    md update buf
    buf.clear
  }


  implicit class TopicString( val underlying: String ) extends AnyVal {
    def toTopic: Topic = Topic.fromString( underlying ).unsafeGet
  }
}

