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

package EDU.purdue.jtb.misc;

/**
 * Class Errors handles errors ranging from warnings to fatal errors, printing
 * a message to the user and handling it appropriately.
 */
public class Errors {
   private static int numErrors = 0;
   private static int numWarnings = 0;

   public static int errorCount()      { return numErrors; }
   public static int warningCount()    { return numWarnings; }
   public static void setErrorCount(int i)   { numErrors = i; }
   public static void setWarningCount(int i) { numWarnings = i; }
   public static void resetCounts()    { numErrors = numWarnings = 0; }

   public static void printSummary() {
      System.err.println(numWarnings + " warnings, " + numErrors + " errors.");
   }

   public static void warning(String s) {
      warning(s, -1);
   }

   public static void warning(String s, int lineNum) {
      if ( lineNum == -1 )
         System.err.println(Globals.inFilename + ":  warning:  " + s);
      else
         System.err.println(Globals.inFilename + " (" +
            Integer.toString(lineNum) + "):  warning:  " + s);

      ++numWarnings;
   }

   public static void softErr(String s) {
      softErr(s, -1);
   }

   public static void softErr(String s, int lineNum) {
      if ( lineNum == -1 )
         System.err.println(Globals.inFilename + ":  soft error:  " + s);
      else
         System.err.println(Globals.inFilename + " (" +
            Integer.toString(lineNum) + "):  " + s);

      ++numErrors;
   }

   public static void notice(String s) {
      System.err.println(Globals.inFilename + ":  " + s);
   }

   //
   // For reporting unexpected fatal program logic errors.  This method
   // terminates the program and prints a stack trace.
   //
   public static void hardErr(String s) {
      System.err.println(Globals.inFilename + ":  unexpected program error:  " + s);
      System.err.println();
      System.err.println("Please report this to " + Globals.email);
      System.err.println();

      try { throw new Exception(); }
      catch (Exception e) {
         e.printStackTrace();
         System.err.println();
         System.exit(-1);
      }
   }

   public static void hardErr(Throwable t) {
      System.err.println(Globals.inFilename + ":  unexpected program error:  " + t.getMessage());
      System.err.println();
      System.err.println("Please report this to " + Globals.email);
      System.err.println();
      t.printStackTrace();
      System.exit(-1);
   }
}
