package penlight.model

/**
  * Created by rolfsd on 7/17/17.
  */
abstract class RecentHistory {
  def points: Seq[PointA]
  def retention: Int
  def withPoints( newPoints: Seq[PointA] ): RecentHistory
}

object RecentHistory {
  /** Number of points in a day assuming 1 pt/s rate
    */
  val LastN: Int = 6 * 60 * 24

  def apply( points: Seq[PointA] = Seq.empty[PointA], retention: Int = LastN ): RecentHistory = {
    RecentHistoryCell( points take retention, retention )
  }

  def unapply( recent: RecentHistory ): Option[Seq[PointA]] = Some( recent.points )

  final case class RecentHistoryCell private[model](
    override val points: Seq[PointA],
    override val retention: Int
  ) extends RecentHistory {
    override def withPoints( newPoints: Seq[PointA] ): RecentHistory = {
      val recorded = newPoints drop ( newPoints.size - retention )
      this.copy( points = this.points.drop( this.points.size - retention + recorded.size ) ++ recorded )
    }
  }
}

