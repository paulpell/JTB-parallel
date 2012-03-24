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

package EDU.purdue.jtb.misc.toolkit;

import java.util.*;
import java.io.*;
import EDU.purdue.jtb.misc.*;

/**
 * Class DepthFirstGenerator generates the ObjectDepthFirst visitor which
 * has Object visit() methods and simply visits all the nodes in a tree.
 * Duplicates the functionality of the Visitor class in versions of JTB
 * before 1.1pre3.
 *
 * Similar to EDU.purdue.jtb.misc.FileGenerator class.
 */
public class GJVoidDepthFirstBuilder {
   public static final String visitorName = "GJVoidDepthFirst";
   public static final String outFilename = visitorName + ".java";
   public static final int INDENT_AMT = 3;

   private Vector classList;
   private File visitorDir;

   //
   // Vector must contain objects of type ClassInfo
   //
   public GJVoidDepthFirstBuilder(Vector classes) {
      classList = classes;
      visitorDir = new File(Globals.visitorDir);

      if ( !visitorDir.exists() )
         visitorDir.mkdir();
   }

   //
   // Visitor generation methods
   //
   public void generateDepthFirstVisitor() throws FileExistsException {
      try {
         File file = new File(visitorDir, outFilename);

         if ( Globals.noOverwrite && file.exists() )
            throw new FileExistsException(outFilename);

         PrintWriter out = new PrintWriter(new FileOutputStream(file), false);
         Spacing spc = new Spacing(INDENT_AMT);

         out.println(Globals.fileHeader(spc));
         out.println();
         out.println(spc.spc + "package " + Globals.visitorPackage + ";");
         if ( !Globals.visitorPackage.equals(Globals.nodePackage) )
            out.println(spc.spc + "import " + Globals.nodePackage + ".*;");
         out.println(spc.spc + "import java.util.*;\n");
         out.println(spc.spc + "/**");
         out.println(spc.spc + " * Provides default methods which visit each " +
                     "node in the tree in depth-first");
         out.println(spc.spc + " * order.  Your visitors may extend this class.");
         out.println(spc.spc + " */");
         out.println(spc.spc + "public class " + visitorName +
            "<A> implements " + Globals.GJVoidVisitorName + "<A> {");
         printAutoVisitorMethods(out);
         
         spc.updateSpc(+1);
         out.println(spc.spc + "//");
         out.println(spc.spc + "// User-generated visitor methods below");
         out.println(spc.spc + "//");
         out.println();

         for ( Enumeration e = classList.elements(); e.hasMoreElements(); ) {
            ClassInfo cur = (ClassInfo)e.nextElement();
            String name = cur.getName();

            out.println(spc.spc + "/**");
            if ( Globals.javaDocComments ) out.println(spc.spc + " * <PRE>");
            out.println(cur.getEbnfProduction(spc));
            if ( Globals.javaDocComments ) out.println(spc.spc + " * </PRE>");
            out.println(spc.spc + " */");
            out.print(spc.spc + "public void visit");
            out.println("(" + name + " n, A argu) {");

            spc.updateSpc(+1);
            for ( Enumeration f = cur.getNameList().elements();
                  f.hasMoreElements(); )
               out.println(spc.spc + "n." + (String)f.nextElement() +
                           ".accept(this, argu);");
            spc.updateSpc(-1);
            out.println(spc.spc + "}\n");
         }

         spc.updateSpc(-1);
         out.println(spc.spc + "}");

         out.flush();
         out.close();
      }
      catch (IOException e) {
         Errors.hardErr("Could not generate " + outFilename);
      }
   }

   private void printAutoVisitorMethods(PrintWriter out) {
      out.println("   //");
      out.println("   // Auto class visitors--probably don't need to be " +
                  "overridden.");
      out.println("   //");

      out.println(getNodeListVisitorStr());
      out.println(getNodeListOptionalVisitorStr());
      out.println(getNodeOptionalVisitorStr());
      out.println(getNodeSequenceVisitorStr());
      out.println(getNodeTokenVisitorStr());
   }

   //
   // The visitor methods for the auto classes
   //
   static String getNodeListVisitorStr() {
      StringBuffer buf = new StringBuffer(240);
      
	  buf.append("   public void visit(NodeList n, A argu) {\n");

      buf.append(
		 "      int _count=0;\n" +
         "      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {\n" +
         "         e.nextElement().accept(this,argu);\n" +
		 "         _count++;\n" +
		 "      }\n" +
         "   }\n");

      return buf.toString();
   }

   static String getNodeListOptionalVisitorStr() {
      StringBuffer buf = new StringBuffer(300);

      buf.append("   public void visit(NodeListOptional n, A argu) {\n");

      buf.append(
         "      if ( n.present() ) {\n" +
		 "         int _count=0;\n" +
         "         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {\n" +
         "            e.nextElement().accept(this,argu);\n" +
		 "            _count++;\n" +
		 "         }\n" +
		 "      }\n" +
         "   }\n");

      return buf.toString();
   }

   static String getNodeOptionalVisitorStr() {
      StringBuffer buf = new StringBuffer(250);

      buf.append("   public void visit(NodeOptional n, A argu) {\n");

      buf.append(
         "      if ( n.present() )\n" +
         "         n.node.accept(this,argu);\n" +
         "   }\n");

      return buf.toString();
   }

   static String getNodeSequenceVisitorStr() {
      StringBuffer buf = new StringBuffer(250);

      buf.append("   public void visit(NodeSequence n, A argu) {\n");

      buf.append(
		 "      int _count=0;\n" +
         "      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {\n" +
         "         e.nextElement().accept(this,argu);\n" +
		 "         _count++;\n" +
		 "      }\n" +
         "   }\n");

      return buf.toString();
   }

   static String getNodeTokenVisitorStr() {
      return "   public void visit(NodeToken n, A argu) {}\n";
   }
}
