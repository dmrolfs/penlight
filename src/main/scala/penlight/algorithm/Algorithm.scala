package penlight.algorithm

import cats.data.State
import cats.syntax.applicative._
import shapeless.the
import org.apache.commons.math3.ml.clustering.DoublePoint
import penlight.model._
import penlight.model.PointT
import penlight.algorithm.Algorithm.AlgorithmState
import penlight.model.data.DataPoint


abstract class Algorithm[S <: Serializable : Advancing] {
  type Context <: AlgorithmContext

  type C[_] = Context

  def score[_: C]( shape: S, point: PointT ): AnomalyPDM

  def evalOne[_: C]( point: DataPoint ): AlgorithmState[S] = {
    State[S, AnomalyPDM] { shape => ( the[Advancing[S]].advance( shape, point ), score( shape, point ) ) }
  }

  def evalAll[_: C]( shape: S, points: Seq[DoublePoint] ): AlgorithmState[S] = {
    points.foldLeft( shape.pure[AlgorithmState[S]] ){ (s, pt) => s flatMap { _ => evalOne( pt.toDataPoint ) } }
  }
}

object Algorithm {
  type AlgorithmState[S <: Serializable] = State[S, AnomalyPDM]
}
