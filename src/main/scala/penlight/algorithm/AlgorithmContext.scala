package penlight.algorithm

import com.typesafe.config.Config
import org.apache.commons.math3.ml.clustering.DoublePoint
import penlight.model.data.Topic
import penlight.model.{Plan, RecentHistory}


/**
  * Created by rolfsd on 7/17/17.
  */
abstract class AlgorithmContext {
  def topic: Topic
  def data: Seq[DoublePoint]
  def tolerance: Double
  def plan: Plan
  def properties: Config
  def recent: RecentHistory
}
