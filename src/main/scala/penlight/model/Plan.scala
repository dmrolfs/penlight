package penlight.model

import scala.concurrent.duration.Duration
import omnibus.commons.util._
import penlight.model.data.{TimeSeries, Topic}

import scala.util.matching.Regex


/**
  * Created by rolfsd on 7/19/17.
  */
abstract class Plan {
  def name: String
  def slug: String = name
  def isActive: Boolean = true
  def appliesTo: Plan.AppliesTo
  def algorithms: Set[AlgorithmSummary]
  def timeout: Duration
  def isQuorum: IsQuorum
  def reduce: ReduceAnomalyPDMs
  def description: String = getClass.safeSimpleName + "(" + name + " " + appliesTo.toString + ")"
  def summarize: Plan.Summary

  private[model] def precedence: Int
  private[model] def originLineNumber: Int
}

object Plan {
  implicit val planOrdering: Ordering[Plan] = new Ordering[Plan] {
    override def compare( lhs: Plan, rhs: Plan ): Int = {
      val precedenceOrdering = Ordering[Int].compare( lhs.precedence, rhs.precedence )
      if ( precedenceOrdering != 0 ) precedenceOrdering
      else Ordering[Int].compare( lhs.originLineNumber, rhs.originLineNumber )
    }
  }

  case class Summary private[model]( name: String, slug: String, isActive: Boolean, appliesTo: AppliesTo )
  implicit def summarize( p: Plan ): Summary = p.summarize

  case class Scope( plan: String, topic: Topic ) {
    def name: String = toString
    override def toString: String = plan + ":" + topic.toString
  }

  object Scope {
    def apply( plan: Plan, topic: Topic ): Scope = Scope( plan = plan.name, topic = topic )
    def apply( plan: Plan, data: TimeSeries ): Scope = Scope( plan = plan.name, topic = data.topic )
  }

  abstract class AppliesTo extends ( Any => Boolean ) with Serializable

  object AppliesTo {
    def function( f: Any => Boolean ): AppliesTo = new AppliesTo {
      override def apply( message: Any ): Boolean = f( message )
      override def toString: String = "Plan.AppliesTo.function"
    }

    def partialFunction( pf: PartialFunction[Any, Boolean] ): AppliesTo = new AppliesTo {
      override def apply( message: Any ): Boolean = if ( pf isDefinedAt message ) pf( message ) else false
      override def toString: String = "Plan.AppliesTo.partialFunction"
    }

    def topics( topics: Set[Topic], extractTopic: ExtractTopic ): AppliesTo = new AppliesTo {
      override def apply( message: Any ): Boolean = {
        if ( !extractTopic.isDefinedAt(message) ) false
        else extractTopic( message ) map { topics.contains } getOrElse { false }
      }
      override def toString: String = "Plan.AppliesTo.topics" + topics.mkString( "[", ", ", "]" )
    }

    def regex( regex: Regex, extractTopic: ExtractTopic ): AppliesTo = new AppliesTo {
      override def apply( message: Any ): Boolean = {
        if ( !extractTopic.isDefinedAt(message) ) false
        else {
          extractTopic( message )
          .flatMap { t => regex findFirstMatchIn t.toString }
          .isDefined
        }
      }
      override def toString: String = "Plan.AppliesTo.regex[" + regex.toString + "]"
    }

    val all: AppliesTo = new AppliesTo {
      override def apply( message: Any ): Boolean = true
      override def toString: String = "Plan.AppliesTo.all"
    }
  }


  type ExtractTopic = PartialFunction[Any, Option[Topic]]
  object ExtractTopic {
    def apply( pf: ExtractTopic ): ExtractTopic = pf
    val direct: ExtractTopic = {
      case label: String => Some( Topic( label, tags = Map.empty[String, String] ) )
      case _ => None
    }
  }
}
