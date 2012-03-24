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
import java.io.*;

/**
 * Class FileGenerator generates the <class>.java files and the generic
 * interface GJVisitor.java as well as GJDepthFirst.java.
 */
public class FileGenerator {
   public static final int INDENT_AMT = 3;

   private Vector classList;
   private File nodeDir;
   private File visitorDir;

   /**
    * Vector must contain objects of type ClassInfo
    */
   public FileGenerator(Vector classes) {
      classList = classes;
      
      nodeDir = new File(Globals.nodeDir);
      visitorDir = new File(Globals.visitorDir);

      if ( !nodeDir.exists() )
         nodeDir.mkdir();
      else if ( !nodeDir.isDirectory() )
         Errors.softErr("\"" + Globals.nodeDir + "\" exists but is not a " +
                        "directory.");

      if ( !visitorDir.exists() )
         visitorDir.mkdir();
      else if ( !visitorDir.isDirectory() )
         Errors.softErr("\"" + Globals.visitorDir + "\" exists but is not a " +
                        "directory.");
   }

   /**
    * Method to list the classes generated in an easier-to-read form
    */
   public void printClassList(PrintWriter out) {
      Spacing spc = new Spacing(INDENT_AMT);

      for ( Enumeration e = classList.elements(); e.hasMoreElements(); ) {
         ClassInfo cur = (ClassInfo)e.nextElement();
         String name = cur.getName();

         out.println(spc.spc + "class " + name + ":");
         spc.updateSpc(+1);

         Enumeration types = cur.getTypeList().elements();
         Enumeration names = cur.getNameList().elements();

         for ( ; types.hasMoreElements(); )
            out.println(spc.spc + (String)types.nextElement() + " " +
                        (String)names.nextElement());

         out.println();
         spc.updateSpc(-1);
      }

      out.flush();
   }

   /**
    * Generates node class source files.
    */
   public void generateClassFiles() throws FileExistsException {
      try {
         boolean exists = false;
         Spacing spc = new Spacing(INDENT_AMT);

         for ( Enumeration e = classList.elements(); e.hasMoreElements(); ) {
            ClassInfo cur = (ClassInfo)e.nextElement();
            File file = new File(nodeDir, cur.getName() + ".java");
            
            if ( Globals.noOverwrite && file.exists() ) {
               exists = true;
               break;
            }

            PrintWriter out = new PrintWriter(new FileOutputStream(file),
                                              false);

            out.println(spc.spc + Globals.fileHeader(spc));
            out.println();
            out.println(spc.spc + "package " + Globals.nodePackage + ";");
            out.println();
            out.println(spc.spc + "/**");
            out.println(spc.spc + " * Grammar production:");

            if ( Globals.javaDocComments ) out.println(spc.spc + " * <PRE>");
            out.println(spc.spc + cur.getEbnfProduction(spc));
            if ( Globals.javaDocComments ) out.println(spc.spc + " * </PRE>");
            out.println(spc.spc + " */");
            out.println(cur.getClassString(spc));
            
            out.flush();
            out.close();
         }

         if ( Globals.noOverwrite && exists )
            throw new FileExistsException("one of the generated node classes");
      }
      catch (IOException e) {
         Errors.hardErr(e);
      }
   }

   /**
    * Generates the automatic classes.
    */
   public void generateAutoClassFiles() throws FileExistsException {
      try {
         boolean b = true;
         b = b && printStringToFile("Node.java", AutoClasses.getNodeClassStr());
         b = b && printStringToFile("NodeListInterface.java",
            AutoClasses.getNodeListInterfaceClassStr());
	     b = b && printStringToFile("NodeChoice.java",
            AutoClasses.getNodeChoiceClassStr());
         b = b && printStringToFile("NodeList.java",
            AutoClasses.getNodeListClassStr());
         b = b && printStringToFile("NodeListOptional.java",
            AutoClasses.getNodeListOptionalClassStr());
         b = b && printStringToFile("NodeOptional.java",
            AutoClasses.getNodeOptionalClassStr());
         b = b && printStringToFile("NodeSequence.java",
            AutoClasses.getNodeSequenceClassStr());
         b = b && printStringToFile("NodeToken.java",
            AutoClasses.getNodeTokenClassStr());

         if ( Globals.noOverwrite && !b )
            throw new FileExistsException("one of the automatic node classes");
      }
      catch (IOException e) {
         Errors.hardErr(e);
      }
   }

   // returns false if the file exists and Globals.noOverwrite is true
   private boolean printStringToFile(String fname, String s)
   throws IOException {
      File file = new File(Globals.nodeDir, fname);

      if ( Globals.noOverwrite && file.exists() )
         return false;

      PrintWriter out = new PrintWriter(new FileOutputStream(file), true);
      out.println(Globals.fileHeader());
      out.println();
      out.println(s);
      out.close();
      return true;
   }

   /**
    * Generates the visitor source files.
    */
   public void generateVisitorFile() throws FileExistsException {
      try {
         File file = new File(visitorDir, Globals.GJVisitorName + ".java");

         if ( Globals.noOverwrite && file.exists() )
            throw new FileExistsException(Globals.GJVisitorName + ".java");

         PrintWriter out = new PrintWriter(new FileOutputStream(file), false);
         Spacing spc = new Spacing(INDENT_AMT);

         out.println(Globals.fileHeader(spc));
         out.println();
         out.println(spc.spc + "package " + Globals.visitorPackage + ";");
         if ( !Globals.visitorPackage.equals(Globals.nodePackage) )
            out.println(spc.spc + "import " + Globals.nodePackage + ".*;");
         out.println(spc.spc + "import java.util.*;\n");
         out.println(spc.spc + "/**");
         out.println(spc.spc + " * All GJ visitors must implement this interface.");
         out.println(spc.spc + " */\n");
         out.println(spc.spc + "public interface " + Globals.GJVisitorName + "<R,A> {\n");
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
            out.print(spc.spc + "public R visit");
            out.println("(" + name + " n, A argu);\n");
         }

         spc.updateSpc(-1);
         out.println(spc.spc + "}");
         out.flush();
         out.close();
      }
      catch (IOException e) {
         Errors.hardErr(e);
      }
   }

   private void printAutoVisitorMethods(PrintWriter out) {
      out.println("   //");
      out.println("   // GJ Auto class visitors");
      out.println("   //\n");

      out.print(AutoClasses.getNodeListVisitorStr());
      out.print(AutoClasses.getNodeListOptionalVisitorStr());
      out.print(AutoClasses.getNodeOptionalVisitorStr());
      out.print(AutoClasses.getNodeSequenceVisitorStr());
      out.print(AutoClasses.getNodeTokenVisitorStr());
      out.println();
   }
}