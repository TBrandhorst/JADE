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

package jade.imtp.leap;


import jade.core.Node;
import jade.core.HorizontalCommand;
import jade.core.Service;
import jade.core.ServiceException;
import jade.core.IMTPException;

/**
   This calss implements a skeleton to a local LEAP Node.
   @author Giovanni Rimassa - FRAMeTech s.r.l.
   @author Giovanni Caire - TILAB
*/
class NodeSkel extends Skeleton {

  private Node myNode;

  public NodeSkel(Node n) {
		myNode = n;
  }

  public Command executeCommand(Command command) throws Throwable {
		Command resp = null;
	
		switch (command.getCode()) {
			case Command.ACCEPT_COMMAND: {
		    HorizontalCommand cmd = (HorizontalCommand)command.getParamAt(0);
		    Object result = myNode.accept(cmd);
	
		    resp = new Command(Command.OK);
		    resp.addParam(result);
		    break;
			}
			case Command.PING_NODE_BLOCKING:
			case Command.PING_NODE_NONBLOCKING: {
		    Boolean hang = (Boolean)command.getParamAt(0);
		    Boolean result = new Boolean(myNode.ping(hang.booleanValue()));
	
		    resp = new Command(Command.OK);
		    resp.addParam(result);
		    break;
			} 
			case Command.EXIT_NODE: {
		    myNode.exit();
		    resp = new Command(Command.OK);
		    break;
			} 
			case Command.INTERRUPT_NODE: {
		    myNode.interrupt();
		    resp = new Command(Command.OK);
		    break;
			}
		}

		return resp;
  }


    //public Object accept(HorizontalCommand cmd, String itfName, String[] formalParameterTypes) throws IMTPException {

	/***
	System.out.println("--- Command Received ---");
	System.out.println("Name: <" + cmd.getName() + ">");
	System.out.println("Service: <" + cmd.getService() + ">");

	Object[] args = cmd.getParams();
	for(int i = 0; i < args.length; i++) {
	    System.out.println("param[" + i + "] = " + args[i]);
	}

	System.out.println("--- ================ ---");

	***/
/*
	try {
	    return myNode.serve(cmd);
	}
	catch(ServiceException se) {
	    throw new IMTPException("Service Error", se);
	}
    }

    public boolean ping(boolean hang) throws IMTPException {
      if(hang) {
	  waitTermination();
      }
      return terminating;
    }

    public void exit() throws IMTPException {
	// Unblock threads hung in ping() method (this will deregister the container)
	terminating = true;
	notifyTermination();
    }

    public void interrupt() throws IMTPException {
	notifyTermination();
    }

    private void waitTermination() {
	synchronized(terminationLock) {
	    try {
		terminationLock.wait();
	    }
	    catch(InterruptedException ie) {
		System.out.println("PING wait interrupted");
		// Do nothing
	    }
	}
    }

    private void notifyTermination() {
      synchronized(terminationLock) {
	  terminationLock.notifyAll();
      }
    }


    // This monitor is used to hang a remote ping() call in order to
    // detect node failures.
    //private Object terminationLock = new Object();
    //private boolean terminating = false;
*/
}
