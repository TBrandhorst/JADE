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

package jade.tools.rma;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.InputStreamReader;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import jade.util.leap.Iterator;
import java.net.URL;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;
import jade.domain.mobility.*;
import jade.domain.FIPANames;
import jade.gui.AgentTreeModel;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.onto.basic.Result;
import jade.content.onto.basic.Action;

import jade.proto.SimpleAchieveREInitiator;

import jade.tools.ToolAgent;


/**
  <em>Remote Management Agent</em> agent. This class implements
  <b>JADE</b> <em>RMA</em> agent. <b>JADE</b> applications cannot use
  this class directly, but interact with it through <em>ACL</em>
  message passing. Besides, this agent has a <em>GUI</em> through
  which <b>JADE</b> Agent Platform can be administered.


  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class rma extends ToolAgent {

  private APDescription myPlatformProfile;

  // Sends requests to the AMS
    private class AMSClientBehaviour extends SimpleAchieveREInitiator {

    private String actionName;

	public AMSClientBehaviour(String an, ACLMessage request) {
	    super(rma.this, request);
	    actionName = an;
	}


    protected void handleNotUnderstood(ACLMessage reply) {
      myGUI.showErrorDialog("NOT-UNDERSTOOD received by RMA during " + actionName, reply);
    }

    protected void handleRefuse(ACLMessage reply) {
      myGUI.showErrorDialog("REFUSE received during " + actionName, reply);
    }

    protected void handleAgree(ACLMessage reply) {
	//System.out.println("AGREE received"+reply);
    }

    protected void handleFailure(ACLMessage reply) {
      myGUI.showErrorDialog("FAILURE received during " + actionName, reply);
    }

    protected void handleInform(ACLMessage reply) {
	//System.out.println("INFORM received"+reply);
    }

  } // End of AMSClientBehaviour class


  private class handleAddRemotePlatformBehaviour extends AMSClientBehaviour{

    	public handleAddRemotePlatformBehaviour(String an, ACLMessage request){
    		super(an,request);

    	}

    	protected void handleInform(ACLMessage msg){
    		//System.out.println("arrived a new APDescription");
    		try{
    			AID sender = msg.getSender();
    			Result r =(Result)getContentManager().extractContent(msg);

    			Iterator i = r.getItems().iterator();
    			APDescription APDesc = (APDescription)i.next();
    			if(APDesc != null){
    			myGUI.addRemotePlatformFolder();
    			myGUI.addRemotePlatform(sender,APDesc);}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}

    }//end handleAddRemotePlatformBehaviour

    private class handleRefreshRemoteAgentBehaviour extends AMSClientBehaviour{

    	private APDescription platform;

    	public handleRefreshRemoteAgentBehaviour(String an, ACLMessage request,APDescription ap){
    		super(an,request);
    		platform = ap;

    	}

    	protected void handleInform(ACLMessage msg){
	    //System.out.println("arrived a new agents from a remote platform");
    		try{
    			AID sender = msg.getSender();
    			Result r = (Result)getContentManager().extractContent(msg);
    			Iterator i = r.getItems().iterator();
    			myGUI.addRemoteAgentsToRemotePlatform(platform,i);
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}

    }//end handleAddRemotePlatformBehaviour


  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  private transient MainWindow myGUI;

  private String myContainerName;

  class RMAAMSListenerBehaviour extends AMSListenerBehaviour {
      protected void installHandlers(Map handlersTable) {

	// Fill the event handler table.
	handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, new EventHandler() {
	  public void handle(Event ev) {
	    AddedContainer ac = (AddedContainer)ev;
	    ContainerID cid = ac.getContainer();
	    String name = cid.getName();
	    String address = cid.getAddress();
	    try {
	      InetAddress addr = InetAddress.getByName(address);
	      myGUI.addContainer(name, addr);
	    }
	    catch(UnknownHostException uhe) {
	      myGUI.addContainer(name, null);
	    }
	  }
	});


	handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, new EventHandler() {
	  public void handle(Event ev) {
	    RemovedContainer rc = (RemovedContainer)ev;
	    ContainerID cid = rc.getContainer();
	    String name = cid.getName();
	    myGUI.removeContainer(name);
	  }
	});

	handlersTable.put(IntrospectionVocabulary.BORNAGENT, new EventHandler() {
          public void handle(Event ev) {
	    BornAgent ba = (BornAgent)ev;
	    ContainerID cid = ba.getWhere();
	    String container = cid.getName();
	    AID agent = ba.getAgent();
	    myGUI.addAgent(container, agent);
	    myGUI.modifyAgent(container, agent, ba.getState(), ba.getOwnership());
	    if (agent.equals(getAID()))
	      myContainerName = container;
	  }
	});

        handlersTable.put(IntrospectionVocabulary.DEADAGENT, new EventHandler() {
          public void handle(Event ev) {
	    DeadAgent da = (DeadAgent)ev;
	    ContainerID cid = da.getWhere();
	    String container = cid.getName();
	    AID agent = da.getAgent();
	    myGUI.removeAgent(container, agent);
	  }
        });

        handlersTable.put(IntrospectionVocabulary.SUSPENDEDAGENT, new EventHandler() {
          public void handle(Event ev) {
      	    SuspendedAgent sa = (SuspendedAgent)ev;
      	    ContainerID cid = sa.getWhere();
      	    String container = cid.getName();
      	    AID agent = sa.getAgent();
      	    myGUI.modifyAgent(container, agent, AMSAgentDescription.SUSPENDED, null);
	        }
        });

        handlersTable.put(IntrospectionVocabulary.RESUMEDAGENT, new EventHandler() {
          public void handle(Event ev) {
      	    ResumedAgent ra = (ResumedAgent)ev;
      	    ContainerID cid = ra.getWhere();
      	    String container = cid.getName();
      	    AID agent = ra.getAgent();
      	    myGUI.modifyAgent(container, agent, AMSAgentDescription.ACTIVE, null);
	        }
        });

        handlersTable.put(IntrospectionVocabulary.CHANGEDAGENTOWNERSHIP, new EventHandler() {
          public void handle(Event ev) {
          	ChangedAgentOwnership cao = (ChangedAgentOwnership)ev;
      	    ContainerID cid = cao.getWhere();
      	    String container = cid.getName();
      	    AID agent = cao.getAgent();
      	    myGUI.modifyAgent(container, agent, null, cao.getTo());
	        }
        });

        handlersTable.put(IntrospectionVocabulary.MOVEDAGENT, new EventHandler() {
          public void handle(Event ev) {
	    MovedAgent ma = (MovedAgent)ev;
	    AID agent = ma.getAgent();
	    ContainerID from = ma.getFrom();
	    myGUI.removeAgent(from.getName(), agent);
	    ContainerID to = ma.getTo();
	    myGUI.addAgent(to.getName(), agent);
	  }
        });

	handlersTable.put(IntrospectionVocabulary.ADDEDMTP, new EventHandler() {
	  public void handle(Event ev) {
	    AddedMTP amtp = (AddedMTP)ev;
	    String address = amtp.getAddress();
	    ContainerID where = amtp.getWhere();
	    myGUI.addAddress(address, where.getName());
	  }
        });

        handlersTable.put(IntrospectionVocabulary.REMOVEDMTP, new EventHandler() {
	  public void handle(Event ev) {
	    RemovedMTP rmtp = (RemovedMTP)ev;
	    String address = rmtp.getAddress();
	    ContainerID where = rmtp.getWhere();
	    myGUI.removeAddress(address, where.getName());
	  }
	});

	//handle the APDescription provided by the AMS
	handlersTable.put(IntrospectionVocabulary.PLATFORMDESCRIPTION, new EventHandler(){
	  public void handle(Event ev){
	    PlatformDescription pd = (PlatformDescription)ev;
	    APDescription APdesc = pd.getPlatform();
	    myPlatformProfile = APdesc;
	    myGUI.refreshLocalPlatformName(myPlatformProfile.getName());
	  }
        });

      }
  } // END of inner class RMAAMSListenerBehaviour


  /**
   This method starts the <em>RMA</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  protected void toolSetup() {

    // Register the supported ontologies
    getContentManager().registerOntology(MobilityOntology.getInstance());

    // Send 'subscribe' message to the AMS
    AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

    // Handle incoming 'inform' messages
    AMSSubscribe.addSubBehaviour(new RMAAMSListenerBehaviour());

    // Schedule Behaviour for execution
    addBehaviour(AMSSubscribe);

    // Show Graphical User Interface
    myGUI = new MainWindow(this);
    myGUI.ShowCorrect();

  }

  /**
   Cleanup during agent shutdown. This method cleans things up when
   <em>RMA</em> agent is destroyed, disconnecting from <em>AMS</em>
   agent and closing down the platform administration <em>GUI</em>.
  */
  protected void toolTakeDown() {
    send(getCancel());
    if (myGUI != null) {
      // The following call was removed as it causes a threading
      // deadlock with join. Its also not needed as the async
      // dispose will do it.
      // myGUI.setVisible(false);
	  myGUI.disposeAsync();
    }
  }

  protected void beforeClone() {
  }

  protected void afterClone() {
    // Add yourself to the RMA list
    ACLMessage AMSSubscription = getSubscribe();
    AMSSubscription.setSender(getAID());
    send(AMSSubscription);
    myGUI = new MainWindow(this);
    myGUI.ShowCorrect();
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public AgentTreeModel getModel() {
    return myGUI.getModel();
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void newAgent(String agentName, String className, Object arg[], String containerName) {

    CreateAgent ca = new CreateAgent();

    if(containerName.equals(""))
      containerName = AgentManager.MAIN_CONTAINER_NAME;

    ca.setAgentName(agentName);
    ca.setClassName(className);
    ca.setContainer(new ContainerID(containerName, null));
    for(int i = 0; i<arg.length ; i++)
    	ca.addArguments((Object)arg[i]);

    try {
      Action a = new Action();
      a.setActor(getAMS());
      a.setAction(ca);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(JADEManagementOntology.NAME);
      getContentManager().fillContent(requestMsg, a);
      addBehaviour(new AMSClientBehaviour("CreateAgent", requestMsg));
    }
    catch(Exception fe) {
      fe.printStackTrace();
    }

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void suspendAgent(AID name) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setState(AMSAgentDescription.SUSPENDED);
    Modify m = new Modify();
    m.setDescription(amsd);

    try {
      Action a = new Action();
      a.setActor(getAMS());
      a.setAction(m);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(FIPAManagementOntology.NAME);
      getContentManager().fillContent(requestMsg, a);
      addBehaviour(new AMSClientBehaviour("SuspendAgent", requestMsg));
    }
    catch(Exception fe) {
      fe.printStackTrace();
    }
  }



  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void suspendContainer(String name) {
    // FIXME: Not implemented
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void resumeAgent(AID name) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setState(AMSAgentDescription.ACTIVE);
    Modify m = new Modify();
    m.setDescription(amsd);

    try {
      Action a = new Action();
      a.setActor(getAMS());
      a.setAction(m);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(FIPAManagementOntology.NAME);
      getContentManager().fillContent(requestMsg, a);
      addBehaviour(new AMSClientBehaviour("ResumeAgent", requestMsg));
    }
    catch(Exception fe) {
      fe.printStackTrace();
    }
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void changeAgentOwnership(AID name, String ownership) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setState(AMSAgentDescription.ACTIVE);//SUSPENDED);
    amsd.setOwnership(ownership);
    Modify m = new Modify();
    m.setDescription(amsd);

    try {
      Action a = new Action();
      a.setActor(getAMS());
      a.setAction(m);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(FIPAManagementOntology.NAME);
      getContentManager().fillContent(requestMsg, a);
      addBehaviour(new AMSClientBehaviour("ChangeAgentOwnership", requestMsg));
    }
    catch(Exception fe) {
      fe.printStackTrace();
    }
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void resumeContainer(String name) {
    // FIXME: Not implemented
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void killAgent(AID name) {

    KillAgent ka = new KillAgent();

    ka.setAgent(name);

    try {
      Action a = new Action();
      a.setActor(getAMS());
      a.setAction(ka);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(JADEManagementOntology.NAME);
      getContentManager().fillContent(requestMsg, a);
      addBehaviour(new AMSClientBehaviour("KillAgent", requestMsg));
    }
    catch(Exception fe) {
      fe.printStackTrace();
    }

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void killContainer(String name) {

    KillContainer kc = new KillContainer();

    kc.setContainer(new ContainerID(name, null));

    try {
      Action a = new Action();
      a.setActor(getAMS());
      a.setAction(kc);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(JADEManagementOntology.NAME);
      getContentManager().fillContent(requestMsg, a);
      addBehaviour(new AMSClientBehaviour("KillContainer", requestMsg));
    }
    catch(Exception fe) {
      fe.printStackTrace();
    }

  }

  /**
  Callback method for platform management
  */

  public void moveAgent(AID name, String container)
  {
     MoveAction moveAct = new MoveAction();
     MobileAgentDescription desc = new MobileAgentDescription();
     desc.setName(name);
     ContainerID dest = new ContainerID(container, null);

     desc.setDestination(dest);
     moveAct.setMobileAgentDescription(desc);

      try{
      	Action a = new Action();
     	  a.setActor(getAMS());
     	  a.setAction(moveAct);

		  ACLMessage requestMsg = getRequest();
     	  requestMsg.setOntology(MobilityOntology.NAME);
     	  getContentManager().fillContent(requestMsg, a);
     	  addBehaviour(new AMSClientBehaviour("MoveAgent",requestMsg));

      } catch(Exception fe) {
      	  fe.printStackTrace();
      }
  }

  /**
  Callback method for platform management
  */
  public void cloneAgent(AID name,String newName, String container)
  {
  	CloneAction cloneAct = new CloneAction();
  	MobileAgentDescription desc = new MobileAgentDescription();
  	desc.setName(name);
  	ContainerID dest = new ContainerID(container, null);
  	desc.setDestination(dest);
  	cloneAct.setMobileAgentDescription(desc);
  	cloneAct.setNewName(newName);

  	try{
  		Action a = new Action();
  		a.setActor(getAMS());
  		a.setAction(cloneAct);

  		ACLMessage requestMsg = getRequest();
  		requestMsg.setOntology(MobilityOntology.NAME);
  		getContentManager().fillContent(requestMsg,a);

  		addBehaviour(new AMSClientBehaviour("CloneAgent",requestMsg));

  	} catch(Exception fe) {
  		fe.printStackTrace();
  	}
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void exit() {
    if(myGUI.showExitDialog("Exit this container"))
      killContainer(myContainerName);
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void shutDownPlatform() {
    if(myGUI.showExitDialog("Shut down the platform"))
      killContainer(AgentManager.MAIN_CONTAINER_NAME);
  }

  public void installMTP(String containerName) {
    InstallMTP imtp = new InstallMTP();
    imtp.setContainer(new ContainerID(containerName, null));
    if(myGUI.showInstallMTPDialog(imtp)) {
      try {
		Action a = new Action();
		a.setActor(getAMS());
		a.setAction(imtp);

		ACLMessage requestMsg = getRequest();
		requestMsg.setOntology(JADEManagementOntology.NAME);
		getContentManager().fillContent(requestMsg, a);
		addBehaviour(new AMSClientBehaviour("InstallMTP", requestMsg));
      }
      catch(Exception fe) {
		fe.printStackTrace();
      }

    }
  }

  public void uninstallMTP(String containerName) {
    UninstallMTP umtp = new UninstallMTP();
    umtp.setContainer(new ContainerID(containerName, null));
    if(myGUI.showUninstallMTPDialog(umtp)) {
      uninstallMTP(umtp.getContainer().getName(), umtp.getAddress());
    }
  }

  public void uninstallMTP(String containerName, String address) {
    UninstallMTP umtp = new UninstallMTP();
    umtp.setContainer(new ContainerID(containerName, null));
    umtp.setAddress(address);
    try {
      Action a = new Action();
      a.setActor(getAMS());
      a.setAction(umtp);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(JADEManagementOntology.NAME);
      getContentManager().fillContent(requestMsg, a);
      addBehaviour(new AMSClientBehaviour("UninstallMTP", requestMsg));
    }
    catch(Exception fe) {
      fe.printStackTrace();
    }
  }

  //this method sends a request to a remote AMS to know the APDescription of a remote Platform
  public void addRemotePlatform(AID remoteAMS){
      //System.out.println("AddRemotePlatform"+remoteAMS.toString());
  	try{

  		ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
		requestMsg.setSender(getAID());
    	requestMsg.clearAllReceiver();
    	requestMsg.addReceiver(remoteAMS);
    	//requestMsg.setProtocol("fipa-request");
    	requestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
    	requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		requestMsg.setOntology(FIPAManagementOntology.NAME);

		GetDescription action = new GetDescription();
   		List l = new ArrayList(1);
   		Action a = new Action();
   		a.setActor(remoteAMS);
   		a.setAction(action);

   		getContentManager().fillContent(requestMsg,a);
   		addBehaviour(new handleAddRemotePlatformBehaviour("GetDescription",requestMsg));

  	}catch(Exception e){
  		e.printStackTrace();
  	}
  }


  public void addRemotePlatformFromURL(String url){

  	try{
  		URL AP_URL = new URL(url);
    	BufferedReader in = new BufferedReader(new InputStreamReader(AP_URL.openStream()));

 		StringBuffer buf=new StringBuffer();
     	String inputLine;
 		while( (inputLine = in.readLine()) != null ) {
 	    	if( ! inputLine.equals("") ) {
 				buf.append(inputLine);
 				buf.append(" ");
 	    	}
 		}
	//to parse the APDescription it is put in a Dummy ACLMessage
     	ACLMessage dummyMsg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
     	dummyMsg.setOntology(FIPAManagementOntology.NAME);
     	dummyMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
     	String content = "(( result (action ( agent-identifier :name ams :addresses (sequence IOR:00000000000000) :resolvers (sequence ) ) (get-description ) ) (set " + buf.toString() +" ) ) )";
     	dummyMsg.setContent(content);
     	try{

     	Result r = (Result)getContentManager().extractContent(dummyMsg);

    	Iterator i = r.getItems().iterator();

    	APDescription APDesc = null;

    	while( i.hasNext() && ((APDesc = (APDescription)i.next()) != null) ){
    			String amsName = "ams@" + APDesc.getName();

    			if(amsName.equalsIgnoreCase(getAMS().getName())){
    				System.out.println("ERROR: Action not allowed.");
    			}
    				else
    			{
    				AID ams = new AID(amsName, AID.ISGUID);
    				Iterator TP = (APDesc.getTransportProfile()).getAllAvailableMtps();

    				while(TP.hasNext())
    				{
    					MTPDescription mtp = (MTPDescription)TP.next();
    					Iterator add = mtp.getAllAddresses();
    					while(add.hasNext())
    					{
    						String address = (String)add.next();
    						ams.addAddresses(address);
    					}
    				}
    				myGUI.addRemotePlatformFolder();
    				myGUI.addRemotePlatform(ams,APDesc);
    			}
    	}

     	}catch(Exception e){

     		e.printStackTrace();}
    		in.close();
  		}catch(java.net.MalformedURLException e){
  			e.printStackTrace();
  		}catch(java.io.IOException ioe){
  			ioe.printStackTrace();
  		}
  }

  public void viewAPDescription(String title){
  	myGUI.viewAPDescriptionDialog(myPlatformProfile,title);
  }

  public void viewAPDescription(APDescription remoteAP, String title){
  	myGUI.viewAPDescriptionDialog(remoteAP,title);
  }

  public void removeRemotePlatform(APDescription platform){
  	myGUI.removeRemotePlatform(platform.getName());
  }

  //make a search on a specified ams in order to return
  //all the agents registered with that ams.
  public void refreshRemoteAgent(APDescription platform,AID ams){
  	try{
  		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  		request.setSender(getAID());
  		request.addReceiver(ams);
   		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
                request.setOntology(FIPAManagementOntology.NAME);
  		AMSAgentDescription amsd = new AMSAgentDescription();
  		SearchConstraints constraints = new SearchConstraints();
    	// Build a AMS action object for the request
    	Search s = new Search();
    	s.setDescription(amsd);
    	s.setConstraints(constraints);

    	Action act = new Action();
    	act.setActor(ams);
    	act.setAction(s);

		getContentManager().fillContent(request, act);

		addBehaviour(new handleRefreshRemoteAgentBehaviour ("search",request,platform));

  	}catch(Exception e){
  		e.printStackTrace();
  	}
  }

  // ask the local AMS to register a remote Agent.
  public void registerRemoteAgentWithAMS(AMSAgentDescription amsd){

  	Register register_act = new Register();
  	register_act.setDescription(amsd);

  	try{
  		Action a = new Action();
  		a.setActor(getAMS());
  		a.setAction(register_act);

		ACLMessage requestMsg = getRequest();
  		requestMsg.setOntology(FIPAManagementOntology.NAME);
      	getContentManager().fillContent(requestMsg, a);

      	addBehaviour(new AMSClientBehaviour("Register", requestMsg));

  	}catch(Exception e){e.printStackTrace();}
 }
}
