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

package jade.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

interface AgentPlatform extends AgentContainer {

  String getAddress() throws RemoteException;

  String addContainer(AgentContainer ac) throws RemoteException;
  void removeContainer(String name) throws RemoteException;

  AgentContainer lookup(String name) throws RemoteException, NotFoundException;

  void bornAgent(String name, RemoteProxy rp, String containerName) throws RemoteException, NameClashException;
  void deadAgent(String name) throws RemoteException, NotFoundException;

  boolean transferIdentity(String agentName, String src, String dest) throws RemoteException, NotFoundException;

  RemoteProxy getProxy(String agentName, String agentAddress) throws RemoteException, NotFoundException;

}
