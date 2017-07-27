package penlight.algorithm

import penlight.model.data.DataPoint
import shapeless.the


/**
  * Created by rolfsd on 7/17/17.
  */
abstract class Advancing[S] {
  def empty: S
  def advance( shape: S, dp: DataPoint ): S
  def N( shape: S ): Long
  def copy( shape: S ): S
}

object Advancing {
  /** type class summoner
    *
    * @tparam S
    * @return
    */
  def apply[S]( implicit advancing: Advancing[S] ): Advancing[S] = advancing

  object syntax {
    implicit class AdvancingOps[S: Advancing]( val shape: S ) {
      def N: Long = the[Advancing[S]].N( shape )
      def advance( dp: DataPoint ): S = the[Advancing[S]].advance( shape, dp )
      def copy: S = the[Advancing[S]].copy( shape )
    }
  }
}