//
// Generated by JTB 1.3.1
//

package EDU.purdue.jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> Literal()
 *       | Name()
 *       | "this"
 *       | "super" "." <IDENTIFIER>
 *       | "(" Expression() ")"
 *       | AllocationExpression()
 */
public class PrimaryPrefix implements Node {
   public NodeChoice f0;

   public PrimaryPrefix(NodeChoice n0) {
      f0 = n0;
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

