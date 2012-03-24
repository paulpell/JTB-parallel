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
import EDU.purdue.jtb.visitor.Visitor;

/**
 * Class TreeFormatterBuilder generates the TreeFormatter visitor which
 * is a skeleton pretty-printer.  Using some pre-defined methods, a user
 * can quickly and easily create a formatter for their grammar.  The
 * formatter will then take a tree, insert token location information into
 * the NodeTokens of the tree.  TreeDumper can then be used to output the
 * result.
 *
 * Note that unlike the other automatically generated file, since this one
 * must be edited to be useful, JTB will not overwrite this file automatically.
 * JTB will take this precaution for the other files only if the "-ow"
 * command-line parameter is used.
 */
public class TreeFormatterBuilder {
   public static final String visitorName = "TreeFormatter";
   public static final String outFilename = visitorName + ".java";
   private File visitorDir;
   private Vector classList;     // a vector of ClassInfos

   //
   // Vector must contain objects of type ClassInfo
   //
   public TreeFormatterBuilder(Vector classes) {
      classList = classes;
      visitorDir = new File(Globals.visitorDir);

      if ( !visitorDir.exists() )
         visitorDir.mkdir();
   }

   /**
    * Generates the tree formatter template.  Since the user is expected to
    * edit and customize this file, this method will never overwrite the file
    * if it exists, regardless of the Globals.noOverwrite flag.
    */
   public void generateTreeFormatter() throws FileExistsException {
      try {
         File file = new File(visitorDir, outFilename);
         PrintWriter out;

         if ( file.exists() )
            throw new FileExistsException(outFilename);

         out = new PrintWriter(new FileOutputStream(file), false);

         out.println(Globals.fileHeader());
         out.print(
"package " + Globals.visitorPackage + ";\n\n" +
"import " + Globals.nodePackage + ".*;\n" +
"import java.util.*;\n\n" +
"/**\n" +
" * A skeleton output formatter for your language grammar.  Using the\n" +
" * add() method along with force(), indent(), and outdent(), you can\n" +
" * easily specify how this visitor will format the given syntax tree.\n" +
" * See the JTB documentation for more details.\n" +
" *\n" +
" * Pass your syntax tree to this visitor, and then to the TreeDumper\n" +
" * visitor in order to \"pretty print\" your tree.\n" +
" */\n" +
"public class TreeFormatter extends "+
   OldDepthFirstVisitorBuilder.visitorName + " {\n" +
"   private Vector<FormatCommand> cmdQueue = new Vector<FormatCommand>();\n" +
"   private boolean lineWrap;\n" +
"   private int wrapWidth;\n" +
"   private int indentAmt;\n" +
"   private int curLine = 1;\n" +
"   private int curColumn = 1;\n" +
"   private int curIndent = 0;\n\n" +
"   /**\n" +
"    * The default constructor assumes an indentation amount of 3 spaces\n" +
"    * and no line-wrap.  You may alternately use the other constructor to\n" +
"    * specify your own indentation amount and line width.\n" +
"    */\n" +
"   public TreeFormatter() { this(3, 0); }\n\n" +
"   /**\n" +
"    * This constructor accepts an indent amount and a line width which is\n" +
"    * used to wrap long lines.  If a token's beginColumn value is greater\n" +
"    * than the specified wrapWidth, it will be moved to the next line and\n" +
"    * indented one extra level.  To turn off line-wrapping, specify a\n" +
"    * wrapWidth of 0.\n" +
"    *\n" +
"    * @param   indentAmt   Amount of spaces per indentation level.\n" +
"    * @param   wrapWidth   Wrap lines longer than wrapWidth.  0 for no wrap.\n" +
"    */\n" +
"   public TreeFormatter(int indentAmt, int wrapWidth) {\n" +
"      this.indentAmt = indentAmt;\n" +
"      this.wrapWidth = wrapWidth;\n\n" +
"      if ( wrapWidth > 0 )\n" +
"         lineWrap = true;\n" +
"      else\n" +
"         lineWrap = false;\n" +
"   }\n\n" +
"   /**\n" +
"    * Accepts a NodeListInterface object and performs an optional format\n" +
"    * command between each node in the list (but not after the last node).\n" +
"    */\n" +
"   protected void processList(NodeListInterface n) {\n" +
"      processList(n, null);\n" +
"   }\n\n" +
"   protected void processList(NodeListInterface n, FormatCommand cmd) {\n" +
"      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {\n" +
"         e.nextElement().accept(this);\n" +
"         if ( cmd != null && e.hasMoreElements() )\n" +
"            cmdQueue.addElement(cmd);\n" +
"      }\n" +
"   }\n\n" +
"   /**\n" +
"    * A Force command inserts a line break and indents the next line to\n" +
"    * the current indentation level.  Use \"add(force());\".\n" +
"    */\n" +
"   protected FormatCommand force() { return force(1); }\n" +
"   protected FormatCommand force(int i) {\n" +
"      return new FormatCommand(FormatCommand.FORCE, i);\n" +
"   }\n\n" +
"   /**\n" +
"    * An Indent command increases the indentation level by one (or a\n" +
"    * user-specified amount).  Use \"add(indent());\".\n" +
"    */\n" +
"   protected FormatCommand indent() { return indent(1); }\n" +
"   protected FormatCommand indent(int i) {\n" +
"      return new FormatCommand(FormatCommand.INDENT, i);\n" +
"   }\n\n" +
"   /**\n" +
"    * An Outdent command is the reverse of the Indent command: it reduces\n" +
"    * the indentation level.  Use \"add(outdent());\".\n" +
"    */\n" +
"   protected FormatCommand outdent() { return outdent(1); }\n" +
"   protected FormatCommand outdent(int i) {\n" +
"      return new FormatCommand(FormatCommand.OUTDENT, i);\n" +
"   }\n\n" +
"   /**\n" +
"    * A Space command simply adds one or a user-specified number of\n" +
"    * spaces between tokens.  Use \"add(space());\".\n" +
"    */\n" +
"   protected FormatCommand space() { return space(1); }\n" +
"   protected FormatCommand space(int i) {\n" +
"      return new FormatCommand(FormatCommand.SPACE, i);\n" +
"   }\n\n" +
"   /**\n" +
"    * Use this method to add FormatCommands to the command queue to be\n" +
"    * executed when the next token in the tree is visited.\n" +
"    */\n" +
"   protected void add(FormatCommand cmd) {\n" +
"      cmdQueue.addElement(cmd);\n" +
"   }\n\n" +
"   /**\n" +
"    * Executes the commands waiting in the command queue, then inserts the\n" +
"    * proper location information into the current NodeToken.\n" +
"    *\n" +
"    * If there are any special tokens preceding this token, they will be\n" +
"    * given the current location information.  The token will follow on\n" +
"    * the next line, at the proper indentation level.  If this is not the\n" +
"    * behavior you want from special tokens, feel free to modify this\n" +
"    * method.\n" +
"    */\n" +
"   public void visit(NodeToken n) {\n" +
"      for ( Enumeration<FormatCommand> e = cmdQueue.elements(); e.hasMoreElements(); ) {\n" +
"         FormatCommand cmd = e.nextElement();\n" +
"         switch ( cmd.getCommand() ) {\n" +
"         case FormatCommand.FORCE :\n" +
"            curLine += cmd.getNumCommands();\n" +
"            curColumn = curIndent + 1;\n" +
"            break;\n" +
"         case FormatCommand.INDENT :\n" +
"            curIndent += indentAmt * cmd.getNumCommands();\n" +
"            break;\n" +
"         case FormatCommand.OUTDENT :\n" +
"            if ( curIndent >= indentAmt )\n" +
"               curIndent -= indentAmt * cmd.getNumCommands();\n" +
"            break;\n" +
"         case FormatCommand.SPACE :\n" +
"            curColumn += cmd.getNumCommands();\n" +
"            break;\n" +
"         default :\n" +
"            throw new TreeFormatterException(\n" +
"               \"Invalid value in command queue.\");\n" +
"         }\n" +
"      }\n\n" +
"      cmdQueue.removeAllElements();\n\n" +
"      //\n" +
"      // Handle all special tokens preceding this NodeToken\n" +
"      //\n" +
"      if ( n.numSpecials() > 0 )\n" +
"         for ( Enumeration<NodeToken> e = n.specialTokens.elements();\n" +
"               e.hasMoreElements(); ) {\n" +
"            NodeToken special = e.nextElement();\n\n" +
"            //\n" +
"            // -Place the token.\n" +
"            // -Move cursor to next line after the special token.\n" +
"            // -Don't update curColumn--want to keep current indent level.\n" +
"            //\n" +
"            placeToken(special, curLine, curColumn);\n" +
"            curLine = special.endLine + 1;\n" +
"         }\n\n" +
"      placeToken(n, curLine, curColumn);\n" +
"      curLine = n.endLine;\n" +
"      curColumn = n.endColumn;\n" +
"   }\n\n" +
"   /**\n" +
"    * Inserts token location (beginLine, beginColumn, endLine, endColumn)\n" +
"    * information into the NodeToken.  Takes into account line-wrap.\n" +
"    * Does not update curLine and curColumn.\n" +
"    */\n" +
"   private void placeToken(NodeToken n, int line, int column) {\n" +
"      int length = n.tokenImage.length();\n\n" +
"      //\n" +
"      // Find beginning of token.  Only line-wrap for single-line tokens\n" +
"      //\n" +
"      if ( !lineWrap || n.tokenImage.indexOf('\\n') != -1 ||\n" +
"           column + length <= wrapWidth )\n" +
"         n.beginColumn = column;\n" +
"      else {\n" +
"         ++line;\n" +
"         column = curIndent + indentAmt + 1;\n" +
"         n.beginColumn = column;\n" +
"      }\n\n" +
"      n.beginLine = line;\n\n" +
"      //\n" +
"      // Find end of token; don't count \\n if it's the last character\n" +
"      //\n" +
"      for ( int i = 0; i < length; ++i ) {\n" +
"         if ( n.tokenImage.charAt(i) == '\\n' && i < length - 1 ) {\n" +
"            ++line;\n" +
"            column = 1;\n" +
"         }\n" +
"         else\n" +
"            ++column;\n" +
"      }\n\n" +
"      n.endLine = line;\n" +
"      n.endColumn = column;\n" +
"   }\n\n" +
"   //\n" +
"   // User-generated visitor methods below\n" +
"   //\n\n");

         out.flush();
         Spacing spc = new Spacing(3);
         spc.updateSpc(+1);

         for ( Enumeration e = classList.elements(); e.hasMoreElements(); ) {
            ClassInfo cur = (ClassInfo)e.nextElement();
            String className = cur.getName();

            out.println(spc.spc + "/**");
            if ( Globals.javaDocComments ) out.println(spc.spc + " * <PRE>");
            out.println(cur.getEbnfProduction(spc));
            if ( Globals.javaDocComments ) out.println(spc.spc + " * </PRE>");
            out.println(spc.spc + " */");
            out.print(spc.spc + "public void visit");
            out.println("(" + className + " n) {");

            spc.updateSpc(+1);

            Enumeration names = cur.getNameList().elements();
            Enumeration types = cur.getTypeList().elements();

            while ( names.hasMoreElements() && types.hasMoreElements() ) {
               String name = (String)names.nextElement();
               String type = (String)types.nextElement();

               if ( type.equals(Globals.listName) )
                  out.println(spc.spc + "processList(n." + name + ");");
               else if ( type.equals(Globals.listOptName) ) {
                  out.println(spc.spc + "if ( n." + name + ".present() ) {");
                  spc.updateSpc(+1);
                  out.println(spc.spc + "processList(n." + name + ");");
                  spc.updateSpc(-1);
                  out.println(spc.spc + "}");
               }
               else if ( type.equals(Globals.optionalName) ) {
                  out.println(spc.spc + "if ( n." + name + ".present() ) {");
                  spc.updateSpc(+1);
                  out.println(spc.spc + "n." + name + ".accept(this);");
                  spc.updateSpc(-1);
                  out.println(spc.spc + "}");
               }
               else
                  out.println(spc.spc + "n." + name + ".accept(this);");
            }

            spc.updateSpc(-1);
            out.println(spc.spc + "}\n");
         }

         spc.updateSpc(-1);
         out.println(spc.spc + "}\n");

         //
         // Print classes FormatCommand and TreeFormatterExcpetion
         //
         out.print(
"class FormatCommand {\n" +
"   public static final int FORCE = 0;\n" +
"   public static final int INDENT = 1;\n" +
"   public static final int OUTDENT = 2;\n" +
"   public static final int SPACE = 3;\n\n" +
"   private int command;\n" +
"   private int numCommands;\n\n" +
"   FormatCommand(int command, int numCommands) {\n" +
"      this.command = command;\n" +
"      this.numCommands = numCommands;\n" +
"   }\n\n" +
"   public int getCommand()             { return command; }\n" +
"   public int getNumCommands()         { return numCommands; }\n" +
"   public void setCommand(int i)       { command = i; }\n" +
"   public void setNumCommands(int i)   { numCommands = i; }\n" +
"}\n\n" +
"class TreeFormatterException extends RuntimeException {\n" +
"   TreeFormatterException()         { super(); }\n" +
"   TreeFormatterException(String s) { super(s); }\n" +
"}\n");

         out.flush();
      }
      catch (IOException e) {
         Errors.hardErr("Could not generate " + outFilename);
      }
   }
}
