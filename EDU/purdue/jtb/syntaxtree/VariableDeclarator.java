//
// Generated by JTB 1.3.1
//

package EDU.purdue.jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> VariableDeclaratorId()
 * f1 -> [ "=" VariableInitializer() ]
 */
public class VariableDeclarator implements Node {
   public VariableDeclaratorId f0;
   public NodeOptional f1;

   public VariableDeclarator(VariableDeclaratorId n0, NodeOptional n1) {
      f0 = n0;
      f1 = n1;
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
}

