package penlight.model.data

import com.github.nscala_time.time.Imports._
import omnibus.commons.util._
import org.joda.{time => joda}


/**
  * Created by rolfsd on 7/16/17.
  */
sealed abstract class TimeSeries extends Series {
  def start: Option[joda.DateTime]
  def end: Option[joda.DateTime]
  def interval: Option[joda.Interval] = {
    for {
      s ← start
      e ← end
    } yield new joda.Interval( s, e + 1.millis )
  }


  def contains( ts: DateTime ): Boolean = points exists { _.timestamp == ts }
  override def toString: String = getClass.safeSimpleName + ":" + points.mkString( "[", ", ", "]" )
}

object TimeSeries {
  def apply( topic: Topic, points: Seq[TimePoint] = Seq.empty[TimePoint] ): TimeSeries = {
    val sorted = points sortBy { _.timestamp }
    val ( start, end ) = if ( sorted.nonEmpty ) ( Some( sorted.head.timestamp ), Some( sorted.last.timestamp ) ) else ( None, None )
    TimeSeriesCell( topic = topic, points = sorted, start = start, end = end )
  }

  def unapply( ts: TimeSeries ): Option[(Topic, Seq[TimePoint])] = Option( (ts.topic, ts.points.toTimePoints) )


  final case class TimeSeriesCell private[model](
    override val topic: Topic,
    override val points: Seq[DataPoint],
    override val start: Option[joda.DateTime],
    override val end: Option[joda.DateTime]
  ) extends TimeSeries


  final case class IncompatibleTopicsError private[model](originalTopic: Topic, newTopic: Topic )
  extends IllegalArgumentException( s"cannot merge time series topics: original [${originalTopic}] amd mew topic [${newTopic}]" )
  with SeriesError
}
