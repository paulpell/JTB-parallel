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
					ThreadedVisitor v = new ThreadedVisitor();
					r.accept(v, true);
					
					// let's give some time to the tasks to finish
					try {
						while (!v.isTerminated())
							Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(0);
					}

					// we have to explicitly end the thread pool, too bad
					v.shutdown();
					//r.accept(new DepthFirstVisitor());
				} catch (ParseException e) {
						e.printStackTrace();
				}
		}
}
