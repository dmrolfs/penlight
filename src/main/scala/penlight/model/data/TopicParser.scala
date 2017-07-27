package penlight.model.data

import cats.syntax.validated._
import omnibus.commons.AllIssuesOr


/**
  * Created by rolfsd on 7/12/17.
  */
abstract class TopicParser extends ( String => AllIssuesOr[Topic] )


object TopicParser {
  class IdentityTopicParser extends TopicParser {
    override def apply( topic: String ): AllIssuesOr[Topic] = Topic( label = topic, Map( Topic.NameTag -> topic ) ).validNel
  }

  implicit val labelIdentity: TopicParser = new IdentityTopicParser
}
