package penlight.model.data

import cats.Monoid
import cats.syntax.validated._
import shapeless.the
import monocle.{Lens, PLens}
import org.apache.commons.math3.ml.clustering.DoublePoint
import omnibus.commons._
import omnibus.commons.util._
import penlight.model.data.TimeSeries.IncompatibleTopicsError


/**
  * Created by rolfsd on 7/23/17.
  */
abstract class Series {
  def topic: Topic
  def points: Seq[DoublePoint]
  def size: Int = points.size
  override def toString: String = getClass.safeSimpleName + "(" + topic.toString + ":" + points.mkString( "[", ", ", "]") + ")"
}

object Series {
  trait Factory[S <: Series] {
    type Repr <: Series
    def empty: Repr
    def make( topic: Topic, points: Seq[DoublePoint] ): Repr
  }

  object Factory {
    type Aux[S <: Series, Repr0 <: Series] = Factory[S] { type Repr = Repr0 }
    def apply[S <: Series]( implicit f: Factory[S] ): Aux[S, f.Repr] = f
  }

  type FA[S <: Series] = Factory[S]
  def topicLens[S <: Series : FA]: Lens[Series, Topic] = {
    val get: (Series => Topic) = { _.topic }
    val set: (Topic => Series => Series) = (t: Topic) => (s: Series) => { the[Factory[S]].make( t, s.points ) }
    PLens( get )( set )
  }

  def pointsLens[ S <: Series : FA]: Lens[Series, Seq[DoublePoint]] = {
    val get: (Series => Seq[DoublePoint]) = { _.points }
    val set: (Seq[DoublePoint] => Series => Series) = (pts: Seq[DoublePoint]) => (s: Series) => { the[Factory[S]].make(s.topic, pts) }
    PLens( get )( set )
  }

  def mergeMonoid[S <: Series : FA]( topic: Topic ): Monoid[Series] = new Monoid[Series] {
    override def empty: Series = the[Factory[S]].empty

    override def combine( lhs: Series, rhs: Series ): Series = {
      checkTopic( lhs.topic, rhs.topic )
      .map { t =>
        val combined = combinePoints( lhs.points, rhs.points )
        pointsLens.set( combined )( lhs )
      }
      .unsafeGet
    }

    protected def checkTopic( lhs: Topic, rhs: Topic ): AllIssuesOr[Topic] = {
      if ( lhs == rhs ) lhs.validNel else IncompatibleTopicsError( originalTopic = lhs, newTopic = rhs ).invalidNel
    }

    private def combinePoints( lhs: Seq[DoublePoint], rhs: Seq[DoublePoint] ): Seq[DoublePoint] = {
      val merged = lhs ++ rhs
      val ( uniques, dups ) = merged.groupBy{ _.getPoint.head }.values.partition{ _.size == 1 }

      val dupsAveraged = for {
        ds <- dups
        x: Double = ds.head.getPoint.head
        values: Seq[Double] = ds map { _.getPoint.last }
        avg: Double = values.sum / values.size.toDouble
      } yield Array[Double]( x, avg ).toDoublePoint

      val normalized = uniques.flatten ++ dupsAveraged
      normalized.toIndexedSeq.sortBy{ _.getPoint.head }
    }
  }
}

trait SeriesCompanion[S <: Series] {
  import Series.Factory

  def apply( topic: Topic, points: Seq[DoublePoint] = Seq.empty[DoublePoint] ): Series = {
    val sorted = points sortBy { _.getPoint.head }
    factory.make( topic, sorted )
  }

  implicit def factory: Factory[S]
}