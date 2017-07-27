package penlight.model

import cats.syntax.validated._
import omnibus.commons.{AllErrorsOr, AllIssuesOr}
import penlight.model.data.{TimeSeries, Topic}

/**
  * Created by rolfsd on 7/19/17.
  */
abstract class ReduceAnomalyPDMs extends ( ( AlgorithmResults, TimeSeries, Plan ) => AllErrorsOr[AnomalyPDM] ) with Serializable

object ReduceAnomalyPDMs {

  def checkResults( results: AlgorithmResults, plan: Plan, topic: Topic ): AllIssuesOr[AlgorithmResults] = {
    if ( results.nonEmpty ) results.validNel
    else EmptyResultsError( plan, topic ).invalidNel
  }

  final case class EmptyResultsError private[outlier] ( plan: Plan, topic: Topic )
  extends IllegalStateException( s"ReduceOutliers called on empty results in plan:[${plan.name}] topic:[${topic}]" )

}
