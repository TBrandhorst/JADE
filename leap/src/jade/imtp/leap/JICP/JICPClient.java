/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Motorola.
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
 * **************************************************************
 */


package jade.imtp.leap.JICP;

import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import jade.core.CaseInsensitiveString;
import java.io.*;
// import java.net.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 * @author Ronnie Taib - Motorola
 * @author Steffen Rusitschka - Siemens
 */
class JICPClient {

  /**
   * Constructor declaration
   */
  public JICPClient() {
  } 

  /**
   * Send a command to this transport address
   * @param ta the address to send the command to
   * @param dataType the type of data as defined in the JICPPeer
   * @param data the command
   * @return a byte array corresponding to the answer
   * 
   * @throws ICP.ICPException
   */
  public byte[] send(TransportAddress ta, byte dataType, byte[] data) throws ICP.ICPException {
    Connection       connection = null;
    DataOutputStream out = null;
    DataInputStream  inp = null;
    JICPPacket       reply = null;

    try {
      // Check that the protocol is JICP
      String proto = ta.getProto();

      if (!CaseInsensitiveString.equalsIgnoreCase(proto, JICPProtocol.NAME)) {
        throw new ICP.ICPException("Incorrect protocol "+proto);
      } 

      // Open the connection and gets the output and input streams
      connection = new Connection(ta);
      out = new DataOutputStream(connection.getOutputStream());
      inp = new DataInputStream(connection.getInputStream());

      byte dataInfo = JICPProtocol.COMPRESSED_INFO;

      // Set the JICP additional information
      if (dataType == JICPProtocol.COMMAND_TYPE) {
        int commandType = Command.getCommandType(data);
        switch (commandType) {
        case Command.PING:
          dataInfo |= JICPProtocol.NON_BLOCKING_IMTP_PING_INFO;
          break;

        case Command.BLOCKING_PING:
          dataInfo |= JICPProtocol.BLOCKING_IMTP_PING_INFO;
          break;
        }

        /*if ((commandType&Command.HAS_RESULT) != 0) {
          dataInfo |= JICPProtocol.IMTP_COMMAND_WITH_RESULT_INFO;
        }*/ 
      } 

      // Send the complete JICPPacket
      JICPPacket request = new JICPPacket(dataType, dataInfo, ta.getFile(), data);
      request.writeTo(out);

      // Read the reply
      reply = JICPPacket.readFrom(inp);
    } 
    catch (EOFException eof) {
      throw new ICP.ICPException("EOF reached");
    } 
    // catch (UnknownHostException uhe) {
    // throw new ICP.ICPException("Cannot connect to "+ta.getHost()+":"+ta.getPort());
    // }
    catch (IOException ioe) {
      throw new ICP.ICPException("I/O error sending/receiving data to "+ta.getHost()+":"+ta.getPort(), ioe);
    } 
    catch (Exception e) {
      throw new ICP.ICPException("Problems in communication with "+ta.getHost()+":"+ta.getPort(), e);
    } 
    finally {
      try {
        // Close the connection
        if (inp != null) {
          inp.close();
        } 

        if (out != null) {
          out.close();
        } 

        if (connection != null) {
          connection.close();
        } 
      } 
      catch (IOException ioe) {
        throw new ICP.ICPException("I/O error while closing the connection", ioe);
      } 
    } 

    if (reply.getDataType() == JICPProtocol.ERROR_TYPE) {
      throw new ICP.ICPException(new String(reply.getData()));
    } 

    return reply.getData();
  } 

}
