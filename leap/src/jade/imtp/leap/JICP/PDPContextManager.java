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

package jade.imtp.leap.JICP;

//#J2ME_EXCLUDE_FILE

import jade.util.leap.Properties;

import java.net.InetAddress;

public interface PDPContextManager {
	public static final String MSISDN = "msisdn";
	public static final String USERNAME = "pdp-context-username";
	public static final String PASSWORD = "pdp-context-password";
	public static final String LOCATION = "location";
	
	public static interface Listener {
		void handlePDPContextClosed(String msisdn);
	}
	
  void init(Properties p) throws Exception;
  
  void registerListener(Listener l);
  
  Properties getPDPContextInfo(InetAddress addr, String owner);
}