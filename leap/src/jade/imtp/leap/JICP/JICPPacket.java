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

import java.io.*;
import jade.util.Logger;

/**
 * This class is the JICP data packet representation along
 * with methods for reading from and writing to dataXXputStreams.
 * @author Ronnie Taib - Motorola
 * @author Steffen Rusitschka - Siemens AG
 */
class JICPPacket {

  /**
   * The type of data included in the packet
   */
  private byte   type;

  /**
   * Some bit encoded information about the packet:
   * - Whether the payload is compressed 
   * - Whether the recipientID field is present
   * - Whether this packet carries a PING command
   */
  private byte   info;

  /**
   * An optional field indicating the actual recipient for this JICPPacket. 
   * - A JICPServer receiving a JICPPacket from a remote container
   * interprets this field as the ID of a local Mediator.
   * - A Mediator receiving a JICPPacket from its mediated container
   * interprets this field as the serialized transport address of 
   * final destination to forward the packet.
   */
  private String recipientID;

  /**
   * The payload data itself, as a byte array
   */
  private byte[] data;

  /**
   * Empty constructor
   */
  private JICPPacket() {
  } 

  /**
   * Constructor.
   * @param type The type of data included in the packet
   * @param data The data itself, as a byte array.
   */
  JICPPacket(byte type, byte info, byte[] data) {
    init(type, info, null, data);
  }

  /**
   * Constructor used to set the recipientID.
   * @param type The type of data included in the packet
   * @param data The data itself, as a byte array.
   */
  JICPPacket(byte type, byte info, String recipientID, byte[] data) {
    init(type, info, recipientID, data);
  }

  /**
   * constructs a JICPPacket of type JICPProtocol.ERROR_TYPE and sets the
   * data to the string representation of the exception.
   */
  JICPPacket(String explanation, Exception e) {
    if (e != null) {
      explanation = explanation+": "+e.toString();
    } 

    init(JICPProtocol.ERROR_TYPE, JICPProtocol.UNCOMPRESSED_INFO, null, explanation.getBytes());
  }

  /**
   */
  private void init(byte t, byte i, String id, byte[] d) {
    type = t;
  	info = i;
    data = d;
    
    setRecipientID(id);

    if (data != null) {
    	info |= JICPProtocol.DATA_PRESENT_INFO;
    	//if ((info & JICPProtocol.COMPRESSED_INFO) != 0) {
      //	data = JICPCompressor.compress(data);
    	//}
    }
  } 

  /**
   * @return The type of data included in the packet.
   */
  byte getType() {
    return type;
  } 

  /**
   * @return the info field of this packet
   */
  byte getInfo() {
    return info;
  } 

  /**
   * @return The recipientID of this packet.
   */
  String getRecipientID() {
    return recipientID;
  } 

  /**
   * Set the recipientID of this packet and adjust the info field
   * accordingly.
   */
  void setRecipientID(String id) {
    recipientID = id;
    
    if (recipientID != null) {
    	info |= JICPProtocol.RECIPIENT_ID_PRESENT_INFO;
    }
    else {
    	info &= (~JICPProtocol.RECIPIENT_ID_PRESENT_INFO);
    }
  } 

  /**
   * Set the TERMINATED_INFO flag in the info field.
   */
  void setTerminatedInfo() {
    info |= JICPProtocol.TERMINATED_INFO;
  } 

  /**
   * @return The actual data included in the packet, as a byte array.
   */
  byte[] getData() {
    //if (data != null && data.length != 0) {
    //  return (info & JICPProtocol.COMPRESSED_INFO) != 0 ? JICPCompressor.decompress(data) : data;
    //} 
    //else {
      return data;
    //} 
  } 

  /**
   * Writes the packet into the provided <code>DataOutputStream</code>.
   * The packet is serialized in an internal representation, so the
   * data should be retrieved and deserialized with the
   * <code>readFrom()</code> static method below. The output stream is flushed
   * but not opened nor closed by this method.
   * 
   * @param out The  <code>OutputStream</code> to write the data in
   * @exception May send a large bunch of exceptions, mainly in the IO
   * package.
   */
  int writeTo(OutputStream out) throws IOException {
  	int cnt = 2;
    try {
      // Write the packet type
      out.write(type);

      // Write the packet info
      out.write(info);

      // Write recipient ID only if != null
      if (recipientID != null) {
      	out.write(recipientID.length());
      	out.write(recipientID.getBytes());
      } 

      // Write data only if != null
      if (data != null) {
	      // Size
      	int size = data.length;
      	out.write(size);
      	out.write(size >> 8);
      	// Payload
      	if (size > 0) {
        	out.write(data, 0, size);
        	cnt += size;
      	}
      }
    	// DEBUG
    	//System.out.println(getLength()+" bytes written");
      return cnt;
    } 
    finally {
      out.flush();
    } 
  } 

  /**
   * This static method reads from a given
   * <code>DataInputStream</code> and returns the JICPPacket that
   * it reads. The input stream is not opened nor closed by this method.
   * 
   * @param in The <code>InputStream</code> to read from
   * @exception May send a large bunch of exceptions, mainly in the IO
   * package.
   */
  static JICPPacket readFrom(InputStream in) throws IOException {
    JICPPacket p = new JICPPacket();

    // Read packet type
    p.type = (byte) in.read();

    // Read the packet info
    p.info = (byte) in.read();

    // Read recipient ID if present
    if ((p.info & JICPProtocol.RECIPIENT_ID_PRESENT_INFO) != 0) {
    	int size = (byte) (in.read() & 0x000000ff);
    	byte[] bb = new byte[size];
    	in.read(bb, 0, size);
      p.recipientID = new String(bb);
    } 

    // Read data if present
    if ((p.info & JICPProtocol.DATA_PRESENT_INFO) != 0) {
    	int b1 = in.read();
    	int b2 = in.read();
    	int size = ((b2 << 8) & 0x0000ff00) | (b1 & 0x000000ff);
    	if (size == 0) {
      	p.data = new byte[0];
    	} 
    	else {
      	// Read the actual data
      	p.data = new byte[size];

      	int cnt = 0;
      	int n;
      	do {
        	n = in.read(p.data, cnt, size-cnt);
        	if (n == -1) {
          	break;
        	} 
        	cnt += n;
      	} 
      	while (cnt < size);

      	if (cnt < size) {
        	Logger.println("WARNING: only "+cnt+" bytes received back, while "+size+" were expected");
      	} 
    	}
      //Logger.println("JICPPacket read. Type:"+p.type+" Info:"+p.info+" RID:"+p.recipientID+" Data-length:"+(p.data != null ? p.data.length : 0));
    } 

    // DEBUG
    //System.out.println(p.getLength()+" bytes read");
    return p;
  } 

  public int getLength() {      
  	return (2 + (recipientID != null ? recipientID.length()+4 : 0) + (data != null ? 4+data.length : 0));
  }
  
}

