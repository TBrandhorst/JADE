/******************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2002 TILAB S.p.A.
 *
 * This file is donated by Acklin B.V. to the JADE project.
 *
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * ***************************************************************/
package jade.tools.testagent;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;
import jade.lang.Codec;
import jade.lang.acl.*;
import jade.lang.acl.*;
import jade.lang.sl.*;
import jade.lang.sl.SL0Codec;
import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.OntologyException;


/**
 *  This can be used as test agent for sending and receiving messages to
 *  the Agentplatform and Other agents.
 *  A couple of message templates are included in the menu.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    May 6, 2002
 */

public class TestAgent extends Agent {
  /**
   *  Constructor for the TestAgent
   */
  public TestAgent() {
    super();
  }


  /**
   *  The main program for the TestAgent class
   *
   * @param  args  The command line arguments
   */
  public static void main(String[] args) {
    String host = "cross.hq.acklin.nl";
    String argsv[] = {"-host", host, "-container", "ruurd:jade.tools.testagent.TestAgent"};
    jade.Boot.main(argsv);
  }


  /**
   *  Gets the PlatformRequest attribute of the TestAgent object
   *
   * @return    The PlatformRequest value
   */
  public ACLMessage getPlatformRequest() {
    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    msg.setSender(getAID());
    msg.setEncoding("String");
    msg.setLanguage(SL0Codec.NAME);
    msg.setOntology(FIPAAgentManagementOntology.NAME);
    msg.setProtocol("fipa-request");
    msg.setReplyWith("Req" + (new Date()).getTime());
    msg.setConversationId("Req" + (new Date()).getTime());
    return msg;
  }


  /**
   *  Description of the Method
   */
  public void doRegisterAMS() {

    AMSAgentDescription amsAgentDescription = new AMSAgentDescription();
    amsAgentDescription.setName(getAID());
    amsAgentDescription.setOwnership(getHap());
    amsAgentDescription.setState("ACTIVE");
    Register register = new Register();
    register.set_0(amsAgentDescription);

    ACLMessage msg = createRequestMessage(this, this.getAMS(), register);
    itsFrame.setItsMsg(msg);
  }


  public void doRegisterDF() {

    ServiceDescription serviceDescr = new ServiceDescription();
    serviceDescr.setName("testagent");
    serviceDescr.setOwnership(getHap());
    serviceDescr.setType("jade.testagent");

    DFAgentDescription dFAgentDescription = new DFAgentDescription();
    dFAgentDescription.setName(getAID());
    dFAgentDescription.addOntologies(FIPAAgentManagementOntology.NAME);
    dFAgentDescription.addLanguages(SL0Codec.NAME);
    dFAgentDescription.addServices(serviceDescr);

    Register register = new Register();
    register.set_0(dFAgentDescription);

    ACLMessage msg = createRequestMessage(this, getDefaultDF(), register);
    itsFrame.setItsMsg(msg);
  }


  public void doDeregisterDF() {
    DFAgentDescription dFAgentDescription = new DFAgentDescription();
    dFAgentDescription.setName(getAID());
    Deregister deregister = new Deregister();
    deregister.set_0(dFAgentDescription);

    ACLMessage msg = createRequestMessage(this, getDefaultDF(), deregister);
    itsFrame.setItsMsg(msg);
  }


  public void doDeRegisterAMS() {

    AMSAgentDescription amsAgentDescription = new AMSAgentDescription();
    amsAgentDescription.setName(getAID());
    Deregister deregister = new Deregister();
    deregister.set_0(amsAgentDescription);

    ACLMessage msg = createRequestMessage(this, getAMS(), deregister);
    itsFrame.setItsMsg(msg);
  }


  public void doSearchAMS() {

    AMSAgentDescription amsAgentDescription = new AMSAgentDescription();
    SearchConstraints searchConstraints = new SearchConstraints();
    searchConstraints.setMaxDepth(new Long(100));
    searchConstraints.setMaxResults(new Long(100));

    Search search = new Search();
    search.set_1(searchConstraints);
    search.set_0(amsAgentDescription);

    ACLMessage msg = createRequestMessage(this, getAMS(), search);
    itsFrame.setItsMsg(msg);
  }


  public void doSearchDF() {

    DFAgentDescription dFAgentDescription = new DFAgentDescription();
    SearchConstraints searchConstraints = new SearchConstraints();

    searchConstraints.setMaxDepth(new Long(100));
    searchConstraints.setMaxResults(new Long(100));

    Search search = new Search();
    search.set_1(searchConstraints);
    search.set_0(dFAgentDescription);

    ACLMessage msg = createRequestMessage(this, this.getDefaultDF(), search);
    itsFrame.setItsMsg(msg);
  }


  public void doLausannePing() {
    ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
    msg.setSender(getAID());

    AID aclPing = new AID("acl_ping", true);
    aclPing.setName("acl_ping@beta.lausanne.agentcities.net");
    aclPing.addAddresses("http://srv02.lausanne.agentcities.net:8080/acc");
    msg.addReceiver(aclPing);
    msg.setReplyWith("Req" + (new Date()).getTime());
    msg.setConversationId("Req" + (new Date()).getTime());

    msg.setContent("ping");
    itsFrame.setItsMsg(msg);
  }


  public void doLocalPing() {
    ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
    msg.setSender(getAID());
    msg.addReceiver(getAID());
    msg.setReplyWith("Req" + (new Date()).getTime());
    msg.setConversationId("Req" + (new Date()).getTime());

    msg.setContent("ping");
    itsFrame.setItsMsg(msg);
  }



  /**
   *  Description of the Method
   */
  public void doHelloWorld() {
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setSender(getAID());
    msg.addReceiver(getAID());
    msg.setContent("Hello World!");
    msg.setLanguage("Plain English");
    msg.setOntology("World Domination");
    msg.addUserDefinedParameter("key1", "value1");
    msg.addUserDefinedParameter("key2", "value2");
    msg.addUserDefinedParameter("key3", "value3");
    itsFrame.setItsMsg(msg);
  }


  /**
   *  Description of the Method
   */
  public void doSystemOut() {
    System.out.println(itsFrame.getItsMsg());
  }


  /**
   *  exit method
   */
  public void doExit() {
    this.doDelete();
    System.exit(0);
  }


  /**
   *  Description of the Method
   */
  public void doNewMessage() {
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setSender(getAID());
    itsFrame.setItsMsg(msg);
  }


  /**
   *  Description of the Method
   */
  public void sendMessage() {
    ACLMessage msg = itsFrame.getItsMsg();
    itsFrame.addMessageNode("out", msg);
    send(msg);
    doNewMessage();
  }


  /**
   *  Description of the Method
   *
   * @param  msg  Description of Parameter
   */
  public void processIncomingMessage(ACLMessage msg) {
    itsFrame.addMessageNode("in", msg);
    if (pingBehaviour) {
      if (msg.getContent() == null) {
        return;
      }

      if ((msg.getPerformative() == ACLMessage.QUERY_REF) &&
        (msg.getContent().equalsIgnoreCase("ping") ||
        (msg.getContent().equalsIgnoreCase("(ping)"))
        )) {
        ACLMessage alive = msg.createReply();
        alive.setPerformative(ACLMessage.INFORM);
        alive.setSender(this.getAID());
        alive.setContent("alive");
        this.send(alive);
        itsFrame.addMessageNode("out", alive);
      }
    }
  }



  public void doReply() {
    ACLMessage msg = itsFrame.aclTreePanel.getCurrentACL();
    if (msg == null) {
      return;
    }
    ACLMessage reply = msg.createReply();
    reply.setSender(this.getAID());
    itsFrame.aclPanel.setItsMsg(reply);
  }


  /**
   *  Description of the Method
   */
  protected void setup() {
    super.setup();
    splash = new SplashScreen();
    splash.show();
    splash.setProgress(25);
    System.out.println("starting up: " + this.getAID().toString());

    try {
      registerLanguage(SL0Codec.NAME, new SL0Codec());
      this.registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());

    }
    catch (Exception e) {
      e.printStackTrace();
    }

    splash.setProgress(50);
    itsFrame = new TestAgentFrame(this);
    splash.setProgress(75);
    addBehaviour(new ReceiveCyclicBehaviour(this));
    splash.setProgress(100);
    splash.hide();
  }


  ACLMessage createRequestMessage(Agent sender, AID receiver, Object what) {
    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
    request.setSender(sender.getAID());
    request.addReceiver(receiver);
    request.setProtocol("fipa-request");
    request.setLanguage(SL0Codec.NAME);
    request.setEncoding("String");
    request.setOntology(FIPAAgentManagementOntology.NAME);
    request.setReplyWith("rw" + sender.getName() + (new Date()).getTime());
    request.setConversationId("conv" + sender.getName() + (new Date()).getTime());

    jade.onto.basic.Action act = new jade.onto.basic.Action();
    act.set_0(receiver);
    act.set_1(what);

    jade.util.leap.ArrayList l = new jade.util.leap.ArrayList();
    jade.lang.sl.SL0Codec sl = new jade.lang.sl.SL0Codec();

    l.add(act);

    String content = encode(act, sl, jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.instance());

    request.setContent(content);
    return request;
  }


  String encode(jade.onto.basic.Action act, Codec c, Ontology o) {
    // Write the action in the :content slot of the request
    jade.util.leap.List l = new jade.util.leap.ArrayList();
    try {
      Frame f = o.createFrame(act, o.getRoleName(act.getClass()));
      l.add(f);
    }
    catch (OntologyException oe) {
      System.out.println("oe ");
    }
    return c.encode(l, o);
  }


  /**
   *  Description of the Class
   *
   * @author     chris
   * @created    May 21, 2002
   */
  private class SplashScreen extends JWindow {
    /**
     *  Constructor for the SplashScreen object
     */
    public SplashScreen() {

      try {
        jbInit();
        this.setSize(400, 50);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width / 2 - this.getSize().width / 2,
          screenSize.height / 2 - this.getSize().height / 2);
        this.setVisible(true);
        this.requestFocus();

        // paintImmediately();
        Toolkit.getDefaultToolkit().sync();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }


    /**
     *  Sets the Progress attribute of the SplashScreen object
     *
     * @param  p  The new Progress value
     */
    public void setProgress(int p) {
      theProgressBar.setValue(p);
    }


    void jbInit() {
      this.getContentPane().setLayout(gridBagLayout1);
      this.getContentPane().setBackground(Color.white);
      this.addFocusListener(
        new java.awt.event.FocusAdapter() {
          public void focusLost(FocusEvent e) {

          }
        });
      jPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
      jPanel1.setLayout(gridBagLayout2);
      jLabel1.setBackground(Color.white);
      jLabel1.setForeground(Color.blue);
      jLabel1.setOpaque(true);
      jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
      jLabel1.setText("loading, please wait...");
      jLabel1.setIcon(dummyIcon);
      theProgressBar.setBackground(Color.white);
      theProgressBar.setForeground(Color.blue);
      theProgressBar.setValue(5);
      theProgressBar.setStringPainted(true);
      this.getContentPane().add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      jPanel1.add(theProgressBar, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 5), 0, 0));

    }


    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanel1 = new JPanel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    JProgressBar theProgressBar = new JProgressBar();

  }


  boolean pingBehaviour = true;

  ImageIcon dummyIcon =
    new ImageIcon(this.getClass().getResource("images/dummy.gif"));

  SplashScreen splash;

  TestAgentFrame itsFrame;
}
//  ***EOF***
