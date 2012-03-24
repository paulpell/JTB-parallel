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
 * Class OldFileGenerator generates the interface Visitor.java interface
 * as well as DepthFirstVisitor.java.
 */
public class OldFileGenerator {
   public static final int INDENT_AMT = 3;

   private Vector classList;
   private File nodeDir;
   private File visitorDir;

   /**
    * Vector must contain objects of type ClassInfo
    */
   public OldFileGenerator(Vector classes) {
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
    * Generates the visitor source files.
    */
   public void generateVisitorFile() throws FileExistsException {
      try {
         File file = new File(visitorDir, Globals.visitorName + ".java");

         if ( Globals.noOverwrite && file.exists() )
            throw new FileExistsException(Globals.visitorName + ".java");

         PrintWriter out = new PrintWriter(new FileOutputStream(file), false);
         Spacing spc = new Spacing(INDENT_AMT);

         out.println(Globals.fileHeader(spc));
         out.println();
         out.println(spc.spc + "package " + Globals.visitorPackage + ";");
         if ( !Globals.visitorPackage.equals(Globals.nodePackage) )
            out.println(spc.spc + "import " + Globals.nodePackage + ".*;");
         out.println(spc.spc + "import java.util.*;\n");
         out.println(spc.spc + "/**");
         out.println(spc.spc + " * All void visitors must implement this interface.");
         out.println(spc.spc + " */\n");
         out.println(spc.spc + "public interface " + Globals.visitorName + " {\n");
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
            out.println("(" + name + " n);\n");
         }

         spc.updateSpc(-1);
         out.println(spc.spc + "}\n");
         out.flush();
         out.close();
      }
      catch (IOException e) {
         Errors.hardErr(e);
      }
   }

   private void printAutoVisitorMethods(PrintWriter out) {
      out.println("   //");
      out.println("   // void Auto class visitors");
      out.println("   //\n");

      out.print(OldAutoClasses.getNodeListVisitorStr());
      out.print(OldAutoClasses.getNodeListOptionalVisitorStr());
      out.print(OldAutoClasses.getNodeOptionalVisitorStr());
      out.print(OldAutoClasses.getNodeSequenceVisitorStr());
      out.print(OldAutoClasses.getNodeTokenVisitorStr());
      out.println();
   }
}