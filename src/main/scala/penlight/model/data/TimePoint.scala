package penlight.model.data

import org.apache.commons.math3.ml.clustering.DoublePoint
import org.joda.{time => joda}


/**
  * Created by rolfsd on 7/3/17.
  */

case class TimePoint( timestamp: joda.DateTime, value: Double ) extends DataPoint {
  override def x: Double = timestamp.getMillis.toDouble
  override def y: Double = value
  override def toString: String = s"(${timestamp}[${timestamp.getMillis}], ${value})"
}

object TimePoint {
  def fromPointA( pt: PointA ): TimePoint = {
    val Array( ts, v ) = pt
    TimePoint( timestamp = new joda.DateTime( ts.toLong ), value = v )
  }

  def fromPointT( pt: PointT ): TimePoint = {
    val ( ts, v ) = pt
    TimePoint( timestamp = new joda.DateTime( ts.toLong ), value = v )
  }

  def fromDoublePoint( pt: DoublePoint ): TimePoint = {
    val Array( ts, v ) = pt.getPoint
    TimePoint( timestamp = new joda.DateTime( ts.toLong ), value = v )
  }

  implicit class SeqDataPointWrapper( val underlying: Seq[TimePoint] ) extends AnyVal {
    def toPointAs: Seq[PointA] = underlying map { _.toPointA }
    def toPointTs: Seq[PointT] = underlying map { _.toPointT }
    def toDoublePoints: Seq[DoublePoint] = underlying map { _.toDoublePoint }
  }

  implicit def timepoint2doublepoint( tp: TimePoint ): DoublePoint = tp.toDoublePoint
  implicit def timepoint2pointa( tp: TimePoint ): PointA = tp.toPointA
  implicit def timepoint2pointt( tp: TimePoint ): PointT = tp.toPointT

  implicit def timepoints2pointAs( tps: Seq[TimePoint] ): Seq[PointA] = tps map { _.toPointA }
  implicit def timepoints2pointts( tps: Seq[TimePoint] ): Seq[PointT] = tps map { _.toPointT }
  implicit def timepoints2doublepoints( tps: Seq[TimePoint] ): Seq[DoublePoint] = tps map { _.toDoublePoint }
}
