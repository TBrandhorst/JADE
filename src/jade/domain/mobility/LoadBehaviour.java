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

package jade.domain.mobility;

//#MIDP_EXCLUDE_FILE

import jade.content.AgentAction;
import jade.util.leap.List;

/**
   This action represents a request to load a <code>Behaviour</code>.
   @see jade.core.behaviours.LoaderBehaviour
   @author Giovanni Caire - TILAB
 */
public class LoadBehaviour implements AgentAction {
	private String className;
	private byte[] code;
	private byte[] zip;
	private List parameters;
	
	public LoadBehaviour() {
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setCode(byte[] code) {
		this.code = code;
	}
	
	public byte[] getCode() {
		return code;
	}
	
	public void setZip(byte[] zip) {
		this.zip = zip;
	}
	
	public byte[] getZip() {
		return zip;
	}
	
	public void setParameters(List parameters) {
		this.parameters = parameters;
	}
	
	public List getParameters() {
		return parameters;
	}
}