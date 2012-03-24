/**
 * Copyright (c) 2004,2005 UCLA Compilers Group. 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *  Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 * 
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 * 
 *  Neither UCLA nor the names of its contributors may be used to endorse 
 *  or promote products derived from this software without specific prior 
 *  written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

/*
 * All files in the distribution of JTB, The Java Tree Builder are 
 * Copyright 1997, 1998, 1999 by the Purdue Research Foundation of Purdue
 * University.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted 
 * provided that this entire copyright notice is duplicated in all 
 * such copies, and that any documentation, announcements, and 
 * other materials related to such distribution and use acknowledge 
 * that the software was developed at Purdue University, West Lafayette,
 * Indiana by Kevin Tao, Wanjun Wang and Jens Palsberg.  No charge may 
 * be made for copies, derivations, or distributions of this material
 * without the express written consent of the copyright holder.  
 * Neither the name of the University nor the name of the author 
 * may be used to endorse or promote products derived from this 
 * material without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR ANY PARTICULAR PURPOSE.
 */

package EDU.purdue.jtb.misc;

import EDU.purdue.jtb.visitor.Printer;
import EDU.purdue.jtb.syntaxtree.*;
import java.util.*;
import java.io.*;

/**
 * Class ClassInfo is used by the visitors to store information about a class
 * including its name, a list of field types, and a list of field names.
 *
 * Class CommentPrinter appears at the end of this file.
 */
public class ClassInfo {
   private Node astNode;
   private String name;
   private Vector typeList = new Vector();
   private Vector nameList = new Vector();
   private Vector initList = new Vector();
   private boolean makeDefaultConstructor = false;

   public ClassInfo(Node node, String n) {
      astNode = node;
      name = n;
   }

   public Node   getAstNode()  { return astNode; }
   public String getName()     { return name; }
   public Vector getTypeList() { return typeList; }
   public Vector getNameList() { return nameList; }

   public void addField(String type, String name) {
      addField(type, name, null);
   }

   /**
    * Use this method to generate a default constructor where this field value
    * is already filled in, e.g.
    *
    * <PRE>
    * MyProduction ::= Foo() "a constant token"
    * 
    * public MyProduction(Foo f) {
    *    n.f0 = f;
    *    n.f1 = "a constant token";
    * }
    * </PRE>
    */
   public void addField(String type, String name, String initialValue) {
      typeList.addElement(type);
      nameList.addElement(name);

      if ( initialValue == null || initialValue.equals("") )
         initList.addElement(null);
      else {
         initList.addElement(initialValue);
         makeDefaultConstructor = true;
      }
   }

   /**
    * Returns the EBNF production of the current class as a bunch of comments
    * showing which field names belong to which parts of the production.
    */
   public String getEbnfProduction(Spacing space) {
      StringWriter buf = new StringWriter();
      PrintWriter out = new PrintWriter(buf);
      String temp;
      BufferedReader str;

      // print result into buffer
      astNode.accept(new CommentPrinter(out, space));
      out.flush();
      temp = buf.toString();
      return temp;
   }

   /**
    * Returns a string representing the class.
    */
   public String getClassString(Spacing space) {
      Enumeration types = typeList.elements();
      Enumeration names = nameList.elements();
      Enumeration inits;
      StringWriter buf = new StringWriter();
      PrintWriter out = new PrintWriter(buf);

      out.print(space.spc + "public class " + name);

      if ( Globals.nodeSuperclass != null )
         out.print(" extends " + Globals.nodeSuperclass);

      out.println(" implements Node {");
      space.updateSpc(+1);

      //
      // Output data fields
      //
      if ( Globals.parentPointers )
         out.println(space.spc + "private Node parent;");

      for ( ; types.hasMoreElements(); )
         out.println(space.spc + "public " + (String)types.nextElement() + " " +
                     (String)names.nextElement() + ";");

      //
      // Output standard constructor header
      //
      out.println();
      out.print(space.spc + "public " + name + "(");
      
      types = typeList.elements();
      if ( types.hasMoreElements() )
         out.print((String)types.nextElement() + " n0");

      for ( int count = 1; types.hasMoreElements(); ++count )
         out.print(", " + (String)types.nextElement() + " n" + count);

      out.println(") {");

      //
      // Output standard constructor body
      //
      names = nameList.elements();
      space.updateSpc(+1);

      for ( int count = 0; names.hasMoreElements(); ++count ) {
         String name = (String)names.nextElement();
         out.println(space.spc + name + " = n" + count + ";");

         if ( Globals.parentPointers )
            out.println(space.spc + "if ( " + name + " != null ) " + name +
               ".setParent(this);");
      }

      space.updateSpc(-1);
      out.println(space.spc + "}");

      //
      // Output default constructor header if necessary
      //
      if ( makeDefaultConstructor ) {
         int count = 0;
         boolean firstTime = true;

         out.println();
         out.print(space.spc + "public " + name + "(");
      
         types = typeList.elements();
         inits = initList.elements();

         while ( types.hasMoreElements() ) {
            String type = (String)types.nextElement();

            if ( inits.nextElement() == null ) {
               if ( !firstTime )
                  out.print(", ");

               out.print(type + " n" + count);
               ++count;
               firstTime = false;
            }
         }

         out.println(") {");
      }

      //
      // Output default constructor body if necessary
      //
      if ( makeDefaultConstructor ) {
         int count = 0;

         names = nameList.elements();
         inits = initList.elements();
         space.updateSpc(+1);

         while ( names.hasMoreElements() ) {
            String name = (String)names.nextElement();
            String init = (String)inits.nextElement();

            if ( init != null )
               out.println(space.spc + name + " = " + init + ";");
            else {
               out.println(space.spc + name + " = n" + count + ";");
               ++count;
            }

            if ( Globals.parentPointers )
               out.println(space.spc + "if ( " + name + " != null ) " + name +
                  ".setParent(this);");
         }

         space.updateSpc(-1);
         out.println(space.spc + "}");
      }

      //
      //
      // Output visit method
      //
      out.println();
      out.println(space.spc + "public void accept(" + Globals.visitorPackage +
                  ".Visitor v) {");
      space.updateSpc(+1);
      out.println(space.spc + "v.visit(this);");
      space.updateSpc(-1);
      out.println(space.spc + "}");

	  out.println(space.spc + "public <R,A> R accept(" + Globals.visitorPackage +
                  ".GJVisitor<R,A> v, A argu) {");
      space.updateSpc(+1);
      out.println(space.spc + "return v.visit(this,argu);");
      space.updateSpc(-1);
      out.println(space.spc + "}");

	  out.println(space.spc + "public <R> R accept(" + Globals.visitorPackage +
                  ".GJNoArguVisitor<R> v) {");
      space.updateSpc(+1);
      out.println(space.spc + "return v.visit(this);");
      space.updateSpc(-1);
      out.println(space.spc + "}");

	  out.println(space.spc + "public <A> void accept(" + Globals.visitorPackage +
                  ".GJVoidVisitor<A> v, A argu) {");
      space.updateSpc(+1);
      out.println(space.spc + "v.visit(this,argu);");
      space.updateSpc(-1);
      out.println(space.spc + "}");

      //
      // Output get/set parent methods
      //
      if ( Globals.parentPointers ) {
         out.println(space.spc+"public void setParent(Node n) { parent = n; }");
         out.println(space.spc+"public Node getParent()       { return parent; }");
      }

      //
      // Finish it all off
      //
      space.updateSpc(-1);
      out.println(space.spc + "}");

      out.flush();
      out.close();
      return buf.toString();
   }
}

/**
 * An extension of VPrinter that prints the comments for the generated Visitor.
 * Also shows which field stors which part of the production.
 *
 * Modified to use JavaDoc style comments.  Assumes the opening and
 * closing comment tokens are printed before and after this class's output.
 */
class CommentPrinter extends Printer {
   private boolean visitedLookahead = false;
   private Spacing spc;
   private int nestLevel = 0;
   private int fieldNum = 0;
   private FieldNameGenerator nameGen = new FieldNameGenerator();

   CommentPrinter(PrintWriter w, Spacing s) {
      super(w);
      spc = s;
   }

   public void visit(NodeToken n) {
      if ( !Globals.javaDocComments ) out.print(n.tokenImage);
      else {
         // Convert special HTML characters
         for ( int i = 0; i < n.tokenImage.length(); ++i ) {
            char c = n.tokenImage.charAt(i);
            if ( c == '<' ) out.print("&lt;");
            else if ( c == '>' ) out.print("&gt;");
            else out.print(c);
         }
      }
   }

   public void visit(LocalLookahead n) {
      visitedLookahead = true;
   }

   public void visit(ExpansionChoices n) {
      if ( !n.f1.present() ) {
         n.f0.accept(this);
         return;
      }

      if ( nestLevel == 0 )
         out.print(spc.spc + " * " + nameGen.curFieldName(Globals.choiceName) +
            " -> ");

      ++nestLevel;
      n.f0.accept(this);
      --nestLevel;

      for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); ) {
         NodeSequence seq = (NodeSequence)e.nextElement();

         if ( nestLevel == 0 ) {
            out.println();
            out.print(spc.spc + " *       ");
         }
         else out.print(" ");

         out.print(seq.elementAt(0) + " ");
         ++nestLevel;
         seq.elementAt(1).accept(this);
         --nestLevel;
      }
   }

   public void visit(Expansion n) {
      if ( !n.f0.present() ) return;

      Enumeration e = n.f0.elements();
      ExpansionUnit unit = (ExpansionUnit)e.nextElement();
      visitedLookahead = false;

      if ( nestLevel == 0 && unit.f0.which != 0 && unit.f0.which != 1 )
         out.print(spc.spc + " * " + nameGen.curFieldName(getUnitName(unit)) +
            " -> ");

      ++nestLevel;
      if ( unit.f0.which != 1) unit.accept(this); // fix for Java block bug
      --nestLevel;

      for ( ; e.hasMoreElements(); ) {
         unit = (ExpansionUnit)e.nextElement();
         if ( !visitedLookahead ) {
            if ( unit.f0.which != 0 && unit.f0.which != 1 ) {
               if ( nestLevel == 0 ) {
                  out.println();
                  out.print(spc.spc + " * " +
                     nameGen.curFieldName(getUnitName(unit)) + " -> ");
               }
               else
                  out.print(" ");
            }
         }
         else visitedLookahead = false;

         ++nestLevel;
         if ( unit.f0.which != 1) unit.accept(this); // fix for Java block bug
         --nestLevel;
      }
   }

   private String getUnitName(ExpansionUnit n) {
      NodeSequence seq;
      switch ( n.f0.which ) {
         //
         // cases 0 and 1 should not occur
         //
         case 2:              // Parenthesized expansion
            seq = (NodeSequence)n.f0.choice;
            NodeOptional ebnfMod = (NodeOptional)seq.elementAt(3);

            if ( ebnfMod.present() ) {
               NodeChoice modChoice = (NodeChoice)ebnfMod.node;
               String mod = ((NodeToken)modChoice.choice).tokenImage;
               return nameGen.getNameForMod(mod);
            }
            else {
               ExpansionChoices ec = (ExpansionChoices)seq.elementAt(1);

               if ( ec.f1.present() ) return Globals.choiceName;
               else                   return Globals.sequenceName;
            }
         case 3:              // Optional expansion
            return Globals.optionalName;
         case 4:              // Normal production
            seq = (NodeSequence)n.f0.choice;
            ExpansionUnitTerm term = (ExpansionUnitTerm)seq.elementAt(1);
            if ( term.f0.which == 0 )
               return Globals.tokenName;
            else {
               NodeSequence s = (NodeSequence)term.f0.choice;
               return ((NodeToken)s.elementAt(0)).tokenImage;
            }
         default:
            Errors.hardErr("n.f0.which = " + String.valueOf(n.f0.which));
            break;
      }

      throw new Error("Error in CommentPrinter.getUnitName()");
   }
}
