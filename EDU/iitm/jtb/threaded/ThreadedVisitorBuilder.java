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
	         strBuf.append(spc.spc + "package " + Globals.visitorPackage + ";\n");
	         if ( !Globals.visitorPackage.equals(Globals.nodePackage) )
	        	 strBuf.append(spc.spc + "import " + Globals.nodePackage + ".*;\n");
	         strBuf.append(spc.spc + "import java.util.*;\n");
        	 strBuf.append(spc.spc + "import java.util.concurrent.*;\n");
	         strBuf.append(spc.spc + "/**\n");
	         strBuf.append(spc.spc + " * Base class for a threaded visitor\n");
	         strBuf.append(spc.spc + " */\n");
	         strBuf.append(spc.spc + "public class " + ThreadedVisitorName + " {\n");
	         

	         // freeThreads is of class Integer so we can use it with synchronized
	         strBuf.append(spc.spc + "private ExecutorService threadPool;\n");
	         strBuf.append("Integer tasks=0; // number of tasks currently running\n");
	         strBuf.append("Integer i=0; // count of visited tokens\n");
	         
	         // default constructor
	         strBuf.append(spc.spc +    "public " + ThreadedVisitorName + "() {\n" +
			        		 		spc.spc + "  this(Runtime.getRuntime().availableProcessors());\n" +
			        		 		spc.spc + "}\n");
	         // constructor to specify the maximum number of threads
	         strBuf.append(spc.spc +    "public " + ThreadedVisitorName + "(int maxNoThreads) {\n" +
	        		 				spc.spc + "  threadPool = Executors.newFixedThreadPool(maxNoThreads);\n" +
	        		 				spc.spc + "}\n\n");
	         
	         // since the pool does not provide a count of threads, we do it ourselves
	         strBuf.append( spc.spc + "public synchronized void addTask(Runnable r) {\n" +
	        		 		spc.spc + "  ++tasks;\n" +
	        		 		spc.spc + "  threadPool.submit(r);\n" + 
	        		 		spc.spc + "}\n");
	         
	         strBuf.append( spc.spc + "public synchronized void taskEnd() {\n" +
						spc.spc + "  --tasks;\n" + 
	     		 		spc.spc + "}\n");
	         
	         strBuf.append( spc.spc + "public synchronized boolean isTerminated() {\n" +
						spc.spc + "  return tasks == 0;\n" + 
	     		 		spc.spc + "}\n");
	         
	         strBuf.append( spc.spc + "public void shutdown() {\n" +
     		 		spc.spc + "  threadPool.shutdown();\n" + 
     		 		spc.spc + "}\n");
	        
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
	        			 StringBuffer threadStrBuf;
	        			 for ( Enumeration e = thrClassList.elements(); e.hasMoreElements(); ) {
	        				 threadStrBuf = new StringBuffer();
	        				 ClassInfo cur = (ClassInfo)e.nextElement();
	     		            String name = cur.getName();


	     		            threadStrBuf.append(spc.spc + "/**\n");
	     		            if ( Globals.javaDocComments ) threadStrBuf.append(spc.spc + " * <PRE>\n");
	     		            threadStrBuf.append(cur.getEbnfProduction(spc) + "\n");
	     		            if ( Globals.javaDocComments ) threadStrBuf.append(spc.spc + " * </PRE>\n");
	     		            threadStrBuf.append(spc.spc + " */\n");
	     		            
	     		            // try to generate a thread for each field in cur
	     		            Vector fields = cur.getNameList();
	     		            threadStrBuf.append(spc.spc +    "public void visit(" + name + " n) {\n");
     		            	threadStrBuf.append(
     		   "synchronized(i){System.out.println(\"visit \"+i+\": "+name+"\");++i;}");
	     		            for (Enumeration e2 = fields.elements(); e2.hasMoreElements();) {
	     		            	String fieldName = (String)e2.nextElement();
	     		            	threadStrBuf.append(
	     		            			spc.spc + "  n." + fieldName + ".accept(this, true);\n");
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
	   }
	   
	   private void printLineSync(PrintWriter out, String txt) {
		   synchronized (out) {
			   out.println(txt);
		   }
	   }
	   
	   private void printAutoVisitorMethods(PrintWriter out) {
		   printLineSync(out,
				   "   //\n" +
				   "   // Threaded Auto class visitors\n" +
				   "   //\n");

		  printLineSync(out, getNodeListVisitorStr());
		  printLineSync(out, getNodeListOptionalVisitorStr());
		  printLineSync(out, getNodeOptionalVisitorStr());
		  printLineSync(out, getNodeSequenceVisitorStr());
		  printLineSync(out, getNodeTokenVisitorStr());
		  printLineSync(out, getNodeChoiceVisitorStr());
	   }
	   
	   private String getEnumerationStr() {
		   return  "  for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {\n" +
				   "    e.nextElement().accept(this, true);\n" +
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
	   
	   private String getNodeChoiceVisitorStr(){
		   return "public void visit(NodeChoice n) {\n" +
				   "  n.choice.accept(this, true);\n" +
				   "}\n";
	   }
	   
	   private String getNodeOptionalVisitorStr() {
		   return "public void visit(NodeOptional n) {\n" +
				   	"  if (!n.present()) return;\n"+
				    "  n.node.accept(this, true);" +
				   	"}\n";
	   }
	   
	   private String getNodeTokenVisitorStr() {
		   // do nothing
		   return "public void visit(NodeToken n) {}\n";
	   }
}
