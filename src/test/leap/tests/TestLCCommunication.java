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

package test.leap.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import test.common.*;
import test.leap.LEAPTesterAgent;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
   This Test addresses the basic operations of life-cycle management (agent creation/killing) 
   and communication (message exchange) in a wireless device.   
   @author Giovanni Caire - TILAB
 */
public class TestLCCommunication extends Test {
	private static final String PING_AGENT = "ping";
	private static final String CONV_ID = "ping_conv";
	
	private String lightContainerName = "Container-1";
	private int ret = Test.TEST_FAILED;
	private AID ping;
	private Logger l = Logger.getLogger();
	
  public Behaviour load(final Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
		// MIDP container name as group argument
		lightContainerName = (String) getGroupArgument(LEAPTesterAgent.LIGHT_CONTAINER_KEY);

		final SequentialBehaviour sb = new SequentialBehaviour(a) {
			public int onEnd() {
				store.put(key, new Integer(ret));
				return 0;
			}
		};
		
		// Step 1: Create the ping agent on the light container
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					ping = TestUtility.createAgent(a, PING_AGENT, "test.leap.midp.PingAgent", null, a.getAMS(), lightContainerName);
					l.log("Ping agent correctly created.");
				}
				catch (Exception e) {
					l.log("Error creating Ping agent.");
					e.printStackTrace();
					sb.skipNext();
				}
			}
		} );
		// Step 2: Send a message to the ping agent and gets the reply
		sb.addSubBehaviour(new SimpleBehaviour(a) {
			private boolean finished = false;
			
			public void onStart() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(ping);
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
				l.log("Message 1 sent to Ping agent.");
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(CONV_ID));
				if (msg != null) {
					if (msg.getSender().equals(ping)) {
						l.log("Reply from Ping agent correctly received.");
					}
					else {
						l.log("Unexpected reply received: "+msg);
						sb.skipNext();
					}
					finished = true;
				}
				else {
					block();
				}
			}
			
			public boolean done() {
				return finished;
			}
		} );
		// Step 3: Kill the ping agent on the light container
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					TestUtility.killAgent(a, ping);
					l.log("Ping agent correctly killed.");
				}
				catch (Exception e) {
					l.log("Error creating Ping agent.");
					e.printStackTrace();
					sb.skipNext();
				}
			}
		} );
		// Step 4: Send a message to the ping agent and gets the FAILURE from the AMS
		sb.addSubBehaviour(new SimpleBehaviour(a) {
			private boolean finished = false;
			
			public void onStart() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(ping);
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
				l.log("Message 2 sent to Ping agent.");
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(CONV_ID));
				if (msg != null) {
					if (msg.getSender().equals(myAgent.getAMS()) && msg.getPerformative() == ACLMessage.FAILURE) {
						l.log("FAILURE notification from AMS correctly received.");
					}
					else {
						l.log("Unexpected reply received: "+msg);
						sb.skipNext();
					}
					finished = true;
				}
				else {
					block();
				}
			}
			
			public boolean done() {
				return finished;
			}
		} );
		// Step 5: If we get here the test is passed
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				ret = Test.TEST_PASSED;
			}
		} );

  	return sb;
  }
}
