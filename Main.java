import visitor.*;
import syntaxtree.*;

import java.io.*;

public class Main {
		public static void main(String[] args) {

				InputStream in = null;

				try {
					in = new FileInputStream(args[0]);
				} catch(Exception e){
						e.printStackTrace();
						System.exit(0);
				}

				JTBParser p = new JTBParser(in);// for JTB grammar
                //JavaParser p =new JavaParser(in); // for Java.jj
				try {
					Node r = p.CompilationUnit();

          // uncomment one of the following visitors to test

                    //GJThreadedVisitor v = new GJThreadedVisitor();
                    //r.accept(v, null, true);

					//GJNoArguThreadedVisitor va = new GJNoArguThreadedVisitor();
                    //r.accept(va, true);
                    
                    //GJVoidThreadedVisitor vv = new GJVoidThreadedVisitor();
					//r.accept(v, null, true);

                    //ThreadedVisitor v2 = new ThreadedVisitor();
                    //r.accept(v2, true);
					
					//r.accept(new DepthFirstVisitor());
				} catch (ParseException e) {
						e.printStackTrace();
				}
		}
}
