/*
  $Id$
*/

package examples.ex1;

import jade.core.*;


// An example of recursive aggregation of complex agent behaviours.
public class Agent3 extends Agent {

  class Behaviour3Step extends SimpleBehaviour {

    private String myCode;

    public Behaviour3Step(Agent a, String code) {
      super(a);
      myCode = code;
    }

    public void action() {
      System.out.println("Agent " + myAgent.getName() + ": Step " + myCode);
    } 

}


  protected void setup() {

    ComplexBehaviour myBehaviour1 = new SequentialBehaviour(this);
    ComplexBehaviour myBehaviour2 = new SequentialBehaviour(this);

    ComplexBehaviour myBehaviour2_1 = new SequentialBehaviour(this);
    ComplexBehaviour myBehaviour2_2 = new SequentialBehaviour(this);

    myBehaviour2_1.addBehaviour(new Behaviour3Step(this,"2.1.1"));
    myBehaviour2_1.addBehaviour(new Behaviour3Step(this,"2.1.2"));
    myBehaviour2_1.addBehaviour(new Behaviour3Step(this,"2.1.3"));

    myBehaviour2_2.addBehaviour(new Behaviour3Step(this,"2.2.1"));
    myBehaviour2_2.addBehaviour(new Behaviour3Step(this,"2.2.2"));
    myBehaviour2_2.addBehaviour(new Behaviour3Step(this,"2.2.3"));

    myBehaviour1.addBehaviour(new Behaviour3Step(this,"1.1"));
    myBehaviour1.addBehaviour(new Behaviour3Step(this,"1.2"));
    myBehaviour1.addBehaviour(new Behaviour3Step(this,"1.3"));

    myBehaviour2.addBehaviour(myBehaviour2_1);
    myBehaviour2.addBehaviour(myBehaviour2_2);
    myBehaviour2.addBehaviour(new Behaviour3Step(this,"2.3"));
    myBehaviour2.addBehaviour(new Behaviour3Step(this,"2.4"));
    myBehaviour2.addBehaviour(new Behaviour3Step(this,"2.5"));

    addBehaviour(myBehaviour1);
    addBehaviour(myBehaviour2);

  }


}
