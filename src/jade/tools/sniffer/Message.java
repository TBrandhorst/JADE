/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation,
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.tools.sniffer;

import java.io.Serializable;
import java.util.Iterator;
import javax.swing.SwingUtilities;

import jade.core.AID;
import jade.lang.acl.ACLMessage;


/**
 * A <em>Message</em> extends the meaning of an ACLMessage (thus extending
 * jade.lang.acl.ACLMessage) providing an ACLMessage with the graphic part: the
 * arrow going from the sender of the message to the receiver of the message to
 * be drawn on the Message Canvas
 *
 * @see jade.lang.acl.ACLMessage

 * MLG added private messageNumber, get and set.
 */

public class Message extends jade.lang.acl.ACLMessage implements Serializable {

  private int x1,x2,y;
  private int xCoords[] = new int[3];
  private int yCoords[] = new int[3];

  public static final int step = 80;
  public static final int offset = 45;
  public static final int r = 8;
  private int yDim = 0;
  private int xS = 0;
  private int xD = 0;
  private int messageNumber=0;

  public Message(AID s, AID r){

    super(ACLMessage.INFORM);

    this.setSender(s);
    this.clearAllReceiver();
    this.addReceiver(r);
  }

  public Message(ACLMessage msg) {

    super(msg.getPerformative());
    this.clearAllReceiver();
    for (Iterator i=msg.getAllReceiver(); i.hasNext(); )
      this.addReceiver((AID)i.next());

    //Iterator it = msg.getAllReceiver();
    //AID id = (AID)it.next();
    //this.addReceiver(id);
    this.setSender(msg.getSender());
    this.setContent(msg.getContent());
    this.setReplyWith(msg.getReplyWith());
    this.setInReplyTo(msg.getInReplyTo());
    this.setLanguage(msg.getLanguage());
    this.setOntology(msg.getOntology());
    this.setReplyByDate(msg.getReplyByDate());
    this.setProtocol(msg.getProtocol());
    this.setConversationId(msg.getConversationId());

  }


  public int getInitSeg(int xS){
    x1 = xS * step + offset+4;
    return x1;
  }

  public int getEndSeg(int xD){
    x2 = xD * step + offset;
    return x2;
  }

  public int getOrdSeg(int yDim){
    y = (yDim * 20) + step - 50;
    return y;
  }

  public int getMessageNumber(){
    return messageNumber;
  }  

  public void setMessageNumber(int n){
    messageNumber=n;
  }

} // End of class Message
