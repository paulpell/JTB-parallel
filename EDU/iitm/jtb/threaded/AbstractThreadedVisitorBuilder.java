package EDU.iitm.jtb.threaded;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import EDU.purdue.jtb.misc.ClassInfo;
import EDU.purdue.jtb.misc.Errors;
import EDU.purdue.jtb.misc.FileExistsException;
import EDU.purdue.jtb.misc.Globals;
import EDU.purdue.jtb.misc.Spacing;


/**
 * Each builder for a threaded visitor will generate a file in a threaded fashion, that is it will
 * send tasks to the thread pool in JTBParallel. Unfortunately that very thread pool does not have
 * the ability to stop when all the tasks are over, so we count them ourselves and this requires every
 * task to call taskEnd()....
 * 
 * The generated visitors will use a thread pool (ExecturService) with a fixed number of threads, specifiable at construction time.
 *
 */

public abstract class AbstractThreadedVisitorBuilder {
	private static final int INDENT_AMT = 3;
    protected final Spacing spc = new Spacing(INDENT_AMT);

    protected PrintWriter out;
    
    
		protected int id;
	   protected Vector<ClassInfo>[] classLists;
	   
	   // the threads will each work on one part of classLists
	   protected Runnable[] runnables;
	   protected int runningRunnables; // initialized with the final number of tasks
	   
	  
	   protected File visitorDir;
	   public AbstractThreadedVisitorBuilder(int index, Vector<ClassInfo>[] classLists) {
		   id = index;
		   this.classLists = classLists;
		   runnables = new Runnable[classLists.length];
	       runningRunnables = classLists.length;
		    
		   visitorDir = new File(Globals.visitorDir);
	
		   if ( !visitorDir.exists() )
		       visitorDir.mkdir();
		   else if ( !visitorDir.isDirectory() )
		       Errors.softErr("\"" + Globals.visitorDir + "\" exists but is not a " +
		                        "directory.");
	   }

	   protected void taskEnd() {
		   --runningRunnables;
		   if (runningRunnables == 0) { // if all tasks are finished
			   // polish the file, and signal the end of the visitor generation
		       spc.updateSpc(-1);
		       printLineSync(spc.spc + "}\n");
		       out.flush();
		       out.close();
		       JTBParallel.setFinished(this.id);
		   }
	   }

	   protected void printLineSync(String txt) {
		   synchronized (out) {
			   out.println(txt);
		   }
	   }
	   
	   protected void printVisitorThreadPoolCode() {
		   // since the pool does not provide a count of threads, we do it ourselves

         // freeThreads is of class Integer so we can use it with synchronized
		 printLineSync(spc.spc + "private ExecutorService threadPool;\n");
		 printLineSync("Integer tasks=0; // number of tasks currently running\n");
		 printLineSync("Integer totalCreatedTasks=0; // added number of tasks\n");
	         
		 printLineSync( spc.spc + "public synchronized void addTask(Runnable r) {\n" +
			 		spc.spc + "  ++tasks;\n" +
			 		spc.spc + "  ++totalCreatedTasks;\n" +
			 		spc.spc + "  threadPool.submit(r);\n" + 
			 		spc.spc + "}\n");
		 
		 printLineSync( spc.spc + "public synchronized void taskEnd() {\n" +
				spc.spc + "  --tasks;\n" +
				spc.spc + "  if (tasks == 0) {\n" +
				spc.spc + "    threadPool.shutdown();\n" +
				spc.spc + "  }\n" +
		 		spc.spc + "}\n");
	   }
	   
	   protected void printAutoVisitorMethods() {
		   printLineSync("   //\n" +
		   "   // Threaded Auto class visitors\n" +
		   "   //\n");

		  printLineSync(getNodeListVisitorStr());
		  printLineSync(getNodeListOptionalVisitorStr());
		  printLineSync(getNodeOptionalVisitorStr());
		  printLineSync(getNodeSequenceVisitorStr());
		  printLineSync(getNodeTokenVisitorStr());
		  printLineSync(getNodeChoiceVisitorStr());
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
	   
	public abstract void generateVisitorFile() throws FileExistsException;
}
