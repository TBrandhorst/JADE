/*
 * $Log$
 * Revision 1.8  1999/04/06 00:09:36  rimassa
 * Documented public classes with Javadoc. Reduced access permissions wherever possible.
 *
 * Revision 1.7  1998/11/03 00:31:26  rimassa
 * A cosmetic change in a comment.
 *
 * Revision 1.6  1998/10/31 16:34:51  rimassa
 * Removed useless inheritance from AWTEvent class.
 *
 * Revision 1.5  1998/10/04 18:01:04  rimassa
 * Added a 'Log:' field to every source file.
 *
 */

package jade.core;

import jade.lang.acl.*;

/***************************************************************

  Name: CommEvent

  Responsibilities and Collaborations:

  + Objectifies the reception event, embedding the received ACL
    message.
    (ACLMessage)

  + Holds a list of recipients agent to allow trasparent message
    multicasting
    (AgentGroup)

******************************************************************/
class CommEvent {

  private ACLMessage  message;
  private AgentGroup recipients;

  public CommEvent(CommBroadcaster source, ACLMessage message) {
    // Message cloning is necessary for intra-VM messaging, since no
    // message serialization is carried out in that case
    this.message = (ACLMessage)message.clone();
    recipients = null;
  }

  public CommEvent(CommBroadcaster source, ACLMessage message, AgentGroup group) {
    this(source, message);
    recipients = group;
  }

  public ACLMessage getMessage() {
    return message;
  }

  public boolean isMulticast() {
    return recipients != null;
  }

  public AgentGroup getRecipients() {
    return recipients;
  }

}
