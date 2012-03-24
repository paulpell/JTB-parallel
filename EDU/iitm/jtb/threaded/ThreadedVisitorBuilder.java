package EDU.iitm.jtb.threaded;

import java.io.File;
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
 * In the visitor, there will be a limit to the number of threads created, specified at construction time. 
 * At every node, the visitor will check whether it can launch threads to do the job.
 * Otherwise, it will go on sequentially. This is done by calling the getThread() method, which checks
 * whether the variable freeThreads is greater than 0, and decrementing it if so.
 * After a thread is finished, it calls freeThread(), which in turn increments freeThreads again.
 *
 * TODO in the generation for the user specified classes, the fields may not be called f0, f1, etc.
 */

public class ThreadedVisitorBuilder {
	public static final int INDENT_AMT = 3;
	
	private String ThreadedVisitorName = "ThreadedVisitor";
	
	private int defaultMaxThreads = 4;

	   private Vector classList;
	   private File visitorDir;

	   /**
	    * Vector must contain objects of type ClassInfo
	    */
	   public ThreadedVisitorBuilder(Vector classes) {
	      classList = classes;
	      
	      visitorDir = new File(Globals.visitorDir);

	      if ( !visitorDir.exists() )
	         visitorDir.mkdir();
	      else if ( !visitorDir.isDirectory() )
	         Errors.softErr("\"" + Globals.visitorDir + "\" exists but is not a " +
	                        "directory.");
	   }
	   
	   public void generateVisitorFile() throws FileExistsException {
		   
		   try {
		         File file = new File(visitorDir, ThreadedVisitorName + ".java");

		         if ( Globals.noOverwrite && file.exists() )
		            throw new FileExistsException(ThreadedVisitorName + ".java");

		         PrintWriter out = new PrintWriter(new FileOutputStream(file), false);
		         Spacing spc = new Spacing(INDENT_AMT);

		         out.println(Globals.fileHeader(spc));
		         out.println();
		         out.println(spc.spc + "package " + Globals.visitorPackage + ";");
		         if ( !Globals.visitorPackage.equals(Globals.nodePackage) )
		            out.println(spc.spc + "import " + Globals.nodePackage + ".*;");
		         out.println(spc.spc + "import java.util.*;\n");
		         out.println(spc.spc + "/**");
		         out.println(spc.spc + " * Base class for a threaded visitor");
		         out.println(spc.spc + " */\n");
		         out.println(spc.spc + "public class " + ThreadedVisitorName + " {\n");
		         

		         // freeThreads is of class Integer so we can use it with synchronized
		         out.println(spc.spc + "private Integer freeThreads;\n");
		         
		         // default constructor
		         out.println(spc.spc +    "public " + ThreadedVisitorName + "() {\n" +
		        		 		spc.spc + "  this(" + defaultMaxThreads + ");\n" +
		        		 		spc.spc + "}");
		         // constructor to specify the maximum number of threads
		         out.println(spc.spc +    "public " + ThreadedVisitorName + "(int maxNoThreads) {\n" +
		        		 		spc.spc + "  freeThreads = maxNoThreads;\n" +
		        		 		spc.spc + "}\n");
		         
		         // the synchronized methods to use the thread counts
		         out.println(spc.spc +    "public void freeThread() {\n" +
		        		 		spc.spc + "  synchronized (freeThreads) {\n" +
		        		 		spc.spc + "    ++freeThreads;\n" +
		        		 		spc.spc + "  }\n" +
		        		 		spc.spc + "}\n");
		         out.println(spc.spc +    "public boolean getThread() {\n" +
		        		 		spc.spc + "  synchronized (freeThreads) {\n" +
		        		 		spc.spc + "    if (freeThreads == 0)\n" +
		        		 		spc.spc + "      return false;\n" +
		        		 		spc.spc + "    --freeThreads;\n" +
		        		 		spc.spc + "    return true;\n" +
		        		 		spc.spc + "  }\n" +
		        		 		spc.spc + "}\n");
		         
		         
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
		            
		            // we will need to know which fields exist in the class (f0, f1, etc.)
		            Field[] fields = null;
		            try {
		            	fields = Class.forName(name).getFields();
		            } catch (ClassNotFoundException ex) {
		            	Errors.hardErr("Could not create visit method for class " + name);
		            }
		            if (fields == null) {
		            	out.close();
		            	return;
		            }
		            out.println (spc.spc +    "public void visit(" + name + " n) {");
		            for (int i=0; i<fields.length; i++) {
		            	String fieldName = fields[i].toString();
		            	if (fieldName.startsWith("f")) { // TODO remove this check?
		            		out.println(
		            				spc.spc + "  if (getThread()) {\n" +
		            				spc.spc + "    new Thread() {\n" +
		            				spc.spc + "      n." + fieldName +".accept(this);\n" +
		            				spc.spc + "      freeThread();\n" +
		            				spc.spc + "    }.start();\n" +
		            				spc.spc + "  }\n" +
		            				spc.spc + "  else\n" +
		            				spc.spc + "    n." + fieldName + ".accept(this);\n");
		            	}
		            }
		            out.println(spc.spc + "}");
		            
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
		   out.println("   // Threaded Auto class visitors");
		   out.println("   //\n");

		      
	      out.print(getNodeListVisitorStr());
	      out.print(getNodeListOptionalVisitorStr());
	      out.print(getNodeOptionalVisitorStr());
	      out.print(getNodeSequenceVisitorStr());
	      out.print(getNodeTokenVisitorStr());
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
				   "}";
	   }
	   private String getNodeListOptionalVisitorStr() {
		   return "public void visit(NodeListOptional n) {\n" +
				   "  if (!n.present()) return;\n" +
				   getEnumerationStr() +
				   "}";
	   }
	   
	   private String getNodeSequenceVisitorStr(){
		   return "public void visit(NodeSequence n) {\n" +
				   getEnumerationStr() + 
				   "}";
	   }
	   
	   // even though there is only one node to visit, we will try to launch a thread:
	   // one may have been freed in between
	   private String getNodeOptionalVisitorStr() {
		   return "public void visit(NodeOptional n) {\n" +
				   	"if (!n.present()) return;\n"+
				   	"if (getThread()) {\n"+
				   	"  new Thread() {\n" +
				   	"    public void run() {\n" +
				   	"      n.node.accept(ThreadedVisitor.this)\n" +
				   	"      freeThread();\n" +
				   	"    }\n" +
				   	"  }.start();\n" +
				   	"}\n"+
				   	"else\n"+
				   	"  n.node.accept(this);";
	   }
	   
	   private String getNodeTokenVisitorStr() {
		   // do nothing
		   return "public void visit(NodeToken n) {}\n";
	   }
}
