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

				JTBParser p = new JTBParser(in);
				try {
					Node r = p.CompilationUnit();
					r.accept(new ThreadedVisitor(), false);
					//r.accept(new DepthFirstVisitor());
				} catch (ParseException e) {
						e.printStackTrace();
				}
		}
}
