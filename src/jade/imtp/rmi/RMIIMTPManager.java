/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.imtp.rmi;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.io.Serializable;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

import java.util.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.util.Map;
import java.util.HashMap;


import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.mtp.TransportAddress;


/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public class RMIIMTPManager implements IMTPManager {

  private static class RemoteProxyRMI implements AgentProxy {
    private AgentContainerRMI ref;
    private AID               receiver;

    // This needs to be restored whenever a RemoteProxyRMI object is
    // serialized.
    private transient RMIIMTPManager manager;

    public RemoteProxyRMI(AgentContainerRMI ac, AID recv) {
        ref = ac;
        receiver = recv;
    }

    public AID getReceiver() {
      return receiver;
    } 

    public AgentContainer getRef() {
      return manager.getAdapter(ref);
    } 

    public void dispatch(ACLMessage msg) throws NotFoundException, UnreachableException {
      try {
        ref.dispatch(msg, receiver);
      } 
      catch (RemoteException re) {
				throw new UnreachableException("IMTP failure: [" + re.getMessage() + "]");
      }
      catch (IMTPException imtpe) {
				throw new UnreachableException("IMTP failure: [" + imtpe.getMessage() + "]");
      }
    }

    public void ping() throws UnreachableException {
      try {
	ref.ping(false);
      } 
      catch (RemoteException re) {
	throw new UnreachableException("Unreachable remote object: [" + re.getMessage() + "]");
      }
      catch (IMTPException imtpe) {
	throw new UnreachableException("Unreachable remote object: [" + imtpe.getMessage() + "]");
      }
    }

    public void setMgr(RMIIMTPManager mgr) {
      manager = mgr;
    }

  } // End of RemoteProxyRMI class

	private static final int DEFAULT_RMI_PORT = 1099;
	
  private Profile myProfile;
  private String mainHost;
  private int mainPort;
  private String platformRMI;
  private MainContainer remoteMC;

  // Maps agent containers into their stubs
  private Map stubs;

  public RMIIMTPManager() {
    stubs = new HashMap();
  }

  /**
   */
  public void initialize(Profile p) throws IMTPException {
    try {
      myProfile = p;
      mainHost = myProfile.getParameter(Profile.MAIN_HOST);
      if (mainHost == null) {
      	// Use the local host by default
      	try {
	  			mainHost= InetAddress.getLocalHost().getHostName();      
      	} 
      	catch(UnknownHostException uhe) {
      		throw new IMTPException("Unknown main host");
      	}
      }
      
      mainPort = DEFAULT_RMI_PORT;
      String mainPortStr = myProfile.getParameter(Profile.MAIN_PORT);
      if (mainPortStr != null) {
      	try {
      		mainPort = Integer.parseInt(mainPortStr);
      	}
      	catch (NumberFormatException nfe) {
      		// Do nothing. The DEFAULT_RMI_PORT is used 
      	}
      }
      
      platformRMI = "rmi://" + mainHost + ":" + mainPort + "/JADE";
    }
    catch (ProfileException pe) {
      throw new IMTPException("Can't get main host and port", pe);
    }
  }

  /**
   */
  public void remotize(AgentContainer ac) throws IMTPException {
    try {
      AgentContainerRMI acRMI = new AgentContainerRMIImpl(ac, this);
      stubs.put(ac, acRMI);
    }
    catch(RemoteException re) {
      throw new IMTPException("Failed to create the RMI container", re);
    }
  }



    /**
     * Get the RMIRegistry. If a registry is already active on this host
     * and the given portNumber, then that registry is returned, 
     * otherwise a new registry is created and returned.
     * @param portNumber is the port on which the registry accepts requests
     * @param host host for the remote registry, if null the local host is used
     * @author David Bell (HP Palo Alto)
     **/
    private Registry getRmiRegistry(String host, int portNumber) throws RemoteException {
	Registry rmiRegistry = null;
	// See if a registry already exists and
	// make sure we can really talk to it.
	try {
	    rmiRegistry = LocateRegistry.getRegistry(host, portNumber);
	    rmiRegistry.list();
	} catch (Exception exc) {
	    rmiRegistry = null;
	}

	// If rmiRegistry is null, then we failed to find an already running
	// instance of the registry, so let's create one.
	if (rmiRegistry == null) {
	    //if (isLocalHost(host))
		rmiRegistry = LocateRegistry.createRegistry(portNumber);
		//else
		//throw new java.net.UnknownHostException("cannot create RMI registry on a remote host");
	}

	return rmiRegistry;
	
    } // END getRmiRegitstry()




  /**
   */
  public void remotize(MainContainer mc) throws IMTPException {
    try {
      MainContainerRMI mcRMI = new MainContainerRMIImpl(mc, this);
      Registry theRegistry = getRmiRegistry(null,mainPort);
      Naming.bind(platformRMI, mcRMI);
    }
    catch(ConnectException ce) {
      // This one is thrown when trying to bind in an RMIRegistry that
      // is not on the current host
      System.out.println("ERROR: trying to bind to a remote RMI registry.");
      System.out.println("If you want to start a JADE main container:");
      System.out.println("  Make sure the specified host name or IP address belongs to the local machine.");
      System.out.println("  Please use '-host' and/or '-port' options to setup JADE host and port.");
      System.out.println("If you want to start a JADE non-main container: ");
      System.out.println("  Use the '-container' option, then use '-host' and '-port' to specify the ");
      System.out.println("  location of the main container you want to connect to.");
      throw new IMTPException("RMI Binding error", ce);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication failure while starting JADE Runtime System. Check if the RMIRegistry CLASSPATH includes the RMI Stub classes of JADE.", re);
    }
    catch(Exception e) {
      throw new IMTPException("Problem starting JADE Runtime System.", e);
    }
  }

  /**
     Disconnects the given Agent Container and hides it from remote
     JVMs.
  */
  public void unremotize(AgentContainer ac) throws IMTPException {
    try {
      AgentContainerRMIImpl impl = (AgentContainerRMIImpl)getRMIStub(ac);    
      if(impl == null)
	throw new IMTPException("No RMI object for this agent container");
      impl.unexportObject(impl, true);
    }
    catch(RemoteException re) {
      throw new IMTPException("RMI error during shutdown", re);
    }
    catch(ClassCastException cce) {
      throw new IMTPException("The RMI implementation is not locally available", cce);
    }
  }

  /**
     Disconnects the given Main Container and hides it from remote
     JVMs.
  */
  public void unremotize(MainContainer mc) throws IMTPException {
    // Unbind the main container from RMI Registry
    // Unexport the RMI object
  }

  public AgentProxy createAgentProxy(AgentContainer ac, AID id) throws IMTPException {
    AgentContainerRMI acRMI = getRMIStub(ac);
    RemoteProxyRMI rp = new RemoteProxyRMI(acRMI, id);
    rp.setMgr(this);
    return rp;
  }

  /**
   */
  public synchronized MainContainer getMain(boolean reconnect) throws IMTPException {
    // Look the remote Main Container up into the
    // RMI Registry.
    try {
      if(remoteMC == null || reconnect) {
	MainContainerRMI remoteMCRMI = (MainContainerRMI)Naming.lookup(platformRMI);
	remoteMC = new MainContainerAdapter(remoteMCRMI, this);
      }
      return remoteMC;
    }
    catch (Exception e) {
      throw new IMTPException("Exception in RMI Registry lookup", e);
    }
  }

  /**
   */
  public void shutDown() {
  }

  /**
   */
  public List getLocalAddresses() throws IMTPException {
    try {
      List l = new ArrayList();
      // The port is meaningful only on the Main container
      TransportAddress addr = new RMIAddress(InetAddress.getLocalHost().getHostName(), String.valueOf(mainPort), null, null);
      l.add(addr);
      return l;
    }
    catch (Exception e) {
      throw new IMTPException("Exception in reading local addresses", e);
    }
  }

  AgentContainerRMI getRMIStub(AgentContainer ac) {
    return (AgentContainerRMI)stubs.get(ac);
  }

  AgentContainer getAdapter(AgentContainerRMI acRMI) {
    Iterator it = stubs.entrySet().iterator();
    while(it.hasNext()) {
      Map.Entry e = (Map.Entry)it.next();
      if(acRMI.equals(e.getValue()))
	return (AgentContainer)e.getKey();
    }
    AgentContainer ac = new AgentContainerAdapter(acRMI, this);
    stubs.put(ac, acRMI);
    return ac;
  }

  void adopt(AgentProxy ap) throws IMTPException {
    try {
      RemoteProxyRMI rpRMI = (RemoteProxyRMI)ap;
      rpRMI.setMgr(this);
    }
    catch(ClassCastException cce) {
      throw new IMTPException("Cannot adopt this Remote Proxy", cce);
    }
  }

	/**
		Creates the client socket factory, which will be used
		to instantiate a <code>UnicastRemoteObject</code>.
		@return The client socket factory.
	*/
	public RMIClientSocketFactory getClientSocketFactory() {
		return new SimpleSocketFactory();
	}

	/**
		Creates the server socket factory, which will be used
		to instantiate a <code>UnicastRemoteObject</code>.
		@return The server socket factory.
	*/
	public RMIServerSocketFactory getServerSocketFactory() { 
		return new SimpleSocketFactory();
	}

}
