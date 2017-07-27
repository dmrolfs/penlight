package penlight.model

import omnibus.commons.util._


/**
  * Created by rolfsd on 7/19/17.
  */
sealed abstract class IsQuorum extends ( AlgorithmResults => Boolean ) with Serializable {
  def totalIssued: Int
  protected def hasAllResults( results: AlgorithmResults ): Boolean = results.size >= totalIssued
}

object IsQuorum {
  def apply( totalIssued: Int ): IsQuorum = RequireFullQuorum( totalIssued = totalIssued )

  case class RequireFullQuorum( override val totalIssued: Int ) extends IsQuorum {
    override def apply( results: AlgorithmResults ): Boolean = hasAllResults( results )
    override def toString: String = getClass.safeSimpleName + "(" + totalIssued + ")"
  }

//DMR: Where anomalies are probability density functions, does anything less than full make sense?
//  case class AtLeastQuorum( override val totalIssued: Int, triggerPoint: Int ) extends IsQuorum {
//    require( triggerPoint <= totalIssued, "trigger point cannot be greater than total issued" )
//    override def apply( results: Map[AlgorithmSummary, AnomalyPDM] ): Boolean = triggerPoint <= results.size
//    override def toString: String = getClass.safeSimpleName + s"(trigger:[${triggerPoint}] of total:[${totalIssued}])"
//  }
//
//  case class MajorityQuorum( override val totalIssued: Int, triggerPoint: Double ) extends IsQuorum {
//    require( triggerPoint <= 1.0, "trigger point must be a percentage" )
//    override def apply( results: Map[AlgorithmSummary, AnomalyPDM] ): Boolean = {
//      triggerPoint <= ( results.size / totalIssued.toDouble )
//    }
//    override def toString: String = getClass.safeSimpleName + s"(trigger:[${triggerPoint}]% of total:[${totalIssued}])"
//  }
}
