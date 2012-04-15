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

package EDU.purdue.jtb;

import EDU.purdue.jtb.parser.*;
import EDU.purdue.jtb.syntaxtree.Node;
import EDU.purdue.jtb.visitor.*;
import EDU.purdue.jtb.misc.*;
import EDU.purdue.jtb.misc.toolkit.*;
import EDU.iitm.jtb.threaded.ThreadedVisitorBuilder;
import EDU.iitm.jtb.threaded.VectorChunker;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

/**
 * Java Tree Builder (JTB) Driver
 *
 * Class JTB contains the main() method of the program as well as related
 * methods.
 *
 * @author  Kevin Tao
 * @author  Wanjun Wang, wanjun@purdue.edu
 * 
 * Modified by:
 * TODO
 */
public class JTB {
   private static InputStream in;

   private static String progName = Globals.progName;
   private static String version = Globals.version;
   private static String scriptName = Globals.scriptName;

   public static void main(String args[]) {
      JTBParser parser;
      final Node root;
      
      
      int coreNo = Runtime.getRuntime().availableProcessors();
      ExecutorService threadPool = Executors.newFixedThreadPool(coreNo);

      try { if ( !processCommandLine(args) ) return; }
      catch (InvalidCommandLineException e) {
         System.err.println(progName + ":  " + e.getMessage());
         return;
      }
      catch (Exception e) { Errors.hardErr(e.getMessage()); }

      System.err.println(progName + " version " + version);
      System.err.println(progName + ":  Reading from " + Globals.inFilename +
                         "...");
      parser = new JTBParser(in);

      try {
          // the setup needs to be done sequentially
         root = parser.JavaCCInput();
         System.err.println(progName + ":  Input file parsed successfully.");
         
         
         //
         // A few notes for the parallelization
         // - If any thread notices errors, it will report them, and exit the program
         // - To print to System.err, we will use our log() method, which provides synchronization
         // - list, the variable holding the classes created from the grammar file, will be
         //     chunked, and hold in an array, see below. But some visitors should be refactored if
         //     we want to use that, since we should instantiate several of them and each would
         //     print the "automatic" classes, behavior that we don't want
         
         
         //
         // Perform actions based on command-line flags
         //
         ClassGenerator vcg = new ClassGenerator();

         Errors.resetCounts();

         if ( !Globals.noSemanticCheck ) {
            root.accept(new SemanticChecker());

            if ( Errors.errorCount() > 0 ) {
               Errors.printSummary();
               return;
            }
         }
         
         root.accept(vcg);              // create the class list
         final Vector list = vcg.getClassList();
         int chunkNumber = 4;
         final Vector[] chunkList = VectorChunker.chunk(list, chunkNumber);
         
         // if any of the two conditions is true, we will use gen
         final FileGenerator gen;
         final FileGenerator[] gens;
         if (Globals.printClassList || Globals.generateFiles) {
        	 gen = new FileGenerator(list);
        	 gens = new FileGenerator[chunkNumber];
        	 for (int i=0; i< chunkNumber; i++)
        		 gens[i] = new FileGenerator(chunkList[i]);
         }
         else {
        	 gen = null;
        	 gens = null;
         }

         if ( Errors.errorCount() > 0 ) {
            Errors.printSummary();
            return;
         }

         // we want these two reports to appear before anything else, so we will
         // let it do sequentially
         if ( Globals.printGrammarToOut ) root.accept(new Printer(System.out));
         if ( Globals.printClassList ) {
            System.out.println("\nThe classes generated and the fields each " +
                               "contains are as follows:\n");
            for (int i=0; i<chunkNumber; i++)
            	gens[i].printClassList(new PrintWriter(System.out, true));
         }
         // is the end of the sequential part here ????

         if ( Globals.generateFiles ) {
        	 
        	 // create the new grammar file
        	threadPool.submit(
        	new Runnable() {
            	public void run() {
            		try {
                        root.accept(new Annotator());

                        if ( Errors.errorCount() > 0 ) {
                        	synchronized (System.err){
                        		Errors.printSummary(); // uses System.err
                                System.exit(1);
                        	}
                        }

                        log( progName + ":  \"" + Globals.outFilename +
                        		"\" generated to current directory.");
                     }
                     catch (FileExistsException e) {
                        log(progName + ":  \"" + Globals.outFilename +
                                           "\" already exists.  Won't overwrite.");
                     }
            		
            	}
            });             

            // generate the auto class files
        	threadPool.submit(
        	new Runnable() {
            	public void run() {
            		// use gens[0], any would be fine
            		try { gens[0].generateAutoClassFiles(); }
                    catch (FileExistsException e) {
                    	log(progName + ":  One or more of the automatic "+
                    		"node class files already exists.  Won't overwrite.");
                    }
            	}
            });
            
            // generate the user class files
            for (int i=0; i<chunkNumber; i++) {
            	final int j = i;
            	threadPool.submit(
            	new Runnable() {
            		public void run() {
	            		try {
	                        gens[j].generateClassFiles();
	                        log(progName + ":  Syntax tree Java source " +
	                        		"files generated to directory \"" + Globals.nodeDir + "\".");
	                     }
	                     catch (FileExistsException e) {
	                    	 log(progName + ":  One or more of the generated "+
	                    				 "node class files already exists.  Won't overwrite.");
	                     }
	            	}
	            });
            }
            
			
            threadPool.submit(
            new Runnable() {
            	public void run() {
		            try {
		               gen.generateVisitorFile();
		               log (progName + ":  \"" + Globals.GJVisitorName +
		                  ".java\" generated " + "to directory \"" +
		                  Globals.visitorDir + "\".");
		            }
		            catch (FileExistsException e) {
		            	log(progName + ":  \"" + Globals.GJVisitorName +
		                  "\" already exists.  Won't overwrite.");
		            }
            	}
            });
            
            // TODO global threaded visitor name, replace below
            threadPool.submit( new Runnable() {
            	public void run() {
		            try {
		                new ThreadedVisitorBuilder(chunkList).generateVisitorFile();
		                log(progName + ":  \"" + Globals.visitorName +
		                   ".java\" (threaded visitor) generated " + "to directory \"" +
		                   Globals.visitorDir + "\".");
		             }
		             catch (FileExistsException e) {
		            	 log(progName + ":  \"" + Globals.visitorName +
		                   "\" (threaded visitor) already exists.  Won't overwrite.");
		             }
            	}
            });
            
            threadPool.submit( new Runnable() {
            	public void run() {
					try {
		               new OldFileGenerator(list).generateVisitorFile();
		               log(progName + ":  \"" + Globals.visitorName +
		                  ".java\" generated " + "to directory \"" +
		                  Globals.visitorDir + "\".");
		            }
		            catch (FileExistsException e) {
		            	log(progName + ":  \"" + Globals.visitorName +
		                  "\" already exists.  Won't overwrite.");
		            }
            	}
            });
            
			threadPool.submit( new Runnable() {
            	public void run() {
					try {
		               new NoArguFileGenerator(list).generateVisitorFile();
		               log(progName + ":  \"" + Globals.GJNoArguVisitorName +
		                  ".java\" generated " + "to directory \"" +
		                  Globals.visitorDir + "\".");
		            }
		            catch (FileExistsException e) {
		            	log(progName + ":  \"" + Globals.GJNoArguVisitorName +
		                  "\" already exists.  Won't overwrite.");
		            }
            	}
			});
			
			threadPool.submit( new Runnable() {
            	public void run() {
					try {
		               new VoidFileGenerator(list).generateVisitorFile();
		               log(progName + ":  \"" + Globals.GJVoidVisitorName +
		                  ".java\" generated " + "to directory \"" +
		                  Globals.visitorDir + "\".");
		            }
		            catch (FileExistsException e) {
		            	log(progName + ":  \"" + Globals.GJVoidVisitorName +
		                  "\" already exists.  Won't overwrite.");
		            }
            	}
			});
			
			threadPool.submit( new Runnable() {
            	public void run() {
		            try {
		               new GJDepthFirstVisitorBuilder(list).generateDepthFirstVisitor();
		               log(progName + ":  \"" +
		                  GJDepthFirstVisitorBuilder.outFilename + "\" " +
		                  "generated to directory \"" + Globals.visitorDir + "\".");
		            }
		            catch (FileExistsException e) {
		            	log(progName + ":  \"" +
		                  GJDepthFirstVisitorBuilder.outFilename + "\" already exists.  "+
		                  "Won't overwrite.");
		            }
            	}
			});
			

            threadPool.submit( new Runnable() {
            	public void run() {
					try {
					   new OldDepthFirstVisitorBuilder(list).generateDepthFirstVisitor();
					   log(progName + ":  \"" +
		                  OldDepthFirstVisitorBuilder.outFilename + "\" " +
		                  "generated to directory \"" + Globals.visitorDir + "\".");
		            }
		            catch (FileExistsException e) {
		            	log(progName + ":  \"" +
		                  OldDepthFirstVisitorBuilder.outFilename + "\" already exists.  "+
		                  "Won't overwrite.");
		            }
            	}
            });
            
			threadPool.submit( new Runnable() {
            	public void run() {
		            try {
		               new GJNoArguDepthFirstBuilder(list).generateDepthFirstVisitor();
		               log(progName + ":  \"" +
		                  GJNoArguDepthFirstBuilder.outFilename + "\" " +
		                  "generated to directory \"" + Globals.visitorDir + "\".");
		            }
		            catch (FileExistsException e) {
		            	log(progName + ":  \"" +
		                  GJNoArguDepthFirstBuilder.outFilename + "\" already exists.  "+
		                  "Won't overwrite.");
		            }
            	}
			});

            threadPool.submit( new Runnable() {
            	public void run() {
            		try {
                        new GJVoidDepthFirstBuilder(list).generateDepthFirstVisitor();
                        log(progName + ":  \"" +
                           GJVoidDepthFirstBuilder.outFilename + "\" " +
                           "generated to directory \"" + Globals.visitorDir + "\".");
                     }
                     catch (FileExistsException e) {
                     	log(progName + ":  \"" +
                           GJDepthFirstVisitorBuilder.outFilename + "\" already exists.  "+
                           "Won't overwrite.");
                     }
            	}
            });
            
            
            // we have to wait for all the tasks to be finished here, since there is an error
            // checking
            threadPool.shutdown();
            while (!threadPool.awaitTermination(1, TimeUnit.SECONDS)); // loop forever until it is finished


            if ( Globals.schemeToolkit ) {
               root.accept(new SchemeSemanticChecker());

               if ( Errors.errorCount() > 0 ) {
                  Errors.printSummary();
                  return;
               }

               threadPool.submit(
            	  new Runnable() {
            		  public void run() {
            			  try {
			                  new SchemeVisitorBuilder(list).generateSchemeBuilder();
			                  System.err.println(progName + ":  \"" +
			                     SchemeVisitorBuilder.outFilename + "\" generated to " +
			                     "directory \"" + Globals.visitorDir + "\".");
			               }
			               catch (FileExistsException e) {
			                  System.err.println(progName + ":  \"" +
			                     SchemeVisitorBuilder.outFilename + "\" already exists.  " +
			                     "Won't overwrite.");
			               }
            		  }
            	  });
               
               threadPool.submit(
             	  new Runnable() {
             		  public void run() {
             			  try {
		                  new SchemeRecordBuilder(list).generateSchemeRecords();
		                  System.err.println(progName + ":  \"" +
		                     SchemeRecordBuilder.outFilename + "\" generated to " +
		                     "current directory.");
		               }
		               catch (FileExistsException e) {
		                  System.err.println(progName + ":  \"" +
		                     SchemeRecordBuilder.outFilename + "\" already exists.  " +
		                     "Won't overwrite.");
		               }
					   System.err.println();
		            }
             	  });
            } // end if Globals.schemeToolkit

            if ( Globals.printerToolkit ) {
            	threadPool.submit(
            		new Runnable() {
            			public void run() {
            				 try {
            	                  new TreeDumperBuilder().generateTreeDumper();
            	                  System.err.println(progName + ":  \"" +
            	                     TreeDumperBuilder.outFilename + "\" generated to " +
            	                     "directory \"" + Globals.visitorDir + "\".");
            	               }
            	               catch (FileExistsException e) {
            	                  System.err.println(progName + ":  \"" +
            	                     TreeDumperBuilder.outFilename + "\" already exists.  " +
            	                     "Won't overwrite.");
            	               }
            			}
            		});
              

            	threadPool.submit(
                		new Runnable() {
                			public void run() {
                				 try {
					                  new TreeFormatterBuilder(list).generateTreeFormatter();
					                  System.err.println(progName + ":  \"" +
					                     TreeFormatterBuilder.outFilename + "\" generated to " +
					                     "directory \"" + Globals.visitorDir + "\".");
					               }
					               catch (FileExistsException e) {
					                  System.err.println(progName + ":  \"" +
					                     TreeFormatterBuilder.outFilename + "\" already exists.  " +
					                     "Won't overwrite.");
					               }
                			}
                		});
            } // end if toolKit
	/* 
            try { 
               new CopyCat().copy();
               System.err.println(progName + ":  gj packages duplicated to directory \"gj\".");
            }
            catch (FileNotFoundException e) {
               System.err.println("Duplicating GJ libraries is not completed.");
            }
            catch (SecurityException e) {
               System.err.println("Duplicating GJ libraries is not completed " +
                                  " due to security reasons.");
            }
	*/

            threadPool.shutdown();
            while (!threadPool.awaitTermination(1, TimeUnit.SECONDS));
            if ( Errors.errorCount() > 0 || Errors.warningCount() > 0 )
               Errors.printSummary();
         }
      }
      catch (ParseException e) {
         System.err.println("\n" + e.getMessage() + "\n");
         System.err.println(progName + ":  Encountered errors during " +
                            "parse.");
      }
      catch (Exception e) { Errors.hardErr(e); }
   }
   
   // since the method is static, only one thread at a time can write here
   private static void log(String s) {
	   System.err.println(s);
   }

   /**
    * Returns true if the program is to continue normally, false if the
    * program is to terminate following this method.
    */
   private static boolean processCommandLine(String[] args)
   throws InvalidCommandLineException {
      boolean returnVal = false;

      for ( int i = 0; i < args.length; ++i ) {
         if ( args[i].charAt(0) != '-' ) {
            if ( returnVal ) {
               returnVal = false;   // 2 filenames passed as arguments?
               break;
            }
            else {
               try { in = new java.io.FileInputStream(args[i]); }
               catch (java.io.FileNotFoundException e) {
                  System.err.println(progName + ":  File \"" + args[i] +
                                     "\" not found.");
                  return false;
               }

               Globals.inFilename = args[i];
               returnVal = true;
            }
         }
         else {
            if ( args[i].length() <= 1 )
               throw new InvalidCommandLineException("Unknown option \"" +
                  args[i] + "\".  Try \"" + scriptName + " -h\" for more " +
                  "information.");
            if ( args[i].equals("-h") ) {
               returnVal = false;
               break;
            }
            else if ( args[i].equals("-o") ) {
               ++i;
               if ( i >= args.length || args[i].charAt(0) == '-' )
                  throw new InvalidCommandLineException("Option \"-o\" " +
                     "must be followed by a filename.");
               else
                  Globals.outFilename = args[i];
            }
            else if ( args[i].equals("-np") ) {
               ++i;
               if ( i >= args.length || args[i].charAt(0) == '-' )
                  throw new InvalidCommandLineException("Option \"-np\" " +
                     "must be followed by a package name.");
               else
                  Globals.setNodePackage(args[i]);
            }
            else if ( args[i].equals("-vp") ) {
               ++i;
               if ( i >= args.length || args[i].charAt(0) == '-' )
                  throw new InvalidCommandLineException("Option \"-vp\" " +
                     "must be followed by a package name.");
               else
                  Globals.setVisitorPackage(args[i]);
            }
            else if ( args[i].equals("-p") ) {
               ++i;
               if ( i >= args.length || args[i].charAt(0) == '-' )
                  throw new InvalidCommandLineException("Option \"-p\" " +
                     "must be followed by a package name.");
               else {
                  Globals.setNodePackage(args[i] + ".syntaxtree");
                  Globals.setVisitorPackage(args[i] + ".visitor");
               }
            }
            else if ( args[i].equals("-si") ) {
               in = System.in;
               Globals.inFilename = "standard input";
               returnVal = true;
            }
            else if ( args[i].equals("-w") )
               Globals.noOverwrite = true;
            else if ( args[i].equals("-e") )
               Globals.noSemanticCheck = true;
            else if ( args[i].equals("-jd") )
               Globals.javaDocComments = true;
            else if ( args[i].equals("-f") )
               Globals.descriptiveFieldNames = true;
            else if ( args[i].equals("-ns") ) {
               ++i;
               if ( i >= args.length || args[i].charAt(0) == '-' )
                  throw new InvalidCommandLineException("Option \"-ns\" " +
                     "must be followed by a class name.");
               else
                  Globals.nodeSuperclass = args[i];
            }
            else if ( args[i].equals("-pp") )
               Globals.parentPointers = true;
            else if ( args[i].equals("-tk") )
               Globals.keepSpecialTokens = true;

            // Toolkit options
            else if ( args[i].equals("-scheme") )
               Globals.schemeToolkit = true;
            else if ( args[i].equals("-printer") )
               Globals.printerToolkit = true;

            // Debug options
            else if ( args[i].equals("-cl") )
               Globals.printClassList = true;
            else if ( args[i].equals("-no") )
               Globals.generateFiles = false;
            else if ( args[i].equals("-g") )
               Globals.printGrammarToOut = true;
            else
               throw new InvalidCommandLineException("Unknown option \"" +
                  args[i] + "\".  Try \"" + scriptName + " -h\" for more " +
                  "information.");
         }
      }

      if ( returnVal )
         return true;
      else {
         printHelp();
         return false;
      }
   }

   private static void printHelp() {
      System.out.print(
progName + " version " + version + "\n" +
"\n" +
"Usage: " + scriptName + " [OPTIONS] " + "[inputfile]\n" +
"\n" +
"Standard options:\n" +
"  -h          Displays this help message.\n" +
"  -o NAME     Uses NAME as the filename for the annotated output grammar.\n" +
"  -np NAME    Uses NAME as the package for the syntax tree nodes.\n" +
"  -vp NAME    Uses NAME as the package for the default Visitor class.\n" +
"  -p NAME     \"-p pkg\" is short for \"-np pkg.syntaxtree -vp pkg.visitor\"\n" +
"  -si         Read from standard input rather than a file.\n" +
"  -w          Do not overwrite existing files.\n" +
"  -e          Suppress JTB semantic error checking.\n" +
"  -jd         Generate JavaDoc-friendly comments in the nodes and visitor.\n" +
"  -f          Use descriptive node class field names.\n" +
"  -ns NAME    Uses NAME as the class which all node classes will extend.\n" +
"  -pp         Generate parent pointers in all node classes.\n" +
"  -tk         Generate special tokens into the tree.\n" +
"\n" +
"Toolkit options:\n" +
"  -scheme     Generate: (1) Scheme records representing the grammar.\n" +
"                        (2) A Scheme tree building visitor.\n"+
"  -printer    Generate a syntax tree dumping visitor.\n" +
/*
"\n" +
"Debugging options:\n" +
"  -cl         Print a list of the classes generated to standard out.\n" +
"  -no         Parse the input but do not generate any output files.\n" +
"  -g          Print the grammar without annotations to standard out.\n" +
(No longer supported) */
"\n");
   }
}

class InvalidCommandLineException extends Exception {
   InvalidCommandLineException()          { super(); }
   InvalidCommandLineException(String s)  { super(s); }
}
