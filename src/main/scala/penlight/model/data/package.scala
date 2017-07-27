package penlight.model

import com.cra.figaro.language.Element
import org.apache.commons.math3.ml.clustering.DoublePoint
import org.joda.{time => joda}


/**
  * Created by rolfsd on 7/2/17.
  */
package object data {
  type AnomalyPDM = Element[Boolean]
  type AlgorithmResults = Map[AlgorithmSummary, AnomalyPDM]

  type PointA = Array[Double]
  type PointT = ( Double, Double )

  implicit class PointAWrapper( val underlying: PointA ) extends AnyVal {
    def x: Double = toPointT._1
    def y: Double = toPointT._2
    def timestamp: Double = x
    def dateTime: joda.DateTime = new joda.DateTime( timestamp.toLong )
    def value: Double = y

    def toPointT: PointT = ( underlying( 0 ), underlying( 1 ) )
    def toDoublePoint: DoublePoint = new DoublePoint( underlying )
    def toDataPoint: DataPoint = DataPoint( x = underlying( 0 ), y = underlying( 1 ) )
    def toTimePoint: TimePoint = TimePoint( timestamp = new joda.DateTime( underlying( 0 ).toLong ), value = underlying( 1 ) )
  }

  implicit class PointTWrapper( val underlying: PointT ) extends AnyVal {
    def x: Double = underlying._1
    def y: Double = underlying._2
    def timestamp: Double = x
    def dateTime: joda.DateTime = new joda.DateTime( timestamp.toLong )
    def value: Double = y

    def toPointA: PointA = Array( underlying._1, underlying._2 )
    def toDoublePoint: DoublePoint = new DoublePoint( toPointA )
    def toDataPoint: DataPoint = DataPoint( x = underlying._1, y = underlying._2 )
    def toTimePoint: TimePoint = TimePoint( timestamp = new joda.DateTime( underlying._1.toLong ), value = underlying._2 )
  }

  implicit class DoublePointWrapper( val underlying: DoublePoint ) extends AnyVal {
    def x: Double = toPointT._1
    def y: Double = toPointT._2
    def timestamp: Double = x
    def dateTime: joda.DateTime = new joda.DateTime( timestamp.toLong )
    def value: Double = y

    def toPointA: PointA = underlying.getPoint
    def toPointT: PointT = {
      val Array( ts, v ) = underlying.getPoint
      ( ts, v )
    }
    def toDataPoint: DataPoint = {
      val pt = underlying.getPoint
      DataPoint( x = pt( 0 ), y = pt( 1 ) )
    }
    def toTimePoint: TimePoint = {
      val Array( ts, v ) = underlying.getPoint
      TimePoint( timestamp = new joda.DateTime( ts.toLong ), value = v )
    }
  }

  implicit class SeqPointAWrapper( val underlying: Seq[PointA] ) extends AnyVal {
    def toPointTs: Seq[PointT] = underlying map { _.toPointT }
    def toDoublePoints: Seq[DoublePoint] = underlying map { _.toDoublePoint }
    def toDataPoints: Seq[DataPoint] = underlying map { _.toDataPoint }
    def toTimePoints: Seq[TimePoint] = underlying map { _.toTimePoint }
  }

  implicit class SeqPointTWrapper( val underlying: Seq[PointT] ) extends AnyVal {
    def toPointAs: Seq[PointA] = underlying map { _.toPointA }
    def toDoublePoints: Seq[DoublePoint] = underlying map { _.toDoublePoint }
    def toDataPoints: Seq[DataPoint] = underlying map { _.toDataPoint }
    def toTimePoints: Seq[TimePoint] = underlying map { _.toTimePoint }
  }

  implicit class SeqDoublePointWrapper( val underlying: Seq[DoublePoint] ) extends AnyVal {
    def toPointAs: Seq[PointA] = underlying map { _.toPointA }
    def toPointTs: Seq[PointT] = underlying map { _.toPointT }
    def toDataPoints: Seq[DataPoint] = underlying map { _.toDataPoint }
    def toTimePoints: Seq[TimePoint] = underlying map { _.toTimePoint }
  }

  implicit def pointa2pointt( pt: PointA ): PointT = pt.toPointT
  implicit def pointa2doublepoint( pt: PointA ): DoublePoint = pt.toDoublePoint
  implicit def pointt2pointa( pt: PointT ): PointA = pt.toPointA
  implicit def pointt2doublepoint( pt: PointT ): DoublePoint = pt.toDoublePoint
  implicit def doublepoint2pointa( dp: DoublePoint ): PointA = dp.toPointA
  implicit def doublepoint2pointt( dp: DoublePoint ): PointT = dp.toPointT

  implicit def pointas2pointts( pts: Seq[PointA] ): Seq[PointT] = pts map { pointa2pointt }
  implicit def pointas2doublepoints( pts: Seq[PointA] ): Seq[DoublePoint] = pts map { pointa2doublepoint }
  implicit def pointts2pointas( pts: Seq[PointT] ): Seq[PointA] = pts map { pointt2pointa }
  implicit def pointts2doublepoints( pts: Seq[PointT] ): Seq[DoublePoint] = pts map { pointt2doublepoint }
  implicit def doublepoints2pointas( dps: Seq[DoublePoint] ): Seq[PointA] = dps map { doublepoint2pointa }
  implicit def doublepoints2pointts( dps: Seq[DoublePoint] ): Seq[PointT] = dps map { doublepoint2pointt }


  trait SeriesError
}
