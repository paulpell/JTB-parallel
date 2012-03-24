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

/**
 * Class TreeDumperBuilder generates the TreeDumper visitor which
 * simply prints all the tokens in the tree at the locations given in their
 * beginLine and beginColumn member variables.
 *
 * Similar to EDU.purdue.jtb.misc.FileGenerator class.
 */
public class TreeDumperBuilder {
   public static final String visitorName = "TreeDumper";
   public static final String outFilename = visitorName + ".java";
   private File visitorDir;

   public TreeDumperBuilder() {
      visitorDir = new File(Globals.visitorDir);

      if ( !visitorDir.exists() )
         visitorDir.mkdir();
   }

   //
   // Visitor generation methods
   //
   public void generateTreeDumper() throws FileExistsException {
      try {
         File file = new File(visitorDir, outFilename);

         if ( Globals.noOverwrite && file.exists() )
            throw new FileExistsException(outFilename);

         PrintWriter out = new PrintWriter(new FileOutputStream(file), false);

         out.println(Globals.fileHeader());
         out.print(
"package " + Globals.visitorPackage + ";\n\n" +
"import " + Globals.nodePackage + ".*;\n" +
"import java.util.*;\n" +
"import java.io.*;\n\n" +
"/**\n" +
" * Dumps the syntax tree to a Writer using the location information in\n" +
" * each NodeToken.\n" +
" */\n" +
"public class TreeDumper extends " +
   OldDepthFirstVisitorBuilder.visitorName + " {\n" +
"   protected PrintWriter out;\n" +
"   private int curLine = 1;\n" +
"   private int curColumn = 1;\n" +
"   private boolean startAtNextToken = false;\n" +
"   private boolean printSpecials = true;\n\n" +

"   /**\n" +
"    * The default constructor uses System.out as its output location.\n" +
"    * You may specify your own Writer or OutputStream using one of the\n" +
"    * other constructors.\n" +
"    */\n" +
"   public TreeDumper()       { out = new PrintWriter(System.out, true); }\n" +
"   public TreeDumper(Writer o)        { out = new PrintWriter(o, true); }\n" +
"   public TreeDumper(OutputStream o)  { out = new PrintWriter(o, true); }\n\n" +

"   /**\n" +
"    * Flushes the OutputStream or Writer that this TreeDumper is using.\n" +
"    */\n" +
"   public void flushWriter()        { out.flush(); }\n\n" +

"   /**\n" +
"    * Allows you to specify whether or not to print special tokens.\n" +
"    */\n" +
"   public void printSpecials(boolean b)   { printSpecials = b; }\n\n" +

"   /**\n" +
"    * Starts the tree dumper on the line containing the next token\n" +
"    * visited.  For example, if the next token begins on line 50 and the\n" +
"    * dumper is currently on line 1 of the file, it will set its current\n" +
"    * line to 50 and continue printing from there, as opposed to\n" +
"    * printing 49 blank lines and then printing the token.\n" +
"    */\n" +
"   public void startAtNextToken()   { startAtNextToken = true; }\n\n" +

"   /**\n" +
"    * Resets the position of the output \"cursor\" to the first line and\n" +
"    * column.  When using a dumper on a syntax tree more than once, you\n" +
"    * either need to call this method or startAtNextToken() between each\n" +
"    * dump.\n" +
"    */\n" +
"   public void resetPosition()      { curLine = curColumn = 1; }\n\n" +

"   /**\n" +
"    * Dumps the current NodeToken to the output stream being used.\n" +
"    *\n" +
"    * @throws  IllegalStateException   if the token position is invalid\n" +
"    *   relative to the current position, i.e. its location places it\n" +
"    *   before the previous token.\n" +
"    */\n");

   out.print("   public void visit(NodeToken n) {\n");

   out.print(
"      if ( n.beginLine == -1 || n.beginColumn == -1 ) {\n" +
"         printToken(n.tokenImage);\n" +
"         return;\n" +
"      }\n\n" +

"      //\n" +
"      // Handle special tokens\n" +
"      //\n" +
"      if ( printSpecials && n.numSpecials() > 0 )\n" +
"         for ( Enumeration<NodeToken> e = n.specialTokens.elements(); e.hasMoreElements(); )\n" +
"            visit(e.nextElement());\n\n" +

"      //\n" +
"      // Handle startAtNextToken option\n" +
"      //\n" +
"      if ( startAtNextToken ) {\n" +
"         curLine = n.beginLine;\n" +
"         curColumn = 1;\n" +
"         startAtNextToken = false;\n\n" +
"         if ( n.beginColumn < curColumn )\n" +
"            out.println();\n" +
"      }\n\n" +

"      //\n" +
"      // Check for invalid token position relative to current position.\n" +
"      //\n" +
"      if ( n.beginLine < curLine )\n" +
"         throw new IllegalStateException(\"at token \\\"\" + n.tokenImage +\n"+
"            \"\\\", n.beginLine = \" + Integer.toString(n.beginLine) +\n"+
"            \", curLine = \" + Integer.toString(curLine));\n" +
"      else if ( n.beginLine == curLine && n.beginColumn < curColumn )\n" +
"         throw new IllegalStateException(\"at token \\\"\" + n.tokenImage +\n"+
"            \"\\\", n.beginColumn = \" +\n" +
"            Integer.toString(n.beginColumn) + \", curColumn = \" +\n" +
"            Integer.toString(curColumn));\n\n" +

"      //\n" +
"      // Move output \"cursor\" to proper location, then print the token\n" +
"      //\n" +
"      if ( curLine < n.beginLine ) {\n" +
"         curColumn = 1;\n" +
"         for ( ; curLine < n.beginLine; ++curLine )\n" +
"            out.println();\n" +
"      }\n\n" +

"      for ( ; curColumn < n.beginColumn; ++curColumn )\n" +
"         out.print(\" \");\n\n" +

"      printToken(n.tokenImage);\n" +
"   }\n\n" +

"   private void printToken(String s) {\n" +
"      for ( int i = 0; i < s.length(); ++i ) { \n" +
"         if ( s.charAt(i) == '\\n' ) {\n" +
"            ++curLine;\n" +
"            curColumn = 1;\n" +
"         }\n" +
"         else\n" +
"            curColumn++;\n\n" +

"         out.print(s.charAt(i));\n" +
"      }\n\n" +

"      out.flush();\n" +
"   }\n" +
"}\n");
         out.flush();
      }
      catch (IOException e) {
         Errors.hardErr("Could not generate " + outFilename);
      }
   }
}
