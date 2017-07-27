package penlight.model.data

import java.math.BigInteger

import cats.data.NonEmptyList
import cats.syntax.either._
import cats.syntax.validated._
import omnibus.commons.{AllErrorsOr, AllIssuesOr}

import scala.util.Try
import scala.util.hashing.MurmurHash3


/**
  * Represents an identifier for a tagged topic.
  *
  * @param data
  *     Bytes for the id. This is usually the results of computing a SHA1 hash
  *     over a normalized representation of the tags.
  * @param hc
  *     Precomputed hash code for the bytes.
  */
class TopicId private ( private val data: Array[Byte], private val hc: Int ) extends Comparable[TopicId] {

  override def hashCode(): Int = hc

  override def equals( rhs: Any ): Boolean = {
    rhs match {
      case that: TopicId => {
        if ( this eq that ) true
        else {
          ( that.## == this.## ) &&
          ( this.hc == that.hc ) &&
          java.util.Arrays.equals( this.data, that.data )
        }
      }

      case _ => false
    }
  }

  override def compareTo( that: TopicId ): Int = {
    val length = math.min( data.length, that.data.length )
    var i = 0
    while ( i < length ) {
      val b1 = java.lang.Byte.toUnsignedInt( data(i) )
      val b2 = java.lang.Byte.toUnsignedInt( that.data(i) )
      val cmp = b1 - b2
      if ( cmp != 0 ) return cmp
      i += 1
    }
    0
  }

  override def toString: String = {
    val buffer = new StringBuilder
    var i = 0
    while ( i < data.length ) {
      val unsigned = java.lang.Byte.toUnsignedInt( data(i) )
      buffer append TopicId.hexValueForByte(unsigned)
      i += 1
    }
    buffer.toString
  }

  def toBigInteger: BigInteger = new BigInteger( 1, data )
}

object TopicId {
  /**
    * Create a new id from an array of bytes. The pre-computed hash code will be generated using MurmurHash3.
    */
  def apply( data: Array[Byte] ): TopicId = new TopicId( data, MurmurHash3 bytesHash data )

  /**
    * Create a new id from a hex string. The string should match the `toString` output of a `TopicId`.
    */
  def apply( data: String ): AllIssuesOr[TopicId] = {
    checkStringData( data ) withEither { _ flatMap { checkedStringToHex } map { TopicId.apply } }
  }

  private[model] val empty: TopicId = {
    val md = java.security.MessageDigest.getInstance( "SHA-1" )
    md.update( "".getBytes("UTF-8") )
    TopicId( md.digest )
  }

  private val hexValueForByte = ( 0 until 256 ).toArray map { i => "%2s".format( Integer.toHexString(i) ).replace( ' ', '0' ) }

  private def checkStringData( data: String ): AllIssuesOr[String] = {
    if ( data.length % 2 == 0 ) data.validNel else InvalidTopicIdError( data ).invalidNel
  }

  private def checkedStringToHex( data: String ): AllErrorsOr[Array[Byte]] = {
    Either
    .fromTry {
      Try {
        val bytes = new Array[Byte]( data.length / 2 )
        var i = 0
        while ( i < bytes.length ) {
          val c1 = hexToInt( data.charAt(2 * i) )
          val c2 = hexToInt( data.charAt(2 * i + 1) )
          val v = ( c1 << 4 ) | c2
          bytes( i ) = v.toByte
          i += 1
        }

        bytes
      }
    }
    .leftMap { ex => NonEmptyList[Throwable]( ex, Nil ) }
  }

  private def hexToInt( c: Char ): Int = {
    c match {
      case _ if '0' <= c && c <= '9' => c - '0'
      case _ if 'a' <= c  && c <= 'f' => c - 'a' + 10
      case _ if 'A' <= c && c <= 'F' => c - 'A' + 10
      case _ => throw new IllegalArgumentException( s"invalid hex digit: $c" )
    }
  }

  final case class InvalidTopicIdError private[model](data: String )
  extends IllegalArgumentException( s"invalid topic id string: ${data}" )
}
