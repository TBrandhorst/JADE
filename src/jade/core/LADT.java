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

import jade.util.leap.Map;
import jade.util.leap.HashMap;


// Class for the Local Agent Descriptor Table.

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
class LADT {

  // Rows of the LADT are protected by a recursive mutex lock
  private static class Row {
    private Agent value;
    private Thread owner;
    private long depth;

    public Row(Agent a) {
      value = a;
      depth = 0;
    }

    public synchronized Agent get() {
      return value;
    }

    public synchronized void clear() {
      value = null;
    }

    public synchronized void lock() {
      try {
        Thread me = Thread.currentThread();
	while((owner != null) && (owner != me)) {
		// DEBUG
		Runtime.instance().debug(value, "Wait to lock row", "Wait to lock an empy row");
	  wait();
		Runtime.instance().debug(value, "Waking up", "Waking up on an empty row");
	}
	Runtime.instance().debug(value, "Row locked", "Empty row locked");

	owner = me;
        ++depth;
      }
      catch(InterruptedException ie) {
	return;
      }

    }

    public synchronized void unlock() {
      // Must be owner to unlock
      if(owner != Thread.currentThread())
          return;
      --depth;
      if(depth == 0 || value == null) {
      	// Note that if the row has just been cleared we must wake up 
      	// hanging threads even if depth is > 0, otherwise they will 
      	// hang forever
        owner = null;
        // DEBUG
				Runtime.instance().debug(value, "Unlocking row", "Unlocking empty row");
        notifyAll();
      }
    }

  } // End of Row class


  // Initial size of agent hash table
  //private static final int MAP_SIZE = 50;

  // Load factor of agent hash table
  //private static final float MAP_LOAD_FACTOR = 0.50f;


  //private Map agents = new HashMap(MAP_SIZE, MAP_LOAD_FACTOR);
  private Map agents = new HashMap();

  public Agent put(AID aid, Agent a) {
      Row r;
      synchronized(agents) {
	  r = (Row)agents.get(aid);
      }
      if(r == null) {
	  agents.put(aid, new Row(a));
	  return null;
      }
      else {
	  r.lock();

	  agents.put(aid, new Row(a));
	  Agent old = r.get();

	  r.unlock();
	  return old;
      }
  }

  public Agent remove(AID key) {
    Row r;
    synchronized(agents) {
	r = (Row)agents.get(key);
    }
    if(r == null)
	return null;
    else {
	r.lock();

	agents.remove(key);
	Agent a = r.get();
	// Clear the row value, to avoid pending acquire() using the
	// removed agent...
	r.clear();

	r.unlock();
	return a;
    }
  }

  // The caller must call release() after it has finished with the row
  public Agent acquire(AID key) {
    Row r;
    synchronized(agents) {
	r = (Row)agents.get(key);
    }
    if(r == null)
	return null;
    else {
	r.lock();
	return r.get();
    }
  }

  public void release(AID key) {
    Row r;
    synchronized(agents) {
	r = (Row)agents.get(key);
    }
    if(r != null)
      r.unlock();
  }

  public AID[] keys() {
    synchronized(agents) {
      Object[] objs = agents.keySet().toArray();
      AID[] result = new AID[objs.length];
      System.arraycopy(objs, 0, result, 0, result.length);
      return result;
    }
  }

  public Agent[] values() {
    synchronized(agents) {
	Object[] objs = agents.values().toArray();
	Agent[] result = new Agent[objs.length];
	for(int i = 0; i < objs.length; i++) {
	    Row r = (Row)objs[i];
	    result[i] = r.get();
	}
	return result;
    }

  }

}
