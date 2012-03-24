package EDU.iitm.jtb.threaded;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import EDU.purdue.jtb.misc.ClassInfo;
import EDU.purdue.jtb.misc.Errors;
import EDU.purdue.jtb.misc.FileExistsException;
import EDU.purdue.jtb.misc.Globals;
import EDU.purdue.jtb.misc.Spacing;

public class ThreadedVisitorBuilder {
	public static final int INDENT_AMT = 3;
	
	private String ThreadedVisitorName = "ThreadedVisitor";

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
		            throw new FileExistsException(Globals.GJNoArguVisitorName + ".java");

		         PrintWriter out = new PrintWriter(new FileOutputStream(file), false);
		         Spacing spc = new Spacing(INDENT_AMT);

		         out.println(Globals.fileHeader(spc));
		         out.println();
		         out.println(spc.spc + "package " + Globals.visitorPackage + ";");
		         if ( !Globals.visitorPackage.equals(Globals.nodePackage) )
		            out.println(spc.spc + "import " + Globals.nodePackage + ".*;");
		         out.println(spc.spc + "import java.util.*;\n");
		         out.println(spc.spc + "/**");
		         out.println(spc.spc + " * All threaded visitors must implement this interface");
		         out.println(spc.spc + " */\n");
		         out.println(spc.spc + "public interface " + ThreadedVisitorName + " {\n");
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
		            out.print(spc.spc + "public void visit");
		            out.println("(" + name + " n);\n");
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
	   
	   private String getNodeListVisitorStr() {
		   return "public void visit(NodeList n);\n";
	   }
	   private String getNodeListOptionalVisitorStr() {
		   return "public void visit(NodeListOptional n);\n";
	   }
	   
	   private String getNodeOptionalVisitorStr(){
		   return "public void visit(NodeOptional n);\n";
	   }
	   
	   private String getNodeSequenceVisitorStr() {
		   return "public void visit(NodeSequence n);\n";
	   }
	   
	   private String getNodeTokenVisitorStr() {
		   return "public void visit(NodeToken n);\n";
	   }
}
