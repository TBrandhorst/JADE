/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package jade.lang;

import jade.onto.Frame;
import jade.onto.Ontology;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
/**
   Interface for Content Language encoders/decoders. This interface
   declares two methods that must convert between text and frame
   representations according to a specific content language.
 */
public interface Codec {

  public static class CodecException extends Exception {

    private Throwable nested;

    public CodecException(String msg, Throwable t) {
      super(msg);
      nested = t;
    }

    public Throwable getNested() {
      return nested;
    }

  }

  /**
     Encodes a <code>Frame</code> object into a Jave
     <code>String</code>, according to this Content Language and
     looking up the given ontology for the role played by symbols
     (i.e. whether they are concepts, actions or predicates).
     @param f The frame to encode.
     @param o The ontology to use to lookup the roles for the various
     symbols.
     @return A Java string, representing the given frame according to
     this content language.
     @see jade.onto.Ontology
   */
  String encode(Frame f, Ontology o);

  /**
     Decodes a given <code>String</code>, according to the given
     Content Language and obtains a <code>Frame</code> object. This
     method uses the given ontology to distinguish among the different
     kinds of roles a symbol can play (i.e. Concept vs. Action
     vs. Predicate).
     @param s A string containing the representation of an ontological
     element, encoded according to this content language.
     @param o The ontology to use to lookup the roles for the various
     symbols.
     @return A frame, representing the given ontological element.
     @see jade.onto.Ontology
  */
  Frame decode(String s, Ontology o) throws CodecException;

}
