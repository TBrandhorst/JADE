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

package jade.core.behaviours;

import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.core.*;

import java.util.*;

/**
 * This abstract class implements a OneShot task that must be executed
 * only one just after a given timeout is elapsed.
 * <p> The task is simply the call to the method 
 * <code>handleElapsedTimeout()</code> that must be implemented by
 * all subclasses. Notice that the best practice in JADE is when
 * this method just adds a behaviour to the agent class.
 * <p> All subclasses have available the protected variable 
 * <code>myAgent</code> that points to the agent class.
 * <p> The programmer must use this abstract class in this simple way:
 * <ul>
 * <li> implements a subclass that extends WakerBehaviour. This subclass
 * must implement the method <code>handleElapsedTimeout</code>.
 * <li> add the subclass to the list of behaviour of this agent by using
 * <code>addBehaviour()</code> method.
 * <li> the method  <code>handleElapsedTimeout</code> must implement the
 * task that will be executed after the timeout is elapsed.
 * </ul>
 * 
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date$ $Revision$
 */
public abstract class WakerBehaviour extends SimpleBehaviour {

private static final long MINIMUM_TIMEOUT = 10000; // 1 second

/**
@serial
*/
private long wakeupTime, blockTime;
/**
@serial
*/
private int state;
/**
@serial
*/
private boolean finished;

  /**
   * This method constructs the behaviour.
   * @param a is the pointer to the agent
   * @param wakeupDate is the date when the task must be executed
   */
public WakerBehaviour(Agent a, Date wakeupDate) {
  super(a);
  wakeupTime = wakeupDate.getTime();
  state = 0;
  finished = false;
}

  /**
   * This method constructs the behaviour.
   * @param a is the pointer to the agent
   * @param timeout indicates the number of milliseconds after which the
   * task must be executed
   */
public WakerBehaviour(Agent a, long timeout) {
  super(a);
  wakeupTime = System.currentTimeMillis() + timeout;
  state = 0;
  finished = false;
}


public void action() {
  switch (state) {
  case 0: {
    // in this state the behaviour blocks itself
    blockTime = wakeupTime - System.currentTimeMillis();
    if (blockTime < MINIMUM_TIMEOUT)
      blockTime = MINIMUM_TIMEOUT;
    block(blockTime);
    state++;
    break;
  }
  case 1: {
    // in this state the behaviour can be restarted for two reasons
    // 1. the timeout is elapsed (then the handler method is called 
    //                            and the behaviour is definitively finished) 
    // 2. a message has arrived for this agent (then it blocks again and
    //                            the FSM remains in this state)
    blockTime = wakeupTime - System.currentTimeMillis();
    if (blockTime <= 0) {
      // timeout is expired
      finished = true;
      handleElapsedTimeout();
    } else 
      block(blockTime);
    break;
  }
  default : {
    state=0;
    break;
  }
  } // end of switch
  return;
} //end of action

  /**
   * This method is called by this subclass only after the given timeout is
   * elapsed.
   * The method has available the class variable <code>myAgent</code> that
   * points to the agent class.
   */
protected abstract void handleElapsedTimeout();

  /**
   * This method must be called to reset the behaviour and starts again
   * @param wakeupDate is the new time when the task must be executed again
   */
protected void reset(Date wakeupDate) {
  super.reset();
  wakeupTime = wakeupDate.getTime();
  state = 0;
  finished = false;
}

  /**
   * This method must be called to reset the behaviour and starts again
   * @param wakeupDate is the new time when the task must be executed again
   */
protected void reset(long timeout) {
  super.reset();
  wakeupTime = System.currentTimeMillis() + timeout;
  state = 0;
  finished = false;
}

public boolean done() {
  return finished;
}
}