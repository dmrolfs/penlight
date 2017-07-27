package penlight.model.data

import org.apache.commons.math3.ml.clustering.DoublePoint


/**
  * Created by rolfsd on 7/3/17.
  */
abstract class DataPoint {
  def x: Double

  def y: Double

  def toPointA: PointA = Array( x, y )

  def toPointT: PointT = (x, y)

  def toDoublePoint: DoublePoint = new DoublePoint( toPointA )

  override def toString: String = s"(${x}, ${y})"
}

object DataPoint {
  def apply( x: Double, y: Double ): DataPoint = DataPointCell( x, y )
  def unapply( dp: DataPoint ): Option[(Double, Double)] = Option( dp.toPointT )

  case class DataPointCell( override val x: Double, override val y: Double ) extends DataPoint

  def fromPointA( pt: PointA ): DataPoint = DataPointCell( x = pt(0), y = pt(1) )

  def fromPointT( pt: PointT ): DataPoint = DataPoint( x = pt._1, y = pt._2 )

  def fromDoublePoint( pt: DoublePoint ): DataPoint = fromPointA( pt.getPoint )

  implicit class SeqDataPointWrapper( val underlying: Seq[DataPoint] ) extends AnyVal {
    def toPointAs: Seq[PointA] = underlying map { _.toPointA }
    def toPointTs: Seq[PointT] = underlying map { _.toPointT }
    def toDoublePoints: Seq[DoublePoint] = underlying map { _.toDoublePoint }
  }

  implicit def datapoint2doublepoint( dp: DataPoint ): DoublePoint = dp.toDoublePoint
  implicit def datapoint2pointa( dp: DataPoint ): PointA = dp.toPointA
  implicit def datapoint2pointt( dp: DataPoint ): PointT = dp.toPointT

  implicit def datapoints2pointAs( dps: Seq[DataPoint] ): Seq[PointA] = dps map { _.toPointA }
  implicit def datapoints2pointts( dps: Seq[DataPoint] ): Seq[PointT] = dps map { _.toPointT }
  implicit def datapoints2doublepoints( dps: Seq[DataPoint] ): Seq[DoublePoint] = dps map { _.toDoublePoint }
}
