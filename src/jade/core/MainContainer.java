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

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPDescriptor;

//__SECURITY__BEGIN
import jade.security.AgentPrincipal;
import jade.security.UserPrincipal;
import jade.security.AuthException;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
//__SECURITY__END


/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

public interface MainContainer {

    //void register(AgentContainerImpl ac, ContainerID cid) throws IMTPException;

    //void deregister(AgentContainer ac) throws IMTPException;

    //void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException;

    String getPlatformName() throws IMTPException;

    String addContainer(AgentContainer ac, ContainerID cid, UserPrincipal user, byte[] passwd) throws IMTPException, AuthException;
    void removeContainer(ContainerID cid) throws IMTPException;

    AgentContainer lookup(ContainerID cid) throws IMTPException, NotFoundException;

    void bornAgent(AID name, ContainerID cid) throws IMTPException, NameClashException, NotFoundException;
    void deadAgent(AID name) throws IMTPException, NotFoundException;

    void suspendedAgent(AID name) throws IMTPException, NotFoundException;
    void resumedAgent(AID name) throws IMTPException, NotFoundException;

//__SECURITY__BEGIN
    void changedAgentPrincipal(AID name, AgentPrincipal from, AgentPrincipal to) throws IMTPException, NotFoundException;
    DelegationCertificate sign(DelegationCertificate certificate, IdentityCertificate identity, DelegationCertificate[] delegations) throws IMTPException, AuthException;
//__SECURITY__END

    void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException;
    void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException;

    boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws IMTPException, NotFoundException;

    AgentProxy getProxy(AID id) throws IMTPException, NotFoundException;

}
