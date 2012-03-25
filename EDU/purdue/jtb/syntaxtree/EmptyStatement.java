//
// Generated by JTB 1.3.1
//

package EDU.purdue.jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> ";"
 */
public class EmptyStatement implements Node {
   public NodeToken f0;

   public EmptyStatement(NodeToken n0) {
      f0 = n0;
   }

   public EmptyStatement() {
      f0 = new NodeToken(";");
   }

   public void accept(EDU.purdue.jtb.visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(EDU.purdue.jtb.visitor.GJVisitor<R,A> v, A argu) {
      return v.visit(this,argu);
   }
   public <R> R accept(EDU.purdue.jtb.visitor.GJNoArguVisitor<R> v) {
      return v.visit(this);
   }
   public <A> void accept(EDU.purdue.jtb.visitor.GJVoidVisitor<A> v, A argu) {
      v.visit(this,argu);
   }
   public void accept(EDU.purdue.jtb.visitor.ThreadedVisitor v) {
      v.visit(this);
   }
}

