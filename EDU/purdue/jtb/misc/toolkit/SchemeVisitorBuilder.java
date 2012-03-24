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
 * Indiana by Kevin Tao and Jens Palsberg.  No charge may be made
 * for copies, derivations, or distributions of this material
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

/*
 * Class SchemeVisitorBuilder generates a "toolkit" visitor which, given an
 * input file, outputs the scheme version of the tree to a file.
 *
 * This class is similar to the FileGenerator class in the "misc" package.
 *
 * There are several constraints that must be placed on a JavaCC grammar:
 *   -Choices (i.e. ( x | y )) may only occur at the top level of a production,
 *    between nonterminals.
 *   -Whatever goes within a list, optional list, or optional must be a
 *    single non-terminal.
 */
public class SchemeVisitorBuilder {
   public static final String visitorName = "SchemeTreeBuilder";
   public static final String outFilename = visitorName + ".java";
   public static final int INDENT_AMT = 3;

   private Vector classList;
   private File visitorDir;
   private PrintWriter out;

   public SchemeVisitorBuilder(Vector classes) {
      classList = classes;
      visitorDir = new File(Globals.visitorDir);

      if ( !visitorDir.exists() )
         visitorDir.mkdir();
   }

   public void generateSchemeBuilder() throws FileExistsException {
      try {
         File file = new File(visitorDir, outFilename);

         if ( Globals.noOverwrite && file.exists() )
            throw new FileExistsException(outFilename);

         out = new PrintWriter(new FileOutputStream(file), false);
         Spacing spc = new Spacing(INDENT_AMT);
         boolean firstProd = true;

         out.println(Globals.fileHeader(spc));
         out.println();
         out.println(spc.spc + "package " + Globals.visitorPackage + ";");
         if ( !Globals.visitorPackage.equals(Globals.nodePackage) )
            out.println(spc.spc + "import " + Globals.nodePackage + ".*;");
         out.println(spc.spc + "import java.util.*;");
         out.println(spc.spc + "import java.io.*;");
         out.println();
         out.println(spc.spc + "/**");
         out.println(spc.spc + " * Generates a syntax tree in the Scheme " +
                     "language.");
         out.println(spc.spc + " */");
         out.println(spc.spc +
                     "public class " + visitorName + " extends " +
                     OldDepthFirstVisitorBuilder.visitorName + " {");

         spc.updateSpc(+1);
         out.println(spc.spc + "PrintWriter out;");

         out.println();
         printAutoMethods();
         out.println();

         for ( Enumeration e = classList.elements(); e.hasMoreElements(); ) {
            ClassInfo cur = (ClassInfo)e.nextElement();
            String name = cur.getName();
            String typeF1 = (String)cur.getTypeList().elementAt(0);

            out.println(spc.spc + "/**");
            if ( Globals.javaDocComments ) out.println(spc.spc + " * <PRE>");
            out.println(cur.getEbnfProduction(spc));
            if ( Globals.javaDocComments ) out.println(spc.spc + " * </PRE>");
            out.println(spc.spc + " */");
            out.print(spc.spc + "public void visit");
            out.println("(" + name + " n) {");

            spc.updateSpc(+1);

            //
            // -Print leading stuff if it's the first production
            // -Don't print stuff if it's only field is a NodeChoice
            //
            if ( firstProd )
               out.println(spc.spc +
                           "out.print(\"(define root '(" + name + " \");");
            else if ( cur.getNameList().size() > 1 ||
                      !typeF1.equals(Globals.choiceName) )
               out.println(spc.spc + "out.print(\"(" + name + " \");");

            for ( Enumeration f = cur.getNameList().elements();
                  f.hasMoreElements(); )
               out.println(spc.spc + "n." + (String)f.nextElement() +
                           ".accept(this);");

            if ( firstProd ) {
               out.println(spc.spc + "out.print(\")) \");");
               out.println(spc.spc + "out.flush();");
               out.println(spc.spc + "out.close();");
               firstProd = false;
            }
            else if ( cur.getNameList().size() > 1 ||
                      !typeF1.equals(Globals.choiceName) )
               out.println(spc.spc + "out.print(\") \");");


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

   private void printAutoMethods() {
      out.println(

"   public " + visitorName + "() {\n"+
"      this(System.out);\n" +
"   }\n\n" +

"   public " + visitorName + "(Writer w) {\n" +
"      out = new PrintWriter(w);\n" +
"   }\n\n" +

"   public " + visitorName + "(OutputStream o) {\n" +
"      out = new PrintWriter(o);\n" +
"   }\n\n" +

"   private String toSchemeString(String s) {\n" +
"      int len = s.length();\n" +
"      StringBuffer temp = new StringBuffer(s);\n\n" +
"      for ( int i = 0; i < len; i++ )\n" +
"         if ( temp.charAt(i) == '\"' ) {\n" +
"            temp.insert(i, '\\\\');\n" +
"            ++i; ++len;\n" +
"         }\n\n" +
"      return temp.toString();\n" +
"   }\n\n");

out.println(
"   public void visit(NodeList n) {\n" +
"      out.print(\"(\");\n" +
"      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )\n" +
"         e.nextElement().accept(this);\n" +
"      out.print(\") \");\n" +
"   }\n\n" +

"   public void visit(NodeListOptional n) {\n" +
"      out.print(\"( \");\n" +
"      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )\n" +
"         e.nextElement().accept(this);\n" +
"      out.print(\") \");\n" +
"   }\n\n" +

"   public void visit(NodeOptional n) {\n" +
"      out.print(\"(\");\n" +
"      if ( n.present() )\n" +
"         n.node.accept(this);\n" +
"      out.print(\") \");\n" +
"   }\n\n" +

"   public void visit(NodeToken n) {\n" +
"      out.print(\"\\\"\" + toSchemeString(n.tokenImage) + \"\\\" \");\n" + 
"   }\n");
   }
}

