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

package jade.core.messaging;

//#MIDP_EXCLUDE_FILE


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;

import jade.core.Profile;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;

class FileMessageStorage implements MessageStorage {

    private static final String RECEIVER_PREFIX = "AID-";
    private static final String MESSAGE_PREFIX = "MSG-";
    private static final String FOREVER = "FOREVER";

    public FileMessageStorage(Profile p) {
	// Retrieve the base directory from the profile
	String s = p.getParameter(Profile.PERSISTENT_DELIVERY_BASEDIR, null);
	if(s == null) {
	    s = "." + File.separator + "PersistentDeliveryStore";
	}

	baseDir = new File(s);
	if(!baseDir.exists()) {
	    baseDir.mkdir();
	}
    }


    public synchronized void store(ACLMessage msg, AID receiver, Date dueDate) throws IOException {

	// Generate the subdirectory name by hashing the receiver AID
	File subDir = getMessageFolder(receiver);

	// Generate the file name by hashing the receiver AID and the message itself
	File toStore = getMessageFile(subDir, msg, receiver);

	// If the file is already present, increment its copies count
	// If the file is not present, create it and write the data into it
	if(toStore.exists()) {
	    incrementCounter(toStore);
	}
	else {
	    createMessageFile(toStore, msg, receiver, dueDate);
	}

    }

    public synchronized void delete(ACLMessage msg, AID receiver) throws IOException {

	// Generate the subdirectory name by hashing the receiver AID
	File subDir = getMessageFolder(receiver);

	// Generate the file name by hashing the receiver AID and the message itself
	File toDelete = getMessageFile(subDir, msg, receiver);

	// Decrement the counter (if 0, it deletes the file). If the subdirectory is empty, remove it as well
	decrementCounter(toDelete);
	if(subDir.list().length == 0) {
	    subDir.delete();
	}

    }

    public synchronized void loadAll(LoadListener ll) throws IOException {


	// Notify the listener that the load process started
	ll.loadStarted("");

	// Scan all its valid subdirectories.
	File[] subdirs = baseDir.listFiles(new FileFilter() {

		public boolean accept(File f) {
		    return f.isDirectory() && f.getName().startsWith(RECEIVER_PREFIX);
		}

	});
	for(int i = 0; i < subdirs.length; i++) {

	    File subdir = subdirs[i];

	    // Scan all its valid files.
	    File[] files = subdir.listFiles(new FileFilter() {

		    public boolean accept(File f) {
			return !f.isDirectory() && f.getName().startsWith(MESSAGE_PREFIX);
		    }
	    });

	    for(int j = 0; j < files.length; j++) {

		File toRead = files[j];

		// Read the file content
		BufferedReader in = new BufferedReader(new FileReader(toRead));

		// Read the number of copies
		String strHowMany = in.readLine();

		long howMany = 1;
		try {
		    howMany = Long.parseLong(strHowMany);
		}
		catch(NumberFormatException nfe) {
		    // Do nothing; the default value will be used
		}

		// Read the due date
		String strDueDate = in.readLine();
		Date dueDate = null;
		if(!strDueDate.equals(FOREVER)) {
		    try {
			dueDate = new Date(Long.parseLong(strDueDate));
		    }
		    catch(NumberFormatException nfe) {
			// Do nothing; the default value will be used
		    }
		}

		try {

		    // Use an ACL codec to read in the remaining data
		    StringACLCodec codec = new StringACLCodec(in, null);

		    // Read the receiver AID
		    AID receiver = codec.decodeAID();

		    // Read the ACL message
		    ACLMessage message = codec.decode();

		    // Notify the listener that a new item was loaded
		    for(int k = 0; k < howMany; k++) {
			ll.itemLoaded("", message, receiver, dueDate);
		    }
		}
		catch(ACLCodec.CodecException ce) {
		    System.out.println("Error reading file " + toRead.getName() + " [" + ce.getMessage() + "]");
		}
	    }
	}

	// Notify the listener that the load process ended
	ll.loadEnded("");

    }

    private File getMessageFolder(AID receiver) throws IOException {
	String hashedName = RECEIVER_PREFIX + receiver.hashCode();
	File folder = new File(baseDir, hashedName);
	if(!folder.exists()) {
	    folder.mkdir();
	}

	return folder;
    }

    private File getMessageFile(File subDir, ACLMessage msg, AID receiver) throws IOException {
	long hc1 = receiver.hashCode();
	long hc2 = msg.toString().hashCode();
	String hashedName = MESSAGE_PREFIX + (hc1*2 + hc2); 

	File message = new File(subDir, hashedName);
	return message;
    }

    private void incrementCounter(File f) throws IOException {
	BufferedReader in = new BufferedReader(new FileReader(f));
	File tmp = File.createTempFile("JADE", ".tmp");
	String s = in.readLine();
	try {
	    long counter = Long.parseLong(s);
	    BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
	    try {
		counter++;
		s = Long.toString(counter);
		out.write(s, 0, s.length());
		out.newLine();

		s = in.readLine();
		while(s != null) {
		    out.write(s, 0, s.length());
		    out.newLine();
		    s = in.readLine();
		}
	    }
	    finally {
		out.close();
	    }    
	}
	catch(NumberFormatException nfe) {
	    nfe.printStackTrace();
	}
	finally {
	    in.close();
	}

	f.delete();
	tmp.renameTo(f);

    }

    private void decrementCounter(File f) throws IOException {

	BufferedReader in = new BufferedReader(new FileReader(f));
	File tmp = File.createTempFile("JADE", ".tmp");
	String s = in.readLine();
	try {
	    long counter = Long.parseLong(s);
	    counter--;
	    if(counter == 0) {
		in.close();
		f.delete();
	    }
	    else {
		BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
		try {
		    s = Long.toString(counter);
		    out.write(s, 0, s.length());
		    out.newLine();

		    s = in.readLine();
		    while(s != null) {
			out.write(s, 0, s.length());
			out.newLine();
			s = in.readLine();
		    }
		}
		finally {
		    in.close();
		    out.close();
		}

		f.delete();
		tmp.renameTo(f);
	    }
	}
	catch(NumberFormatException nfe) {
	    in.close();
	    nfe.printStackTrace();
	}

    }

    private void createMessageFile(File toStore, ACLMessage msg, AID receiver, Date dueDate) throws IOException {

	BufferedWriter out = null;
	try {
	    toStore.createNewFile();
	    out = new BufferedWriter(new FileWriter(toStore));

	    // Write the number of message copies (of course is 1 to begin with)
	    out.write("1", 0, 1);
	    out.newLine();

	    // Write the due date as a long, or 'FOREVER' if no due date exists
	    String strDate = FOREVER;
	    if(dueDate != null) {
		strDate = Long.toString(dueDate.getTime());
	    }
	    out.write(strDate, 0, strDate.length());
	    out.newLine();

	    // Write the receiver AID in string format
	    String strReceiver = receiver.toString();
	    out.write(strReceiver, 0, strReceiver.length());
	    out.newLine();

	    // Finally, write the ACL message
	    String strMessage = msg.toString();
	    out.write(strMessage, 0, strMessage.length());
	    out.newLine();
	}
	finally {
	    if(out != null) {
		out.close();
	    }
	}
    }


    private File baseDir;

}