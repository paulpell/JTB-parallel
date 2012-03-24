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
 * Class SchemeRecordBuilder generates the Scheme record definitions of a given
 * input grammar.
 *
 * This class is similar to the FileGenerator class in the "misc" package.
 *
 * There are several constraints that must be placed on a JavaCC grammar:
 *   -Choices (i.e. ( x | y )) may only occur at the top level of a production,
 *    between nonterminals.
 *   -Whatever goes within a list, optional list, or optional must be a
 *    single non-terminal.
 */
public class SchemeRecordBuilder {
   public static final String outFilename = "records.scm";
   public static final int INDENT_AMT = 3;
   public static final int MAX_PER_LINE = 4;

   private Vector classList;
   private PrintWriter out;

   public SchemeRecordBuilder(Vector classes) {
      classList = classes;
   }

   public void generateSchemeRecords() throws FileExistsException {
      try {
         File file = new File(outFilename);

         if ( Globals.noOverwrite && file.exists() )
            throw new FileExistsException(outFilename);

         out = new PrintWriter(new FileOutputStream(file), false);

         for ( Enumeration e = classList.elements(); e.hasMoreElements(); ) {
            ClassInfo cur = (ClassInfo)e.nextElement();
            String name = cur.getName();
            Vector types = cur.getTypeList();
            int numFields = cur.getNameList().size();
            int numPerLine = 1;

            if ( cur.getNameList().size() > 1 ||
                 !types.elementAt(0).equals(Globals.choiceName) ) {
               Hashtable typeCounts = new Hashtable();

               out.print("(define-record " + name + " (");

               for ( int i = 0; i < numFields; ++i ) {

                  //
                  // a precaution only--SchemeSemanticChecker should flag these
                  //
                  if ( types.elementAt(i).equals(Globals.choiceName) )
                     Errors.softErr("In production " + name + ": choices may " +
                        "only occur at the top level.");
                  else if ( types.elementAt(i).equals(Globals.sequenceName) )
                     Errors.softErr("In production " + name + ": only single " +
                        "nonterminals may appear within Lists, ListOptionals," +
                        " Optionals, and Choices.");
                  else {
                     String curType = (String)types.elementAt(i);
                     Integer cntForType = (Integer)typeCounts.get(curType);
                     
                     if ( types.elementAt(i).equals(Globals.listName) ||
                          types.elementAt(i).equals(Globals.listOptName) )
                        out.print("List");
                     else if ( types.elementAt(i).equals(Globals.optionalName) )
                        out.print("Optional");
                     else if ( types.elementAt(i).equals(Globals.tokenName) )
                        out.print("Token");
                     else
                        out.print(curType);

                     //
                     // If the field type appears more than once, put numbers
                     // after them to differentiate them.
                     //
                     if ( cntForType == null ) {
                        for ( int j = i + 1; j < numFields; ++j )
                           if ( types.elementAt(j).equals(curType) ) {
                              out.print("1");
                              break;
                           }

                        typeCounts.put(curType, new Integer(1));
                     }
                     else {
                        int newVal = cntForType.intValue() + 1;
                        out.print(newVal);
                        typeCounts.put(curType, new Integer(newVal));
                     }
                  }

                  ++numPerLine;

                  if ( i < numFields - 1 ) {
                     if ( numPerLine >= MAX_PER_LINE ) {
                        out.println();
                        out.print("   ");
                        numPerLine = 0;
                     }
                     else out.print(" ");
                  }
               }

               out.println("))");
            }
         }

         out.flush();
         out.close();
      }
      catch (IOException e) {
         Errors.hardErr("Could not generate " + outFilename);
      }
   }
}
