//
// Generated by JTB 1.3.1
//

package EDU.purdue.jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> "{"
 * f1 -> ( ClassBodyDeclaration() )*
 * f2 -> "}"
 */
public class ClassBody implements Node {
   public NodeToken f0;
   public NodeListOptional f1;
   public NodeToken f2;

   public ClassBody(NodeToken n0, NodeListOptional n1, NodeToken n2) {
      f0 = n0;
      f1 = n1;
      f2 = n2;
   }

   public ClassBody(NodeListOptional n0) {
      f0 = new NodeToken("{");
      f1 = n0;
      f2 = new NodeToken("}");
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

