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
    
    // we need id when we signal the builder is finished, to let Main know which builder it is
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
	   
	   protected void printAutoVisitorMethods(String ret, String arg) {
		   printLineSync("   //\n" +
		   "   // Threaded Auto class visitors\n" +
		   "   //\n");

		  printLineSync(getNodeListVisitorStr(ret, arg));
		  printLineSync(getNodeListOptionalVisitorStr(ret, arg));
		  printLineSync(getNodeOptionalVisitorStr(ret, arg));
		  printLineSync(getNodeSequenceVisitorStr(ret, arg));
		  printLineSync(getNodeTokenVisitorStr(ret, arg));
		  printLineSync(getNodeChoiceVisitorStr(ret, arg));
	   }

	   private String getEnumerationStr(String argType) {
		   return  "  for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {\n" +
				   "    e.nextElement().accept(this, " +
				   (argType == null ? "" : "argu, ") + "true);\n" +
				   "  }\n";
	   }
	   
	   private String getNodeListVisitorStr(String retType, String argType) {
		   String funcStr = 
				   	"public " + retType +" visit(NodeList n" + 
					(argType == null ? "" : ", " + argType + " argu") + ") {\n" +
					getEnumerationStr(argType) +
					(retType.equals("void") ? "" : "return null;") +
					"}\n";
		   return funcStr;
	   }
	   private String getNodeListOptionalVisitorStr(String retType, String argType) {
		   return "public " + retType + " visit(NodeListOptional n" +
				   (argType == null ? "" : ", " + argType + " argu") + ") {\n" +
				   "  if (!n.present()) return" + 
				   (retType.equals("void") ? "" : " null") + ";\n" +
				   getEnumerationStr(argType) +
				   (retType.equals("void") ? "" : "return null;") +
					"}\n";
	   }
	   
	   private String getNodeSequenceVisitorStr(String retType, String argType){
		   return "public " + retType + " visit(NodeSequence n" +
				   (argType == null ? "" : ", " + argType + " argu") + ") {\n" +
				   getEnumerationStr(argType) + 
				   (retType.equals("void") ? "" : "return null;") +
					"}\n";
	   }
	   
	   private String getNodeChoiceVisitorStr(String retType, String argType){
		   return "public " + retType + " visit(NodeChoice n" + 
				   (argType == null ? "" : ", " + argType + " argu") + ") {\n  " +
				   (retType.equals("void") ? "" : "return ") +
				   "n.choice.accept(this, " + 
				   (argType == null ? "" : "argu, ")+"true);\n" +
				   "}\n";
	   }
	   
	   private String getNodeOptionalVisitorStr(String retType, String argType) {
		   return "public " + retType + " visit(NodeOptional n " + 
				    (argType == null ? "" : ", " + argType + " argu") + ") {\n" +
				   	"  if (!n.present()) return" + 
				    (retType.equals("void") ? "" : " null") + ";\n  " +
					   (retType.equals("void") ? "" : "return ") +
				    "n.node.accept(this, " + 
				   (argType == null ? "" : "argu, ")+"true);\n" +
				   "}\n";
	   }
	   
	   private String getNodeTokenVisitorStr(String retType, String argType) {
		   // do nothing
		   return "public " + retType + " visit(NodeToken n"+ 
				   (argType == null ? "" : ", " + argType +  " argu") + ") {" + 
				   (retType.equals("void") ? "" : " return null;") + "}\n";
	   }
	   
	public abstract void generateVisitorFile() throws FileExistsException;
}
