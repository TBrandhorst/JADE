/*
  $Log$
  Revision 1.12  1998/10/11 19:20:14  rimassa
  Changed code to comply with new MessageDispatcher constructor.
  Implemented invalidateCacheEntry() remote method.
  New name clash exception handled properly.
  Implemented a cache refresh on communication failures: when a cached
  AgentDescriptor results in a RemoteException, main Agent Platform is
  called to update remote agent cache and retry with the newer RMI
  object reference before giving up for good with a NotFoundException.

  Revision 1.11  1998/10/05 20:13:51  Giovanni
  Made every agent name table is case insensitive, according to FIPA
  specification.

  Revision 1.10  1998/10/04 18:00:57  rimassa
  Added a 'Log:' field to every source file.
  */

package jade.core;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import jade.lang.acl.*;

/***********************************************************************************

  Name: AgentContainerImpl

  Responsibilities and Collaborations:

  + Creates agents on the local Java VM, and starts the message dispatcher.
    (Agent, MessageDispatcher)

  + Connects with each newly created agent, to allow event-based
    interaction between the two.
    (Agent, MessageDispatcher)

  + Routes outgoing messages to the suitable message dispatcher, caching
    remote agent addresses.
    (Agent, AgentDescriptor, MessageDispatcher)

  + Holds an RMI object reference for the agent platform, used to
    retrieve the addresses of unknown agents.
    (AgentPlatform, MessageDispatcher)


**************************************************************************************/
public class AgentContainerImpl extends UnicastRemoteObject implements AgentContainer, CommListener {

  private static final int MAP_SIZE = 20;
  private static final float MAP_LOAD_FACTOR = 0.50f;

  // Local agents, indexed by agent name
  protected Hashtable localAgents = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  // Remote agents cache, indexed by agent name
  private Hashtable remoteAgentsCache = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  // The message dispatcher of this container
  protected MessageDispatcherImpl myDispatcher;

  // The agent platform this container belongs to
  private AgentPlatform myPlatform;

  // IIOP address of the platform, will be used for inter-platform communications
  protected String platformAddress;


  public AgentContainerImpl() throws RemoteException {
    myDispatcher = new MessageDispatcherImpl(this, localAgents);
  }


  // Interface AgentContainer implementation


  public void invalidateCacheEntry(String key) throws RemoteException {
    remoteAgentsCache.remove(key);
  }


  public void joinPlatform(String platformRMI, String platformIIOP, Vector agentNamesAndClasses) {

    Agent agent = null;
    AgentDescriptor desc = new AgentDescriptor();

    platformAddress = platformIIOP;


    // Retrieve agent platform from RMI registry and register as agent container
    try {
      myPlatform = (AgentPlatform) Naming.lookup(platformRMI);
      myPlatform.addContainer(this); // RMI call
    }
    catch(RemoteException re) {
      System.err.println("Communication failure while contacting agent platform.");
      re.printStackTrace();
    }
    catch(Exception e) {
      System.err.println("Some problem occurred while contacting agent platform.");
      e.printStackTrace();
    }

    /* Create all agents and set up necessary links for message passing.

       The link chain is:
       a) Agent 1 -> AgentContainer1 -- Through CommEvent
       b) AgentContainer 1 -> MessageDispatcher 2 -- Through RMI (cached or retreived from AgentPlatform)
       c) MessageDispatcher 2 -> Agent 2 -- Through postMessage() (direct insertion in message queue, no events here)

       agentNamesAndClasses is a Vector of String containing, orderly, the name of an agent and the name of Agent
       concrete subclass which implements that agent.
    */
    for( int i=0; i < agentNamesAndClasses.size(); i+=2 ) {
      String agentName = (String)agentNamesAndClasses.elementAt(i);
      String agentClass = (String)agentNamesAndClasses.elementAt(i+1);

      System.out.println("new agent: " + agentName + " : " + agentClass);

      try {
	agent = (Agent)Class.forName(new String(agentClass)).newInstance();
      }
      catch(ClassNotFoundException cnfe) {
	System.err.println("Class " + agentClass + " for agent " + agentName + " was not found.");
	continue;
      }
      catch( Exception e ){
	e.printStackTrace();
      }

      // Subscribe as a listener for the new agent
      agent.addCommListener(this);

      // Insert new agent into local agents table
      localAgents.put(agentName.toLowerCase(),agent);

      desc.setDemux(myDispatcher);

      try {
	myPlatform.bornAgent(agentName, desc); // RMI call
      }
      catch(NameClashException nce) {
	System.out.println("Agent name already in use");
	nce.printStackTrace();
      }
      catch(RemoteException re) {
	System.out.println("Communication error while adding a new agent to the platform.");
	re.printStackTrace();
      }
    }

    // Now activate all agents (this call starts their embedded threads)
    Enumeration nameList = localAgents.keys();
    String currentName = null;
    while(nameList.hasMoreElements()) {
      currentName = (String)nameList.nextElement();
      agent = (Agent)localAgents.get(currentName.toLowerCase());
      agent.doStart(currentName, platformAddress);
    }
  }

  public void shutDown() {
    Enumeration agentNames = localAgents.keys();

    try {

      // Remove all agents
      while(agentNames.hasMoreElements()) {
	String name = (String)agentNames.nextElement();
	Agent a = (Agent)localAgents.get(name);
	localAgents.remove(name);
	a.doDelete();
	myPlatform.deadAgent(name); // RMI call
      }

      // Deregister itself as a container
      myPlatform.removeContainer(this); // RMI call
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
  }

  protected void finalize() {
    shutDown();
  }

  public void CommHandle(CommEvent event) {

    // Get ACL message from the event.
    ACLMessage msg = event.getMessage();

    // FIXME: Must use multicast also when more than a recipient is
    // present in ':receiver' field.
    if(event.isMulticast()) {
      AgentGroup group = event.getRecipients();
      while(group.hasMoreMembers()) {
	msg.setDest(group.getNextMember());
	unicastPostMessage(msg);
      }
    }
    else
      unicastPostMessage(msg);
  }

  private void unicastPostMessage(ACLMessage msg) {
    String receiverName = msg.getDest();

    // Look up in local agents.
    Agent receiver = (Agent)localAgents.get(receiverName.toLowerCase());

    if(receiver != null)
      receiver.postMessage(msg);
    else
      // Search failed; look up in remote agents.
      postRemote(msg, receiverName);

    // If it fails again, ask the Agent Platform.
    // If still fails, raise NotFound exception.
  }

  private void postRemote(ACLMessage msg, String receiverName) {

    // Look first in descriptor cache
    AgentDescriptor desc = (AgentDescriptor)remoteAgentsCache.get(receiverName.toLowerCase());
    try {

      if(desc == null) { // Cache miss :-( . Ask agent platform and update agent cache
	desc = myPlatform.lookup(receiverName); // RMI call
	remoteAgentsCache.put(receiverName.toLowerCase(),desc);  // FIXME: The cache grows indefinitely ...
      }
      MessageDispatcher md = desc.getDemux();
      try {
	md.dispatch(msg); // RMI call
      }
      catch(RemoteException re) { // Communication error: retry with a newer object reference from the platform
	remoteAgentsCache.remove(receiverName.toLowerCase());
	desc = myPlatform.lookup(receiverName); // RMI call
	remoteAgentsCache.put(receiverName.toLowerCase(),desc);  // FIXME: The cache grows indefinitely ...
	md = desc.getDemux();
	md.dispatch(msg); // RMI call
      }

    }
    catch(NotFoundException nfe) {
      System.err.println("Agent was not found on agent platform");
      nfe.printStackTrace();
    }
    catch(RemoteException re) {
      System.out.println("Communication error while contacting agent platform");
      re.printStackTrace();
    }
  }

}
