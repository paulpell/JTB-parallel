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

import java.util.*;

/**
 * Class AutoClasses simply contains string representations of the automatic
 * classes and Object visitors.
 */
class AutoClasses {
   static final String packageName = Globals.nodePackage;

   static String parentPointerCode() {
      if ( Globals.parentPointers )
         return
            "   public void setParent(Node n) { parent = n; }\n" +
            "   public Node getParent()       { return parent; }\n\n" +
            "   private Node parent;\n";
      else return "";
   }

   static String getNodeClassStr() {
      StringBuffer buf = new StringBuffer(
         "package " + packageName + ";\n\n" +
         "/**\n" +
         " * The interface which all syntax tree classes must implement.\n" +
         " */\n" +
         "public interface Node extends java.io.Serializable {\n" +
         "   public void accept(" + Globals.visitorPackage + ".Visitor v);\n" +
         "   public <R,A> R accept(" + Globals.visitorPackage + ".GJVisitor<R,A> v, A argu);\n" +
         "   public <R> R accept(" + Globals.visitorPackage + ".GJNoArguVisitor<R> v);\n" +
         "   public <A> void accept(" + Globals.visitorPackage + ".GJVoidVisitor<A> v, A argu);\n" +
         "   public void accept(" + Globals.visitorPackage + ".ThreadedVisitor v, boolean parallel);\n");

      if ( Globals.parentPointers )
         buf.append(
            "   // It is the responsibility of each implementing class to call\n" +
            "   // setParent() on each of its child Nodes.\n" +
            "   public void setParent(Node n);\n" +
            "   public Node getParent();\n");

      buf.append("}\n");
      return buf.toString();
   }

   static String getNodeListInterfaceClassStr() {
      return
         "package " + packageName + ";\n\n" +
         "/**\n" +
         " * The interface which NodeList, NodeListOptional, and NodeSequence\n" +
         " * implement.\n" +
         " */\n" +
         "public interface NodeListInterface extends Node {\n" +
         "   public void addNode(Node n);\n" +
         "   public Node elementAt(int i);\n" +
         "   public java.util.Enumeration<Node> elements();\n" +
         "   public int size();\n\n" +
         "   public void accept(" + Globals.visitorPackage + ".Visitor v);\n" +
         "   public <R,A> R accept(" + Globals.visitorPackage + ".GJVisitor<R,A> v, A argu);\n" +
         "   public <R> R accept(" + Globals.visitorPackage + ".GJNoArguVisitor<R> v);\n" +
         "   public <A> void accept(" + Globals.visitorPackage + ".GJVoidVisitor<A> v, A argu);\n" +
         "   public void accept( "+  Globals.visitorPackage + ".ThreadedVisitor v, boolean parallel);\n" +
         "}\n";
   }

   static String getNodeChoiceClassStr() {
      return 
         "package " + packageName + ";\n\n" +
         "/**\n" +
         " * Represents a grammar choice, e.g. ( A | B )\n" +
         " */\n" +
         "public class NodeChoice" +
            (Globals.nodeSuperclass != null ?
            " extends " + Globals.nodeSuperclass : "") +
            " implements Node {\n" +
         "   public NodeChoice(Node node) {\n" +
         "      this(node, -1);\n" +
         "   }\n\n" +
         "   public NodeChoice(Node node, int whichChoice) {\n" +
         "      choice = node;\n" +
         (Globals.parentPointers ? "      choice.setParent(this);\n" : "") +
         "      which = whichChoice;\n" +
         "   }\n\n" +
         "   public void accept(" + Globals.visitorPackage + ".Visitor v) {\n" +
         "      choice.accept(v);\n" +
         "   }\n" +
         "   public <R,A> R accept(" + Globals.visitorPackage + ".GJVisitor<R,A> v, A argu) {\n" +
         "      return choice.accept(v,argu);\n" +
		 "   }\n" +
         "   public <R> R accept(" + Globals.visitorPackage + ".GJNoArguVisitor<R> v) {\n" +
         "      return choice.accept(v);\n" +
		 "   }\n" +
         "   public <A> void accept(" + Globals.visitorPackage + ".GJVoidVisitor<A> v, A argu) {\n" +
         "      choice.accept(v,argu);\n" +
         "   }\n" +
         "   public void accept(final " + Globals.visitorPackage + ".ThreadedVisitor v, boolean parallel) {\n" +
         "     if (parallel)\n" +
         "       v.addTask(new Runnable() {public void run() {v.visit(NodeChoice.this); v.taskEnd();}});\n" +
         "     else\n" +
         "       v.visit(this);\n" +
         "   }\n\n" +
         parentPointerCode() +
         "   public Node choice;\n" +
         "   public int which;\n" +
         "}\n";
   }

   static String getNodeListClassStr() {
      StringBuffer buf = new StringBuffer(
         "package " + packageName + ";\n\n" +
         "import java.util.*;\n\n" +
         "/**\n" +
         " * Represents a grammar list, e.g. ( A )+\n" +
         " */\n" +
         "public class NodeList" +
            (Globals.nodeSuperclass != null ?
            " extends " + Globals.nodeSuperclass : "") +
            " implements NodeListInterface {\n" +
         "   public NodeList() {\n" +
         "      nodes = new Vector<Node>();\n" +
         "   }\n\n" +
         "   public NodeList(Node firstNode) {\n" +
         "      nodes = new Vector<Node>();\n" +
         "      addNode(firstNode);\n" +
         "   }\n\n" +
         "   public void addNode(Node n) {\n" +
         "      nodes.addElement(n);\n" +
         (Globals.parentPointers ? "      n.setParent(this);\n" : "") +
         "   }\n\n" +
         "   public Enumeration<Node> elements() { return nodes.elements(); }\n" +
         "   public Node elementAt(int i)  { return nodes.elementAt(i); }\n"+
         "   public int size()             { return nodes.size(); }\n" +
         "   public void accept(" + Globals.visitorPackage + ".Visitor v) {\n" +
         "      v.visit(this);\n" +
         "   }\n" +
         "   public <R,A> R accept(" + Globals.visitorPackage + ".GJVisitor<R,A> v, A argu) {\n" +
         "      return v.visit(this,argu);\n" +
		 "   }\n" +
         "   public <R> R accept(" + Globals.visitorPackage + ".GJNoArguVisitor<R> v) {\n" +
         "      return v.visit(this);\n" +
		 "   }\n" +
         "   public <A> void accept(" + Globals.visitorPackage + ".GJVoidVisitor<A> v, A argu) {\n" +
         "      v.visit(this,argu);\n" +
         "   }\n" +
         "   public void accept(final " + Globals.visitorPackage + ".ThreadedVisitor v, boolean parallel) {\n" +
         "     if (parallel)\n" +
         "       v.addTask(new Runnable() {public void run() {v.visit(NodeList.this); v.taskEnd();}});\n" +
         "     else\n" +
         "       v.visit(this);\n" +
         "   }\n\n" +
         parentPointerCode() +
         "   public Vector<Node> nodes;\n" +
         "}\n");

      return buf.toString();
   }

   static String getNodeListOptionalClassStr() {
      StringBuffer buf = new StringBuffer(
         "package " + packageName + ";\n\n" +
         "import java.util.*;\n\n" +
         "/**\n" +
         " * Represents an optional grammar list, e.g. ( A )*\n" +
         " */\n" +
         "public class NodeListOptional" +
            (Globals.nodeSuperclass != null ?
            " extends " + Globals.nodeSuperclass : "") +
            " implements NodeListInterface {\n" +
         "   public NodeListOptional() {\n" +
         "      nodes = new Vector<Node>();\n" +
         "   }\n\n" +
         "   public NodeListOptional(Node firstNode) {\n" +
         "      nodes = new Vector<Node>();\n" +
         "      addNode(firstNode);\n" +
         "   }\n\n" +
         "   public void addNode(Node n) {\n" +
         "      nodes.addElement(n);\n" +
         (Globals.parentPointers ? "      n.setParent(this);\n" : "") +
         "   }\n\n" +
         "   public Enumeration<Node> elements() { return nodes.elements(); }\n" +
         "   public Node elementAt(int i)  { return nodes.elementAt(i); }\n"+
         "   public int size()             { return nodes.size(); }\n" +
         "   public boolean present()      { return nodes.size() != 0; }\n" +
         "   public void accept(" + Globals.visitorPackage + ".Visitor v) {\n" +
         "      v.visit(this);\n" +
         "   }\n" +
         "   public <R,A> R accept(" + Globals.visitorPackage + ".GJVisitor<R,A> v, A argu) {\n" +
         "      return v.visit(this,argu);\n" +
		 "   }\n" +
         "   public <R> R accept(" + Globals.visitorPackage + ".GJNoArguVisitor<R> v) {\n" +
         "      return v.visit(this);\n" +
		 "   }\n" +
         "   public <A> void accept(" + Globals.visitorPackage + ".GJVoidVisitor<A> v, A argu) {\n" +
         "      v.visit(this,argu);\n" +
         "   }\n" +
         "   public void accept(final " + Globals.visitorPackage + ".ThreadedVisitor v, boolean parallel) {\n" +
         "     if (parallel)\n" +
         "       v.addTask(new Runnable() {public void run() {v.visit(NodeListOptional.this); v.taskEnd();}});\n" +
         "     else\n" +
         "       v.visit(this);\n" +
         "   }\n\n" +
         parentPointerCode() +
         "   public Vector<Node> nodes;\n" +
         "}\n");

      return buf.toString();
   }

   static String getNodeOptionalClassStr() {
      StringBuffer buf = new StringBuffer(
         "package " + packageName + ";\n\n" +
         "/**\n" +
         " * Represents an grammar optional node, e.g. ( A )? or [ A ]\n" +
         " */\n" +
         "public class NodeOptional" +
            (Globals.nodeSuperclass != null ?
            " extends " + Globals.nodeSuperclass : "") +
            " implements Node {\n" +
         "   public NodeOptional() {\n" +
         "      node = null;\n" +
         "   }\n\n" +
         "   public NodeOptional(Node n) {\n" +
         "      addNode(n);\n" +
         "   }\n\n" +
         "   public void addNode(Node n)  {\n" +
         "      if ( node != null)                // Oh oh!\n" +
         "         throw new Error(\"Attempt to set optional node twice\");\n\n" +
         "      node = n;\n" +
         (Globals.parentPointers ? "      n.setParent(this);\n" : "") +
         "   }\n" +
         "   public void accept(" + Globals.visitorPackage + ".Visitor v) {\n" +
         "      v.visit(this);\n" +
         "   }\n" +
         "   public <R,A> R accept(" + Globals.visitorPackage + ".GJVisitor<R,A> v, A argu) {\n" +
         "      return v.visit(this,argu);\n" +
		 "   }\n" +
         "   public <R> R accept(" + Globals.visitorPackage + ".GJNoArguVisitor<R> v) {\n" +
         "      return v.visit(this);\n" +
		 "   }\n" +
         "   public <A> void accept(" + Globals.visitorPackage + ".GJVoidVisitor<A> v, A argu) {\n" +
         "      v.visit(this,argu);\n" +
		 "   }\n" +
         "   public void accept(final " + Globals.visitorPackage + ".ThreadedVisitor v, boolean parallel) {\n" +
         "     if (parallel)\n" +
         "       v.addTask(new Runnable() {public void run() {v.visit(NodeOptional.this); v.taskEnd();}});\n" +
         "     else\n" +
         "       v.visit(this);\n" +
         "   }\n" +
         "   public boolean present()   { return node != null; }\n\n" +
         parentPointerCode() +
         "   public Node node;\n" +
         "}\n");

      return buf.toString();
   }

   static String getNodeSequenceClassStr() {
      StringBuffer buf = new StringBuffer(
         "package " + packageName + ";\n\n" +
         "import java.util.*;\n\n" +
         "/**\n" +
         " * Represents a sequence of nodes nested within a choice, list,\n" +
         " * optional list, or optional, e.g. ( A B )+ or [ C D E ]\n" +
         " */\n" +
         "public class NodeSequence" +
            (Globals.nodeSuperclass != null ?
            " extends " + Globals.nodeSuperclass : "") +
            " implements NodeListInterface {\n" +
         "   public NodeSequence(int n) {\n" +
         "      nodes = new Vector<Node>(n);\n" +
         "   }\n\n" +
         "   public NodeSequence(Node firstNode) {\n" +
         "      nodes = new Vector<Node>();\n" +
         "      addNode(firstNode);\n" +
         "   }\n\n" +
         "   public void addNode(Node n) {\n" +
         "      nodes.addElement(n);\n" +
         (Globals.parentPointers ? "      n.setParent(this);\n" : "") +
         "   }\n\n" +
         "   public Node elementAt(int i)  { return nodes.elementAt(i); }\n"+
         "   public Enumeration<Node> elements() { return nodes.elements(); }\n" +
         "   public int size()             { return nodes.size(); }\n" +
		 "   public void accept(" + Globals.visitorPackage + ".Visitor v) {\n" +
		 "      v.visit(this);\n" +
		 "   }\n" +
         "   public <R,A> R accept(" + Globals.visitorPackage + ".GJVisitor<R,A> v, A argu) {\n" +
		 "      return v.visit(this,argu);\n" +
		 "   }\n" +
         "   public <R> R accept(" + Globals.visitorPackage + ".GJNoArguVisitor<R> v) {\n" +
		 "      return v.visit(this);\n" +
		 "   }\n" +
         "   public <A> void accept(" + Globals.visitorPackage + ".GJVoidVisitor<A> v, A argu) {\n" +
		 "      v.visit(this,argu);\n" +
		 "   }\n" +
         "   public void accept(final " + Globals.visitorPackage + ".ThreadedVisitor v, boolean parallel) {\n" +
         "     if (parallel)\n" +
         "       v.addTask(new Runnable() {public void run() {v.visit(NodeSequence.this); v.taskEnd();}});\n" +
         "     else\n" +
         "       v.visit(this);\n" +
         "   }\n\n" +
         parentPointerCode() +
         "   public Vector<Node> nodes;\n" +
         "}\n");

      return buf.toString();
   }

   static String getNodeTokenClassStr() {
      StringBuffer buf = new StringBuffer(
         "package " + packageName + ";\n\n" +
         "import java.util.*;\n" +
         "/**\n" +
         " * Represents a single token in the grammar.  If the \"-tk\" option\n" +
         " * is used, also contains a Vector of preceding special tokens.\n" +
         " */\n" +
         "public class NodeToken" +
            (Globals.nodeSuperclass != null ?
            " extends " + Globals.nodeSuperclass : "") +
            " implements Node {\n" +
         "   public NodeToken(String s) {\n" +
         "      this(s, -1, -1, -1, -1, -1); " +
         "   }\n\n" +
         "   public NodeToken(String s, int kind, int beginLine, int beginColumn, int endLine, int endColumn) {\n" +
         "      tokenImage = s;\n" +
         "      specialTokens = null;\n" +
         "      this.kind = kind;\n" +
         "      this.beginLine = beginLine;\n" +
         "      this.beginColumn = beginColumn;\n" +
         "      this.endLine = endLine;\n" +
         "      this.endColumn = endColumn;\n" +
         "   }\n\n" +
         "   public NodeToken getSpecialAt(int i) {\n" +
         "      if ( specialTokens == null )\n" +
         "         throw new java.util.NoSuchElementException(\"No specials in token\");\n" +
         "      return specialTokens.elementAt(i);\n" +
         "   }\n\n" +
         "   public int numSpecials() {\n" +
         "      if ( specialTokens == null ) return 0;\n" +
         "      return specialTokens.size();\n" +
         "   }\n\n" +
         "   public void addSpecial(NodeToken s) {\n" +
         "      if ( specialTokens == null ) specialTokens = new Vector<NodeToken>();\n" +
         "      specialTokens.addElement(s);\n" +
         (Globals.parentPointers ? "      s.setParent(this);\n" : "") +
         "   }\n\n" +
         "   public void trimSpecials() {\n" +
         "      if ( specialTokens == null ) return;\n" +
         "      specialTokens.trimToSize();\n" +
         "   }\n\n" +
         "   public String toString()     { return tokenImage; }\n\n" +
         "   public String withSpecials() {\n" +
         "      if ( specialTokens == null )\n" +
         "          return tokenImage;\n\n" +
         "       StringBuffer buf = new StringBuffer();\n\n" +
         "       for ( Enumeration<NodeToken> e = specialTokens.elements(); e.hasMoreElements(); )\n" +
         "          buf.append(e.nextElement().toString());\n\n" +
         "       buf.append(tokenImage);\n" +
         "       return buf.toString();\n" +
         "   }\n\n" +
		 "   public void accept(" + Globals.visitorPackage + ".Visitor v) {\n" +
		 "      v.visit(this);\n" +
		 "   }\n" +
         "   public <R,A> R accept(" + Globals.visitorPackage + ".GJVisitor<R,A> v, A argu) {\n" +
		 "      return v.visit(this,argu);\n" +
		 "   }\n" +
         "   public <R> R accept(" + Globals.visitorPackage + ".GJNoArguVisitor<R> v) {\n" +
		 "      return v.visit(this);\n" +
		 "   }\n" +
         "   public <A> void accept(" + Globals.visitorPackage + ".GJVoidVisitor<A> v, A argu) {\n" +
		 "      v.visit(this,argu);\n" +
		 "   }\n" +
         "   public void accept(final " + Globals.visitorPackage + ".ThreadedVisitor v, boolean parallel) {\n" +
         "     if (parallel)\n" +
         "       v.addTask(new Runnable() {public void run() {v.visit(NodeToken.this); v.taskEnd();}});\n" +
         "     else\n" +
         "       v.visit(this);\n" +
         "   }\n\n" +
         parentPointerCode() +
         "   public String tokenImage;\n\n" +
         "   // Stores a list of NodeTokens\n" +
         "   public Vector<NodeToken> specialTokens;\n\n" +
         "   // -1 for these ints means no position info is available.\n" +
         "   public int beginLine, beginColumn, endLine, endColumn;\n\n" +
         "   // Equal to the JavaCC token \"kind\" integer.\n" +
         "   // -1 if not available.\n" +
         "   public int kind;\n" +
         "}\n");

      return buf.toString();
   }

   //
   // The GJ visitor methods for the auto classes
   //
   static String getNodeListVisitorStr() {
      return "   public R visit(NodeList n, A argu);\n";
   }

   static String getNodeListOptionalVisitorStr() {
      return "   public R visit(NodeListOptional n, A argu);\n";
   }

   static String getNodeOptionalVisitorStr() {
      return "   public R visit(NodeOptional n, A argu);\n";
   }

   static String getNodeSequenceVisitorStr() {
      return "   public R visit(NodeSequence n, A argu);\n";
   }

   static String getNodeTokenVisitorStr() {
      return "   public R visit(NodeToken n, A argu);\n";
   }
}
