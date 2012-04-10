package EDU.iitm.jtb.threaded;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import java.lang.reflect.Field;

import EDU.purdue.jtb.misc.ClassInfo;
import EDU.purdue.jtb.misc.Errors;
import EDU.purdue.jtb.misc.FileExistsException;
import EDU.purdue.jtb.misc.Globals;
import EDU.purdue.jtb.misc.Spacing;

/**
 * 
 * This class will generate a default implementation of threaded visitor (no argument, no return).
 * The tree is visited in a depth-first way
 * In the visitor, there will be a limit to the number of threads created, specified at construction time. 
 * At every node, the visitor will check whether it can launch threads to do the job.
 * If not, it will go on sequentially. This is done by calling the getThread() method, which checks
 * whether the variable freeThreads is greater than 0, and decrements it if so.
 * After a thread is finished, it calls freeThread(), which in turn increments freeThreads again.
 *
 */

public class ThreadedVisitorBuilder {
	public static final int INDENT_AMT = 3;
	
	private String ThreadedVisitorName = "ThreadedVisitor";
	
	private int defaultMaxThreads = 4;

	   private final Vector[] classLists;
	   
	   // these threads will each work on one part of the class list
	   private Thread[] threads; // we need to join at the end, so we need to know them
	   private File visitorDir;

	   /**
	    * Vectors must contain objects of type ClassInfo
	    */
	   public ThreadedVisitorBuilder(Vector[] classLists) {
	     this.classLists = classLists;
	     threads = new Thread[classLists.length];
	      
	      visitorDir = new File(Globals.visitorDir);

	      if ( !visitorDir.exists() )
	         visitorDir.mkdir();
	      else if ( !visitorDir.isDirectory() )
	         Errors.softErr("\"" + Globals.visitorDir + "\" exists but is not a " +
	                        "directory.");
	   }
	   
	   // we assume this function is called from a thread, so we won't fork to execute its "main"
	   // 
	   // to print to the output file, we use the method printLine, defined below, which
	   // provides synchronization on the output stream. To ensure a block of text is printed as a whole,
	   // we should give it at once to printLine. For example when we write a function to the output,
	   // it should not be interleaved with another one, so the function would be such a block.
	   public void generateVisitorFile() throws FileExistsException {
		   //try {
		         File file = new File(visitorDir, ThreadedVisitorName + ".java");

		         if ( Globals.noOverwrite && file.exists() )
		            throw new FileExistsException(ThreadedVisitorName + ".java");

		         final PrintWriter out;
		         try {
		        	 out = new PrintWriter(new FileOutputStream(file), false);
		         }
		         catch (FileNotFoundException fe) {
		        	 return;
		         }
		         final Spacing spc = new Spacing(INDENT_AMT);

		         StringBuffer strBuf = new StringBuffer(); 
		         
		         // The initial text has to appear at the top, we like constructors first also, right?
		         // Therefore, we consider this section as setup and execute it sequentially
		         strBuf.append(Globals.fileHeader(spc) + '\n');
		         strBuf.append(spc.spc + "package " + Globals.visitorPackage + ";");
		         if ( !Globals.visitorPackage.equals(Globals.nodePackage) )
		        	 strBuf.append(spc.spc + "import " + Globals.nodePackage + ".*;");
		         strBuf.append(spc.spc + "import java.util.*;\n");
		         strBuf.append(spc.spc + "/**");
		         strBuf.append(spc.spc + " * Base class for a threaded visitor");
		         strBuf.append(spc.spc + " */\n");
		         strBuf.append(spc.spc + "public class " + ThreadedVisitorName + " {\n");
		         

		         // freeThreads is of class Integer so we can use it with synchronized
		         strBuf.append(spc.spc + "private Integer freeThreads;\n");
		         
		         // default constructor
		         strBuf.append(spc.spc +    "public " + ThreadedVisitorName + "() {\n" +
				        		 		spc.spc + "  this(" + defaultMaxThreads + ");\n" +
				        		 		spc.spc + "}\n");
		         // constructor to specify the maximum number of threads
		         strBuf.append(spc.spc +    "public " + ThreadedVisitorName + "(int maxNoThreads) {\n" +
		        		 				spc.spc + "  freeThreads = maxNoThreads;\n" +
		        		 				spc.spc + "}\n\n");
		         
		         // the synchronized methods to use the thread counts
		         strBuf.append(spc.spc +    "public void freeThread() {\n" +
		        		 				spc.spc + "  synchronized (freeThreads) {\n" +
		        		 				spc.spc + "    ++freeThreads;\n" +
		        		 				spc.spc + "  }\n" +
		        		 				spc.spc + "}\n");
		         strBuf.append(spc.spc +    "public boolean getThread() {\n" +
		        		 				spc.spc + "  synchronized (freeThreads) {\n" +
		        		 				spc.spc + "    if (freeThreads == 0)\n" +
		        		 				spc.spc + "      return false;\n" +
		        		 				spc.spc + "    --freeThreads;\n" +
		        		 				spc.spc + "    return true;\n" +
		        		 				spc.spc + "  }\n" +
		        		 				spc.spc + "}\n\n");
		         
		         printLineSync(out, strBuf.toString());
		         
		         printAutoVisitorMethods(out);
		         
		         spc.updateSpc(+1);
		         printLineSync(out, spc.spc + "//\n" +
		        		 spc.spc + "// User-generated visitor methods below\n" +
		        		 spc.spc + "//\n");

		         
		         // for each chunk in the class list
		         for (int i=0; i<classLists.length; i++) {
		        	 final Vector thrClassList = classLists[i];
		        	 threads[i] = new Thread() {
		        		 public void run() {
		        			 // 
		        			 StringBuffer threadStrBuf;
		        			 for ( Enumeration e = thrClassList.elements(); e.hasMoreElements(); ) {
		        				 threadStrBuf = new StringBuffer();
		        				 ClassInfo cur = (ClassInfo)e.nextElement();
		     		            String name = cur.getName();

		     		            threadStrBuf.append(spc.spc + "/**");
		     		            threadStrBuf.append(spc.spc + "/**");
		     		            if ( Globals.javaDocComments ) threadStrBuf.append(spc.spc + " * <PRE>");
		     		            threadStrBuf.append(cur.getEbnfProduction(spc));
		     		            if ( Globals.javaDocComments ) threadStrBuf.append(spc.spc + " * </PRE>");
		     		            threadStrBuf.append(spc.spc + " */");
		     		            
		     		            // try to generate a thread for each field in cur
		     		            Vector fields = cur.getNameList();
		     		            threadStrBuf.append(spc.spc +    "public void visit(" + name + " n) {");
		     		            for (Enumeration e2 = fields.elements(); e2.hasMoreElements();) {
		     		            	String fieldName = (String)e2.nextElement();
		     		            	
		     		            	threadStrBuf.append(
		     		            			spc.spc + "  if (getThread()) {\n" +
		     		            			spc.spc + "    new Thread() {\n" +
		     		            			spc.spc + "      n." + fieldName +".accept(this);\n" +
		     		            			spc.spc + "      freeThread();\n" +
		     		            			spc.spc + "    }.start();\n" +
		     		            			spc.spc + "  }\n" +
		     		            			spc.spc + "  else\n" +
		     		            			spc.spc + "    n." + fieldName + ".accept(this);\n");
		     		            }
		     		            threadStrBuf.append(spc.spc + "}\n");
		     		            printLineSync(out, threadStrBuf.toString());
		        			 }
		        		 }
		        	 };
		        	 threads[i].start();
		         }

		         // we want all the threads to finish
	        	 for (int i=0; i<threads.length; i++)
			         try {
			        		 threads[i].join();
			         } catch (InterruptedException ie) {
			        	 // we don't care.. the thread stopped anyway
			         }

		         spc.updateSpc(-1);
		         printLineSync(out, spc.spc + "}\n");
		         out.flush();
		         out.close();
		      /*}
		      catch (IOException e) {
		         Errors.hardErr(e);
		      }*/
	   }
	   
	   private void printLineSync(PrintWriter out, String txt) {
		   synchronized (out) {
			   out.println(txt);
		   }
	   }
	   
	   private void printAutoVisitorMethods(PrintWriter out) {
		   printLineSync(out,
				   "   //" +
				   "   // Threaded Auto class visitors" +
				   "   //\n");

		      
//	      out.print(getNodeListVisitorStr());
//	      out.print(getNodeListOptionalVisitorStr());
//	      out.print(getNodeOptionalVisitorStr());
//	      out.print(getNodeSequenceVisitorStr());
//	      out.print(getNodeTokenVisitorStr());
		  printLineSync(out, getNodeListVisitorStr());
		  printLineSync(out, getNodeListOptionalVisitorStr());
		  printLineSync(out, getNodeOptionalVisitorStr());
		  printLineSync(out, getNodeSequenceVisitorStr());
		  printLineSync(out, getNodeTokenVisitorStr());
	   }
	   
	   private String getEnumerationStr() {
		   return  "  for (Enumeration e = n.elements(); e.hasMoreElements();) {\n" +
				   "    if (getThread()) {\n" +
				   "      new Thread() {\n" +
				   "        public void run() {\n" +
				   "          e.nextElement().accept(ThreadedVisitor.this);\n" +
				   "          freeThread();\n" +
				   "        }\n" +
				   "      }.start();\n" +
				   "    }\n" +
				   "    else\n" + // or do it sequentially
				   "      e.nextElement().accept(this);\n" +
				   "  }\n";
	   }
	   
	   private String getNodeListVisitorStr() {
		   return "public void visit(NodeList n) {\n" +
				   getEnumerationStr() +
				   "}\n";
	   }
	   private String getNodeListOptionalVisitorStr() {
		   return "public void visit(NodeListOptional n) {\n" +
				   "  if (!n.present()) return;\n" +
				   getEnumerationStr() +
				   "}\n";
	   }
	   
	   private String getNodeSequenceVisitorStr(){
		   return "public void visit(NodeSequence n) {\n" +
				   getEnumerationStr() + 
				   "}\n";
	   }
	   
	   // even though there is only one node to visit, we will try to launch a thread:
	   // one may have been freed in between
	   private String getNodeOptionalVisitorStr() {
		   return "public void visit(NodeOptional n) {\n" +
				   	"  if (!n.present()) return;\n"+
				   	"  if (getThread()) {\n"+
				   	"    new Thread() {\n" +
				   	"      public void run() {\n" +
				   	"        n.node.accept(ThreadedVisitor.this)\n" +
				   	"        freeThread();\n" +
				   	"      }\n" +
				   	"    }.start();\n" +
				   	"  }\n"+
				   	"  else\n"+
				   	"    n.node.accept(this);" +
				   	"}\n";
	   }
	   
	   private String getNodeTokenVisitorStr() {
		   // do nothing
		   return "public void visit(NodeToken n) {}\n";
	   }
}
