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

package jade.proto;

//#MIDP_EXCLUDE_FILE

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import jade.util.leap.*;

/**
 * Class description
 * @author Elena Quarantotto - TILAB
 * @author Giovanni Caire - TILAB
 */
public class TwoPh0Initiator extends Initiator {
  // Data store keys 
	// Private data store keys (can't be static since if we register another instance of this class as state of the FSM 
	// using the same data store the new values overrides the old one.
  /** 
     key to retrieve from the DataStore of the behaviour the ACLMessage 
     object passed in the constructor of the class.
   */
  public final String CFP_KEY = INITIATION_K;
  /** 
     key to retrieve from the DataStore of the behaviour the vector of
     CFP messages that have to be sent.
   */
  public final String ALL_CFPS_KEY = ALL_INITIATIONS_K;
  /** 
     key to retrieve from the DataStore of the behaviour the last
     ACLMessage object that has been received (null if the timeout
     expired). 
   */
  public final String REPLY_KEY = REPLY_K;
  /** 
     key to retrieve from the DataStore of the behaviour the vector of
     all messages that have been received as response.
   */
  public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
  /** 
     key to retrieve from the DataStore of the behaviour the vector of
     PROPOSE messages that have been received as response.
   */
  public final String ALL_PROPOSES_KEY = "__all-proposes" + hashCode();
  /** 
     key to retrieve from the DataStore of the behaviour the vector of
     CFP messages for which a response has not been received yet.
   */
  public final String ALL_PENDINGS_KEY = "__all-pendings" + hashCode();
    
    /* FSM states names */
    private static final String HANDLE_PROPOSE = "Handle-Propose";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    
    private static final int ALL_RESPONSES_RECEIVED = 1;
    
    /* Unique conversation Id */
    //private String conversationId = null;
    /* Cfps messages still pending (i.e. for which it doesn't still received a response */
    //private Vector ph0Pendings = new Vector();
    /* Data store output key */
    private String outputKey = null;
    private int totSessions;

    private boolean logging = true; /**@todo REMOVE IT!!!!!!!!!!! */
    private boolean currentLogging = true; /**@todo REMOVE IT!!!!!!!!!!! */

    /**
       Constructs a <code>TwoPh0Initiator</code> behaviour.
       @param a The agent performing the protocol.
       @param cfp The message that must be used to initiate the protocol.
       Notice that the default implementation of the <code>prepareCfps</code> method
       returns an array composed of that message only.
       @param outputKey Data store key where the behaviour will store the Vector 
       of messages to be sent to initiate the successive phase.
     */
    public TwoPh0Initiator(Agent a, ACLMessage cfp, String outputKey) {
        this(a, cfp, outputKey, new DataStore());
    }

    /**
       Constructs a <code>TwoPh0Initiator</code> behaviour.
       @param a The agent performing the protocol.
       @param cfp The message that must be used to initiate the protocol.
       Notice that the default implementation of the <code>prepareCfps</code> method
       returns an array composed of that message only.
       @param outputKey Data store key where the behaviour will store the Vector 
       of messages to be sent to initiate the successive phase.
       @param store <code>DataStore</code> that will be used by this <code>TwoPh0Initiator</code>.
     */
    public TwoPh0Initiator(Agent a, ACLMessage cfp, String outputKey, DataStore store) {
        super(a, cfp, store);
        //this.conversationId = conversationId;
        this.outputKey = outputKey;
        /* Register the FSM transitions specific to the Two-Phase0-Commit protocol */
        registerTransition(CHECK_IN_SEQ, HANDLE_PROPOSE, ACLMessage.PROPOSE);
        registerDefaultTransition(HANDLE_PROPOSE, CHECK_SESSIONS);
        /* update1
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_PROPOSE);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, SOME_FAILURE);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, PH0_TIMEOUT_EXPIRED);
        */
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED); // update1
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        // Create and register the states specific to the Two-Phase0-Commit protocol */
        Behaviour b = null;

        // HANDLE_PROPOSE 
        // This state is activated when a propose message is received as a reply
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage propose = (ACLMessage) getDataStore().get(REPLY_KEY);
                handlePropose(propose);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_PROPOSE);

        // HANDLE_ALL_RESPONSES 
        // This state is activated when all the responsess have been 
        // received or the specified timeout has expired.
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                Vector responses = (Vector) getDataStore().get(ALL_RESPONSES_KEY);
                Vector proposes = (Vector) getDataStore().get(ALL_PROPOSES_KEY);
                Vector pendings = (Vector) getDataStore().get(ALL_PENDINGS_KEY);
                Vector nextPhMsgs = (Vector) getDataStore().get(TwoPh0Initiator.this.outputKey);
                /*Logger.log("\n\n(TwoPh0Initiator, TwoPh0Initiator(), HANDLE_ALL_RESPONSES, " + myAgent.getName() + "): " +
                        "proposes = " + proposes.size() + " failures = " + failures.size() +
                        " ph0pendings = " + ph0Pendings.size() + " responses = " + nextPhMsgs.size(), logging);
                Logger.log("\n\n(TwoPh0Initiator, TwoPh0Initiator(), HANDLE_ALL_RESPONSES, " + myAgent.getName() + "): " +
                        "proposes = " + proposes, logging);
                Logger.log("\n\n(TwoPh0Initiator, TwoPh0Initiator(), HANDLE_ALL_RESPONSES, " + myAgent.getName() + "): " +
                        "failures = " + failures, logging);
                Logger.log("\n\n(TwoPh0Initiator, TwoPh0Initiator(), HANDLE_ALL_RESPONSES, " + myAgent.getName() + "): " +
                        "ph0Pendings = " + ph0Pendings, logging);
                Logger.log("\n\n(TwoPh0Initiator, TwoPh0Initiator(), HANDLE_ALL_RESPONSES, " + myAgent.getName() + "): " +
                        "nextPhMsgs = " + nextPhMsgs, logging);*/
                handleAllResponses(responses, proposes, pendings, nextPhMsgs);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_ALL_RESPONSES);

        /* DUMMY_FINAL state returns ALL_PROPOSE, SOME_FAILURE or
        PH0_TIMEOUT_EXPIRED code. 
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                Logger.log("(TwoPh0Initiator, TwoPh0Initiator(), DUMMY_FINAL, " + myAgent.getName() + "): " +
                        "dummy started", logging);
                super.onStart();
            }
            public void action() {
            }
            public int onEnd() {
                int result;
                Vector responses = (Vector) getDataStore().get(TwoPh0Initiator.this.outputKey); // update1
                Logger.log("(TwoPh0Initiator, TwoPh0Initiator(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                    "responses = " + responses, currentLogging);
                // update1 return getState(CHECK_SESSIONS).onEnd();
                // fix:
                if(responses.size() != 0)
                    result = ((ACLMessage) responses.get(0)).getPerformative(); // update1
                else
                    result = 0;
                Logger.log("(TwoPh0Initiator, TwoPh0Initiator(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                    "return " + result, currentLogging);
                return result;
            }
        };
        b.setDataStore(getDataStore());
        registerLastState(b, DUMMY_FINAL);*/
    }
    
    public int onEnd() {
      Vector nextPhMsgs = (Vector) getDataStore().get(outputKey);
      if (nextPhMsgs.size() != 0) {
        return ((ACLMessage) nextPhMsgs.get(0)).getPerformative();
      }
      else {
      	return -1;
      }
    }

	private String[] toBeReset = null;
		
  /**
   */
  protected String[] getToBeReset() {
  	if (toBeReset == null) {
			toBeReset = new String[] {
				HANDLE_PROPOSE, 
				HANDLE_NOT_UNDERSTOOD,
				HANDLE_FAILURE,
				HANDLE_OUT_OF_SEQ
			};
  	}
  	return toBeReset;
  }
    
    /* User can override these methods */

    /**
     * This method must return the vector of ACLMessage objects to be sent.
     * It is called in the first state of this protocol. This default
     * implementation just returns the ACLMessage object (a CFP) passed in
     * the constructor. Programmers might prefer to override this method in order
     * to return a vector of CFP objects for 1:N conversations.
     * @param cfp the ACLMessage object passed in the constructor
     * @return a vector of ACLMessage objects. The value of the <code>reply-with</code>
     * slot is ignored and regenerated automatically by this
     * class. Instead user can specify <code>reply-by</code> slot representing phase0
     * timeout.
     */
    protected Vector prepareCfps(ACLMessage cfp) {
        Vector v = new Vector(1);
        v.addElement(cfp);
        return v;
    }

    /**
     * This method is called every time a <code>propose</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param propose the received propose message
     */
    protected void handlePropose(ACLMessage propose) {
    }

    /**
     * This method is called when all the responses have been collected or when
     * the timeout is expired. The used timeout is the minimum value of the slot
     * <code>reply-By</code> of all the sent messages. By response message we
     * intend here all the <code>propose, failure, not-understood</code> received messages, which
     * are not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override this method to modify the Vector of initiation messages 
     * (<code>nextPhMsgs</code>) for next phase. More in details this Vector 
     * already includes messages with the performative set according to the 
     * default protocol rules i.e. QUERY_IF (if all responders replied with 
     * PROPOSE) or REJECT_PROPOSAL (if at least one responder failed or didn't reply).
     * In particular, by setting the <code>reply-by</code> slot, users can 
     * specify a timeout for next phase.
     * @param responses The Vector of all messages received as response
     * @param proposes The Vector of PROPOSE messages received as response
     * @param pendings The Vector of CFP messages for which a response has not 
     * been received yet
     * @param nextPhMsgs The Vector of initiation messages for next phase already
     * filled with <code>QUERY_IF</code> messages (if all responders replied with 
     * <code>PROPOSE</code>) or <code>REJECT_PROPOSAL</code> (if at least one 
     * responder failed or didn't reply). 
     */
    protected void handleAllResponses(Vector responses, Vector proposes,
                                      Vector pendings, Vector nextPhMsgs) {
    }

    /** This method allows to register a user-defined <code>Behaviour</code> in the
     * PREPARE_CFPS state. This behaviour would override the homonymous method. This
     * method also set the data store of the registered <code>Behaviour</code> to the
     * DataStore of this current behaviour. It is responsibility of the registered
     * behaviour to put the <code>Vector</code> of ACLMessage objects to be sent into
     * the datastore at the <code>ALL_CFPS_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareCfps(Behaviour b) {
        registerPrepareInitiations(b);
    }

    /** This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_PROPOSE state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>propose</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandlePropose(Behaviour b) {
        registerState(b, HANDLE_PROPOSE);
        b.setDataStore(getDataStore());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_ALL_RESPONSES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code> to
     * the DataStore of this current behaviour. The registered behaviour can retrieve
     * the vector of ACLMessage proposes, failures, pending and responses from the
     * datastore at <code>ALL_PROPOSES_KEY</code>, <code>ALL_FAILURES_KEY</code>,
     * <code>ALL_PH0_PENDINGS_KEY</code> and <code>output</code> field.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResponses(Behaviour b) {
        registerState(b, HANDLE_ALL_RESPONSES);
        b.setDataStore(getDataStore());
    }

    /* User CAN'T override these methods */

    /**
     * Prepare vector containing cfps.
     * @param initiation cfp passed in the constructor
     * @return Vector of cfps
     */
    protected final Vector prepareInitiations(ACLMessage initiation) {
        return prepareCfps(initiation);
    }

    /**
     * This method sets for all prepared cfps <code>conversation-id</code> slot (with
     * value passed in the constructor), <code>protocol</code> slot and
     * <code>reply-with</code> slot with a unique value
     * constructed by concatenating receiver's agent name and phase number (i.e. 0).
     * After that it sends all cfps.
     * @param initiations vector prepared in PREPARE_CFPS state
     */
    protected final void sendInitiations(Vector initiations) {
      long currentTime = System.currentTimeMillis();
      long minTimeout = -1;
      long deadline = -1;
        
      Vector pendings = new Vector();
			String conversationID = createConvId(initiations);
			replyTemplate = MessageTemplate.MatchConversationId(conversationID);

		  totSessions = 0; // counter of sessions
      for(Enumeration e = initiations.elements(); e.hasMoreElements();) {
          ACLMessage msg = (ACLMessage) e.nextElement();
          if (msg != null) {
              for(Iterator receivers = msg.getAllReceiver(); receivers.hasNext();) {
                  ACLMessage toSend = (ACLMessage) msg.clone();
                  //toSend.setProtocol(JADE_TWO_PHASE_COMMIT);
                  toSend.setConversationId(conversationID);
                  toSend.clearAllReceiver();
                  AID r = (AID) receivers.next();
                  toSend.addReceiver(r);
									String sessionKey = "R" + hashCode()+  "_" + Integer.toString(totSessions) + "_PH0";
                  //String sessionKey = "R_" + r.getName() + "_PH0";
                  toSend.setReplyWith(sessionKey);
                  /* Creates an object Session for all receivers */
                  sessions.put(sessionKey, new Session());
                  /* If initiator coincides with receiver */
                  adjustReplyTemplate(toSend);
                  myAgent.send(toSend);
                  pendings.add(toSend);
                  totSessions++;
              }
              
					    // Update the timeout (if any) used to wait for replies according
					    // to the reply-by field: get the miminum.
              Date d = msg.getReplyByDate();
              if(d != null) {
                  long timeout = d.getTime() - currentTime;
                  if(timeout > 0 && (timeout < minTimeout || minTimeout <= 0)) {
                      minTimeout = timeout;
                      deadline = d.getTime();
                  }
              }
          }
      }
		  // Finally set the MessageTemplate and timeout used in the RECEIVE_REPLY 
		  // state to accept replies, and store the Vector of pending CFPs
      replyReceiver.setTemplate(replyTemplate);
      replyReceiver.setDeadline(deadline);
      getDataStore().put(ALL_PENDINGS_KEY, pendings);
    }

    /**
     * Check whether a reply is in-sequence and than update the appropriate Session
     * and removes corresponding cfp from vector of pendings.
     * @param reply message received
     * @return true if reply is compliant with flow of protocol, false otherwise
     */
    protected final boolean checkInSequence(ACLMessage reply) {
      System.out.println("**** Received message "+ACLMessage.getPerformative(reply.getPerformative()));
      boolean ret = false;
      String inReplyTo = reply.getInReplyTo();
      Session s = (Session) sessions.get(inReplyTo);
      if(s != null) {
        int perf = reply.getPerformative();
        if(s.update(perf)) {
          // The reply is compliant to the protocol
          ((Vector) getDataStore().get(ALL_RESPONSES_KEY)).add(reply);
          if (perf == ACLMessage.PROPOSE) {
        		((Vector) getDataStore().get(ALL_PROPOSES_KEY)).add(reply);
          }
          updatePendings(inReplyTo);
          
          /*switch(perf) {
              case ACLMessage.PROPOSE: {
                  Logger.log("\n\n(TwoPh0Initiator, checkInSequence(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                      "before add proposes = " + getDataStore().get(ALL_PROPOSES_KEY), logging);
                  ((Vector) getDataStore().get(ALL_PROPOSES_KEY)).add(reply);
                  Logger.log("(TwoPh0Initiator, checkInSequence(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                      "PROPOSE message", logging);
                  Logger.log("\n\n(TwoPh0Initiator, checkInSequence(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                      "after add proposes = " + getDataStore().get(ALL_PROPOSES_KEY), logging);
                  break;
              }
              case ACLMessage.FAILURE: {
                  ((Vector) getDataStore().get(ALL_FAILURES_KEY)).add(reply);
                  Logger.log("(TwoPh0Initiator, checkInSequence(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                      "FAILURE message", logging);
                  break;
              }
          }
          Logger.log("(TwoPh0Initiator, checkInSequence(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                  "reply = " + reply.getInReplyTo(), logging);*/
          ret = true;
        }
        if(s.isCompleted()) {
          sessions.remove(inReplyTo);
        }
      }
      return ret;
    }

    private void updatePendings(String key) {
    	Vector pendings = (Vector) getDataStore().get(ALL_PENDINGS_KEY);
      for(int i=0; i<pendings.size(); i++) {
          ACLMessage pendingMsg = (ACLMessage) pendings.get(i);
          if(pendingMsg.getReplyWith().equals(key)) {
              pendings.remove(i);
              break;
          }
      }
    }
    	
    /**
     * Check if there are still active sessions or if timeout is expired.
     * @param reply last message received
     * @return ALL_RESPONSES_RECEIVED or -1 (still active sessions)
     */
    protected final int checkSessions(ACLMessage reply) {
    	if (reply == null) {
    		// Timeout expired --> clear all sessions 
    		sessions.clear();
    	}
    	if (sessions.size() == 0) {
    		// We have finished --> fill the Vector of initiation messages for next 
    		// phase (unless already filled by the user)
        DataStore ds = getDataStore();
    		Vector nextPhMsgs = (Vector) ds.get(outputKey);
    		if (nextPhMsgs.size() == 0) {
	        Vector proposes = (Vector) ds.get(ALL_PROPOSES_KEY);
	        Vector pendings = (Vector) ds.get(ALL_PENDINGS_KEY);
	        fillNextPhInitiations(nextPhMsgs, proposes, pendings);
    		}
        return ALL_RESPONSES_RECEIVED;
    	}
    	else {
    		// We are still waiting for some responses
    		return -1;
    	}
        /*Logger.log("\n\n(TwoPh0Initiator, checkSessions(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
            "before proposes = " + getDataStore().get(ALL_PROPOSES_KEY), currentLogging);
        int ret = ALL_RESPONSES_RECEIVED; // update1
        Vector nextPhMsgs = (Vector) getDataStore().get(outputKey);
        if(reply != null) {
            if(sessions.size() > 0) {
                // We are still waiting for some responses --> the protocol has not completed yet
                ret = -1;
                Logger.log("(TwoPh0Initiator, checkSessions(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                        "still active sessions, ret = -1", currentLogging);
            } 
            else {
                // All responses have been received
                Vector proposes = (Vector) getDataStore().get(ALL_PROPOSES_KEY);
                if(((Vector) getDataStore().get(ALL_FAILURES_KEY)).size() == 0) {
                		// All responders replied with PROPOSE --> Fill the vector 
                		// of initiation messages for next phase with QUERY_IF
                    for(int i=0; i<proposes.size(); i++) {
                        ACLMessage msg = (ACLMessage) proposes.get(i);
                        ACLMessage queryIf = msg.createReply();
                        queryIf.setPerformative(ACLMessage.QUERY_IF);
                        nextPhMsgs.add(queryIf);
                    }
                }
                else {
                		// At least one responder replied with FAILURE or NOT_UNDERSTOOD 
                		// --> Fill the vector of initiation messages for next phase with REJECT_PROPOSAL
                    for(int i=0; i<proposes.size(); i++) {
                        ACLMessage msg = (ACLMessage) proposes.get(i);
                        ACLMessage reject = msg.createReply();
                        reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        responses.add(reject);
                    }
                }
            }
        }
        else {
            // Timeout was expired or we were interrupted, so clear all remaining
            //sessions, prepare vector containing reject messages stored in the datastore
            //at outputKey and returns PH0_TIMEOUT_EXPIRED. Receivers of reject messages
            //are all proposes or pendings. Content of reject messages is replaced by
            //the user during handleAllResponses method call. 
            sessions.clear();
            // fix: va bene creare un msg con createReply ai proposes, ma non va bene
            //farlo con le cfp.
            //Vector proposesAndPendings = (Vector) getDataStore().get(ALL_PROPOSES_KEY);
            //Vector proposesAndPendings = new Vector((Vector) getDataStore().get(ALL_PROPOSES_KEY));
            //proposesAndPendings.addAll(ph0Pendings);
            //for(int i=0; i<proposesAndPendings.size(); i++) {
            //    ACLMessage msg = (ACLMessage) proposesAndPendings.get(i);
            //    ACLMessage reject = msg.createReply();
            //    reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
            //    responses.add(reject);
            //} 
            Vector proposes = (Vector) getDataStore().get(ALL_PROPOSES_KEY);
            for(int i=0; i<proposes.size(); i++) {
                ACLMessage msg = (ACLMessage) proposes.get(i);
                ACLMessage reject = msg.createReply();
                reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                responses.add(reject);
            }
            for(int i=0; i<ph0Pendings.size(); i++) {
                ACLMessage msg = (ACLMessage) ph0Pendings.get(i);
                ACLMessage reject = (ACLMessage) msg.clone();
                reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                responses.add(reject);
            }
            // update1 ret = PH0_TIMEOUT_EXPIRED;
        }
        Logger.log("\n\n(TwoPh0Initiator, checkSessions(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
            "after proposes = " + getDataStore().get(ALL_PROPOSES_KEY), logging);
        Logger.log("\n\n(TwoPh0Initiator, checkSessions(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                "ret (ALL_PROPOSE = 1, SOME_FAILURE = 2, PH0_TIMEOUT_EXPIRED = -1001, -1) = " + ret, logging);
        return ret;*/
    }
    
    private void fillNextPhInitiations(Vector nextPhMsgs, Vector proposes, Vector pendings) {
    	if (proposes.size() == totSessions) {
    		// All responders replied with PROPOSE --> Fill the vector 
    		// of initiation messages for next phase with QUERY_IF
        for(int i=0; i<proposes.size(); i++) {
          ACLMessage msg = (ACLMessage) proposes.get(i);
          ACLMessage queryIf = msg.createReply();
          queryIf.setPerformative(ACLMessage.QUERY_IF);
          nextPhMsgs.add(queryIf);
        }
    	}
    	else {
    		// At least one responder failed or didn't reply --> Fill the vector 
    		// of initiation messages for next phase with REJECT_PROPOSALS
        for(int i=0; i<proposes.size(); i++) {
          ACLMessage msg = (ACLMessage) proposes.get(i);
          ACLMessage reject = msg.createReply();
          reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
          nextPhMsgs.add(reject);
        }
        for(int i=0; i<pendings.size(); i++) {
          ACLMessage msg = (ACLMessage) pendings.get(i);
          ACLMessage reject = (ACLMessage) msg.clone();
          reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
          nextPhMsgs.add(reject);
        }
    	}
    }
    		
    		

    /**
     * Initialize the data store.
     * @param msg Message passed in the constructor
     */
    protected void initializeDataStore(ACLMessage msg) {
        super.initializeDataStore(msg);
        getDataStore().put(ALL_RESPONSES_KEY, new Vector());
        getDataStore().put(ALL_PROPOSES_KEY, new Vector());
        getDataStore().put(outputKey, new Vector());
    }

    public void reset(ACLMessage cfp) {
        super.reset(cfp);
    }

    /*public String getConversationId() {
        return this.conversationId;
    }*/

    /**
     * Inner class Session
     */
    class Session implements Serializable {
        // Session states 
        static final int INIT = 0;
        static final int REPLY_RECEIVED = 1;

        private int state = INIT;

        /**
         * Return true if received ACLMessage is consistent with the protocol.
         * @param perf
         * @return Return true if received ACLMessage is consistent with the protocol
         */
        public boolean update(int perf) {
            if(state == INIT) {
                switch(perf) {
                    case ACLMessage.PROPOSE:
                    case ACLMessage.FAILURE:
                    case ACLMessage.NOT_UNDERSTOOD:
                      state = REPLY_RECEIVED;
	                    return true;
                    default: 
                    	return false;
                }
            }
            else {
                return false;
            }
        }

        public int getState() {
            return state;
        }

        public boolean isCompleted() {
            return (state == REPLY_RECEIVED);
        }
    }
}

