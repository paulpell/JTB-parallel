package EDU.iitm.jtb.threaded;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import EDU.purdue.jtb.misc.ClassInfo;
import EDU.purdue.jtb.misc.FileExistsException;
import EDU.purdue.jtb.misc.Globals;

/**
 * 
 * This class will generate a default implementation of threaded visitor (no argument, no return),
 * where the tree is visited in a depth-first way.
 *
 */

public class ThreadedVisitorBuilder extends AbstractThreadedVisitorBuilder {
	
	   /**
	    * Vectors must contain objects of type ClassInfo
	    */
	
	public ThreadedVisitorBuilder(int id, Vector<ClassInfo>[] classLists) {
		super(id, classLists);
	}
	   
	   
	   // we assume this function is called from a thread, so we won't fork to execute it
	   // 
	   // to print to the output file, we use the method printLine, defined below, which
	   // provides synchronization on the output stream. To ensure a block of text is printed as a whole,
	   // we should give it at once to printLine. For example when we write a function to the output,
	   // it should not be interleaved with another one, so the function would be such a block.
	   public void generateVisitorFile() throws FileExistsException {
		   File file = new File(visitorDir, IITGlobals.threadedDFVisitorName + ".java");

	         if ( Globals.noOverwrite && file.exists() )
	            throw new FileExistsException(IITGlobals.threadedDFVisitorName + ".java");

	         try {
	        	 out = new PrintWriter(new FileOutputStream(file), false);
	         }
	         catch (FileNotFoundException fe) {
	        	 return;
	         }

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
	         strBuf.append(spc.spc + "public class " + IITGlobals.threadedDFVisitorName + " {\n");

	         printLineSync(strBuf.toString());

	         printVisitorThreadPoolCode();
  
	         // default constructor
	         printLineSync(spc.spc +    "public " + IITGlobals.threadedDFVisitorName + "() {\n" +
			        		 		spc.spc + "  this(Runtime.getRuntime().availableProcessors());\n" +
			        		 		spc.spc + "}\n");
	         // constructor to specify the maximum number of threads
	         printLineSync(spc.spc +    "public " + IITGlobals.threadedDFVisitorName + "(int maxNoThreads) {\n" +
	        		 				spc.spc + "  threadPool = Executors.newFixedThreadPool(maxNoThreads);\n" +
	        		 				spc.spc + "}\n\n");
	         
	         
	         printAutoVisitorMethods();
	         
	         spc.updateSpc(+1);
	         printLineSync(spc.spc + "//\n" +
	        		 spc.spc + "// User-generated visitor methods below\n" +
	        		 spc.spc + "//\n");

	         
	         // for each chunk in the class list
	         for (int i=0; i<classLists.length; i++) {
	        	 final Vector<ClassInfo> thrClassList = classLists[i];
	        	 runnables[i] = new Runnable() {
	        		 public void run() {
	        			 StringBuffer threadStrBuf;
	        			 for ( Enumeration<ClassInfo> e = thrClassList.elements(); e.hasMoreElements(); ) {
	        				 threadStrBuf = new StringBuffer();
	        				 ClassInfo cur = (ClassInfo)e.nextElement();
	     		            String name = cur.getName();
	
	
	     		            threadStrBuf.append(spc.spc + "/**\n");
	     		            if ( Globals.javaDocComments ) threadStrBuf.append(spc.spc + " * <PRE>\n");
	     		            threadStrBuf.append(cur.getEbnfProduction(spc) + "\n");
	     		            if ( Globals.javaDocComments ) threadStrBuf.append(spc.spc + " * </PRE>\n");
	     		            threadStrBuf.append(spc.spc + " */\n");
	     		            
	     		            // try to generate a thread for each field in cur
	     		            Vector<String> fields = cur.getNameList();
	     		            threadStrBuf.append(spc.spc +    "public void visit(" + name + " n) {\n");
	     		            for (Enumeration<String> e2 = fields.elements(); e2.hasMoreElements();) {
	     		            	String fieldName = e2.nextElement();
	     		            	threadStrBuf.append(
	     		            			spc.spc + "  n." + fieldName + ".accept(this, true);\n");
	     		            }
	     		            threadStrBuf.append(spc.spc + "}\n");
	     		            printLineSync(threadStrBuf.toString());
	        			 }
	        			 taskEnd(); // signals this runnable is finished
	        		 }
	        	 };
	        	 JTBParallel.submitTask(runnables[i]);
	         }
	   }
}
