/**
 * Copyright (c) 2004,2005 UCLA Compilers Group. 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *  Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 * 
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 * 
 *  Neither UCLA nor the names of its contributors may be used to endorse 
 *  or promote products derived from this software without specific prior 
 *  written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

/*
 * All files in the distribution of JTB, The Java Tree Builder are
 * Copyright 1997, 1998, 1999 by the Purdue Research Foundation of Purdue
 * University.  All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that this entire copyright notice is duplicated in all
 * such copies, and that any documentation, announcements, and
 * other materials related to such distribution and use acknowledge
 * that the software was developed at Purdue University, West Lafayette,
 * Indiana by Kevin Tao and Jens Palsberg.  No charge may be made
 * for copies, derivations, or distributions of this material
 * without the express written consent of the copyright holder.
 * Neither the name of the University nor the name of the author
 * may be used to endorse or promote products derived from this
 * material without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR ANY PARTICULAR PURPOSE.
 */

package EDU.purdue.jtb.visitor;

import EDU.purdue.jtb.syntaxtree.*;
import EDU.purdue.jtb.misc.JavaStringMaker;
import EDU.purdue.jtb.misc.Spacing;
import EDU.purdue.jtb.misc.Errors;
import java.io.*;
import java.util.*;

/**
 * Class Printer is a pretty printer for the JavaCC grammar.
 */
public class Printer extends DepthFirstVisitor {
   protected PrintWriter out;
   protected int nestLevel = 0;
   protected Spacing spc;
   protected JavaStringMaker javaStringMaker;

   public Printer()                 { this(System.out); }
   public Printer(Writer w)         { this(w, new Spacing(3)); }
   public Printer(Writer w, Spacing s) {
      out = new PrintWriter(w);
      spc = s;
      javaStringMaker = new JavaStringMaker(spc);
   }
   public Printer(OutputStream o) {
      out = new PrintWriter(o);
      spc = new Spacing(3);
      javaStringMaker = new JavaStringMaker(spc);
   }

   public void setOut(Writer w)  { out = new PrintWriter(w); } 
   public void flushWriter()        { out.flush(); }

   //
   // A few convenience methods
   //
   protected String javaString(Node n) { return javaStringMaker.javaString(n); }

   protected void visit(NodeList n, String sep) {
      for ( Enumeration e = n.elements(); e.hasMoreElements(); ) {
         ((Node)e.nextElement()).accept(this);
         if ( e.hasMoreElements() ) out.print(sep);
      }
   }

   protected void visit(NodeListOptional n, String sep) {
      if ( n.present() )
         for ( Enumeration e = n.elements(); e.hasMoreElements(); ) {
            ((Node)e.nextElement()).accept(this);
            if ( e.hasMoreElements() ) out.print(sep);
         }
   }

   protected void visit(NodeOptional n, String sep) {
      if ( n.present() ) {
         n.node.accept(this);
         out.print(sep);
      }
   }

   //
   // For convenience, trusts that the node passed to it is a NodeSequence.
   // (of course, will throw an exception if it isn't).
   //
   protected void visit(Node n1, String sep) {
      NodeSequence n = (NodeSequence)n1;
      for ( Enumeration e = n.elements(); e.hasMoreElements(); ) {
         ((Node)e.nextElement()).accept(this);
         if ( e.hasMoreElements() ) out.print(sep);
      }
   }

   protected void visit(NodeToken n, String sep) {
      out.print(n.tokenImage + sep);
   }

   public void visit(NodeToken n) { out.print(n.tokenImage); }

   //
   // User generated visitor methods below
   //
   //
   // f0 -> JavaCCOptions()
   // f1 -> < PARSER_BEGIN_TK > 
   // f2 -> < LPAREN > 
   // f3 -> < IDENTIFIER > 
   // f4 -> < RPAREN > 
   // f5 -> CompilationUnit()
   // f6 -> < PARSER_END_TK > 
   // f7 -> < LPAREN > 
   // f8 -> < IDENTIFIER > 
   // f9 -> < RPAREN > 
   // f10 -> ( Production() )*
   // f11 -> < EOF > 
   //
   public void visit(JavaCCInput n) {
      out.print(spc.spc);
      n.f0.accept(this);
      out.println("\n");
      out.println(spc.spc + n.f1 + n.f2 + n.f3 + n.f4);
      out.println(spc.spc + javaString(n.f5));
      out.println(spc.spc + n.f6 + n.f7 + n.f8 + n.f9 + "\n");
      out.print(spc.spc);
      visit(n.f10, "\n\n" + spc.spc);
      out.println();
      flushWriter();
   }

   //
   // f0 -> [ < OPTIONS_TK >  < LBRACE >  ( OptionBinding() )* < RBRACE >  ]
   //
   public void visit(JavaCCOptions n) {
      if ( n.f0.present() ) {
         NodeSequence seq = (NodeSequence)n.f0.node;
         NodeListOptional nlo = (NodeListOptional)seq.elementAt(2);

         out.println(seq.elementAt(0) + " " + seq.elementAt(1));
         if ( nlo.present() ) {
            spc.updateSpc(+1);
            out.print(spc.spc);
            visit(nlo, "\n" + spc.spc);
            out.println();
            spc.updateSpc(-1);
         }
         out.print(spc.spc + seq.elementAt(3));
      }
   }

   //
   // f0 -> < IDENTIFIER >  < ASSIGN >  < INTEGER_LITERAL >  < SEMICOLON >
   //       | < IDENTIFIER >  < ASSIGN >  BooleanLiteral() < SEMICOLON >
   //
   // NEW:
   //
   // f0 -> ( <IDENTIFIER> | <LOOKAHEAD_TK> | <IGNORE_CASE_TK> | <STATIC> )
   // f1 -> <ASSIGN>
   // f2 -> ( <INTEGER_LITERAL> | BooleanLiteral() | <STRING_LITERAL> )
   // f3 -> <SEMICOLON>
   //
   public void visit(OptionBinding n) {
/* Changed for new JTB 1.1 grammar
      NodeSequence seq = (NodeSequence)n.f0.choice;
      out.print(seq.elementAt(0) + " " + seq.elementAt(1) + " ");
      seq.elementAt(2).accept(this);
      seq.elementAt(3).accept(this);
*/
      n.f0.accept(this);
      out.print(" " + n.f1 + " ");
      n.f2.accept(this);
      n.f3.accept(this);
   }

   //
   // f0 -> JavaCodeProduction()
   //       | RegularExprProduction() 
   //       | BNFProduction() 
   //       | TokenManagerDecls() 
   //
   public void visit(Production n) {
      n.f0.accept(this);
   }

   //
   // f0 -> < JAVACODE_TK > 
   // f1 -> ResultType()
   // f2 -> < IDENTIFIER > 
   // f3 -> FormalParameters()
   // f4 -> Block()
   //
   public void visit(JavaCodeProduction n) {
      out.println(n.f0);
      out.println(spc.spc + javaString(n.f1) + " " + n.f2 + javaString(n.f3));
      out.print(spc.spc + javaString(n.f4));
   }
    /**
     * new Grammar production:
     * f0 -> ResultType()
     * f1 -> <IDENTIFIER>
     * f2 -> FormalParameters()
     * f3 -> <COLON>
     * f4 -> "{"
     * f5 -> ( BlockStatement() )*
     * f6 -> "}"
     * f7 -> <LBRACE>
     * f8 -> ExpansionChoices()
     * f9 -> <RBRACE>
     */
    
    /**
     * old Grammar production: 
     * f0 -> ResultType()
     * f1 -> <IDENTIFIER>
     * f2 -> FormalParameters()
     * f3 -> <COLON>
     * f4 -> Block()
     * f5 -> <LBRACE>
     * f6 -> ExpansionChoices()
     * f7 -> <RBRACE>
     */
   public void visit(BNFProduction n) {
      nestLevel = 0;

      out.println(javaString(n.f0) + " " + n.f1 + javaString(n.f2) + " " +n.f3);
      out.println(spc.spc + javaString(n.f5));
      out.println(spc.spc + n.f7);

      spc.updateSpc(+1);
      out.print(spc.spc);
      n.f8.accept(this);
      out.println();
      spc.updateSpc(-1);

      out.print(spc.spc + n.f9);
   }

   //
   // f0 -> [ LexicalStateList() ]
   // f1 -> RegExprKind()
   // f2 -> [ < LBRACKET >  < IGNORE_CASE_TK >  < RBRACKET >  ]
   // f3 -> < COLON > 
   // f4 -> < LBRACE > 
   // f5 -> RegExprSpec()
   // f6 -> ( < BIT_OR >  RegExprSpec() )*
   // f7 -> < RBRACE > 
   //
   public void visit(RegularExprProduction n) {
      visit(n.f0, " ");
      n.f1.accept(this);
      out.print(" ");
      visit(n.f2, " ");
      out.println(n.f3);
      out.println(spc.spc + n.f4);

      spc.updateSpc(+1);
      out.print(spc.spc);
      n.f5.accept(this);
      out.println();

      if ( n.f6.present() ) {
         out.print(spc.spc);
         for ( Enumeration e = n.f6.elements(); e.hasMoreElements(); ) {
            NodeSequence seq = (NodeSequence)e.nextElement();
            out.print(seq.elementAt(0) + " ");
            seq.elementAt(1).accept(this);
            out.println();
            if ( e.hasMoreElements() )
               out.print(spc.spc);
         }
      }

      spc.updateSpc(-1);
      out.print(spc.spc);
      n.f7.accept(this);
   }

   //
   // f0 -> < TOKEN_MGR_DECLS_TK > 
   // f1 -> < COLON > 
   // f2 -> ClassBodyDeclaration()
   //
   public void visit(TokenManagerDecls n) {
      n.f0.accept(this);
      n.f1.accept(this);
      out.println(spc.spc + javaString(n.f2));
   }

   //
   // f0 -> < LT >  < STAR >  < GT > 
   //       | < LT >  < IDENTIFIER >  ( < COMMA >  < IDENTIFIER >  )* < GT >  
   //
   public void visit(LexicalStateList n) {
      if ( n.f0.which == 0 )
         n.f0.accept(this);
      else {
         NodeSequence seq = (NodeSequence)n.f0.choice;
         NodeListOptional nlo = (NodeListOptional)seq.elementAt(2);

         out.print(seq.elementAt(0).toString() + seq.elementAt(1).toString());
         if ( nlo.present() )
            for ( Enumeration e = nlo.elements(); e.hasMoreElements(); ) {
               NodeSequence seq1 = (NodeSequence)e.nextElement();
               out.print(seq1.elementAt(0) + " " + seq1.elementAt(1));
            }
         out.print(seq.elementAt(3));
      }
   }

   //
   // f0 -> < TOKEN_TK > 
   //       | < SPECIAL_TOKEN_TK >  
   //       | < SKIP_TK >  
   //       | < MORE_TK >  
   //
   public void visit(RegExprKind n) {
      n.f0.accept(this);
   }

   //
   // f0 -> RegularExpression()
   // f1 -> [ Block() ]
   // f2 -> [ < COLON >  < IDENTIFIER >  ]
   //
   public void visit(RegExprSpec n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.println();
         spc.updateSpc(+1);
         out.print(spc.spc + javaString(n.f1.node));
         spc.updateSpc(-1);
      }
      if ( n.f2.present() ) {
         NodeSequence seq = (NodeSequence)n.f2.node;
         out.print(" " + seq.elementAt(0) + " " + seq.elementAt(1));
      }
   }

   //
   // f0 -> Expansion()
   // f1 -> ( < BIT_OR >  Expansion() )*
   //
   public void visit(ExpansionChoices n) {
      if ( !n.f1.present() )
         n.f0.accept(this);
      else {
         ++nestLevel;
         n.f0.accept(this);
         --nestLevel;

         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); ) {
            NodeSequence seq = (NodeSequence)e.nextElement();

            if ( nestLevel != 0 ) out.print(" ");
            else {
               out.println();
               out.print(spc.spc);
            }

            out.print(seq.elementAt(0) + " ");
            ++nestLevel;
            seq.elementAt(1).accept(this);
            --nestLevel;
         }
      }
   }

   //
   // f0 -> ( ExpansionUnit() )*
   //
   public void visit(Expansion n) {
      if ( nestLevel == 0 )   visit(n.f0, "\n" + spc.spc);
      else                    visit(n.f0, " ");
   }

   //
   // f0 -> LocalLookahead()
   //       | Block() 
   //       | < LPAREN >  ExpansionChoices() < RPAREN >  [ < PLUS > | < STAR >  | < HOOK >   ] 
   //       | < LBRACKET >  ExpansionChoices() < RBRACKET >  
   //       | [ PrimaryExpression() < ASSIGN >  ] ExpansionUnitTerm() 
   //
   public void visit(ExpansionUnit n) {
      NodeSequence seq;
      switch ( n.f0.which ) {
         case 0:
            n.f0.accept(this);
            break;
         case 1:
            out.println();
            out.print(spc.spc);
            out.print(javaString(n.f0.choice));
            out.println();
            out.print(spc.spc);
            break;
         case 2:
            seq = (NodeSequence)n.f0.choice;
            out.print(seq.elementAt(0) + " ");
            ++nestLevel;
            seq.elementAt(1).accept(this);
            --nestLevel;
            out.print(" " + seq.elementAt(2));
            seq.elementAt(3).accept(this);
            break;
         case 3:
            seq = (NodeSequence)n.f0.choice;
            out.print(seq.elementAt(0) + " ");
            ++nestLevel;
            seq.elementAt(1).accept(this);
            --nestLevel;
            out.print(" " + seq.elementAt(2));
            break;
         case 4:
            n.f0.accept(this);
            break;
         default:
            Errors.hardErr("n.f0.which = " + String.valueOf(n.f0.which));
            break;
      }
   }

   //
   // f0 -> RegularExpression()
   //       | < IDENTIFIER >  Arguments() 
   //
   public void visit(ExpansionUnitTerm n) {
      n.f0.accept(this);
   }

   //
   // f0 -> < LOOKAHEAD_TK > 
   // f1 -> < LPAREN > 
   // f2 -> [ < INTEGER_LITERAL >  ]
   // f3 -> [ < COMMA >  ]
   // f4 -> ExpansionChoices()
   // f5 -> [ < COMMA >  ]
   // f6 -> [ < LBRACE >  Expression() < RBRACE >  ]
   // f7 -> < RPAREN > 
   //
   public void visit(LocalLookahead n) {
      out.print(n.f0.toString() + n.f1.toString());
      n.f2.accept(this);
      visit(n.f3, " ");
      ++nestLevel;
      n.f4.accept(this);
      --nestLevel;
      visit(n.f5, " ");
      if ( n.f6.present() ) {
         NodeSequence seq = (NodeSequence)n.f6.node;
         out.print(seq.elementAt(0) + " " + javaString(seq.elementAt(1)) +
                   " " + seq.elementAt(2));
      }
      n.f7.accept(this);
   }

   //
   // f0 -> < STRING_LITERAL > 
   //       | < LT >  [ [ < POUND >  ] < IDENTIFIER >  < COLON >  ] ComplexRegularExpressionChoices() < GT >  
   //       | < LT >  < IDENTIFIER >  < GT >  
   //       | < LT >  < EOF_TK >  < GT >  
   //
   public void visit(RegularExpression n) {
      if ( n.f0.which != 1 )
         n.f0.accept(this);
      else {
         NodeSequence seq = (NodeSequence)n.f0.choice;
         NodeOptional opt = (NodeOptional)seq.elementAt(1);
         seq.elementAt(0).accept(this);
         if ( opt.present() ) {
            NodeSequence seq1 = (NodeSequence)opt.node;
            seq1.elementAt(0).accept(this);
            out.print(seq1.elementAt(1).toString() +
                      seq1.elementAt(2).toString() + " ");
         }

         seq.elementAt(2).accept(this);
         seq.elementAt(3).accept(this);
      }
   }

   //
   // f0 -> ComplexRegularExpression()
   // f1 -> ( < BIT_OR >  ComplexRegularExpression() )*
   //
   public void visit(ComplexRegularExpressionChoices n) {
      n.f0.accept(this);
      for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); ) {
         out.print(" ");
         visit((NodeSequence)e.nextElement(), " ");
      }
   }

   //
   // f0 -> ( ComplexRegularExpressionUnit() )*
   //
   public void visit(ComplexRegularExpression n) {
      visit(n.f0, " ");
   }

   //
   // f0 -> < STRING_LITERAL > 
   //       | < LT >  < IDENTIFIER >  < GT >  
   //       | CharacterList() 
   //       | < LPAREN >  ComplexRegularExpressionChoices() < RPAREN >  [ < PLUS > | < STAR >  | < HOOK >   ] 
   //
   public void visit(ComplexRegularExpressionUnit n) {
      n.f0.accept(this);
   }

   //
   // f0 -> [ < TILDE >  ]
   // f1 -> < LBRACKET > 
   // f2 -> [ CharacterDescriptor() ( < COMMA >  CharacterDescriptor() )* ]
   // f3 -> < RBRACKET > 
   //
   public void visit(CharacterList n) {
      n.f0.accept(this);
      n.f1.accept(this);
      if ( n.f2.present() ) {
         NodeSequence seq = (NodeSequence)n.f2.node;
         seq.elementAt(0).accept(this);
         for ( Enumeration e = ((NodeListOptional)seq.elementAt(1)).elements();
               e.hasMoreElements(); ) {
            NodeSequence seq1 = (NodeSequence)e.nextElement();
            out.print(seq1.elementAt(0) + " ");
            seq1.elementAt(1).accept(this);
         }
      }
      n.f3.accept(this);
   }

   //
   // f0 -> < STRING_LITERAL > 
   // f1 -> [ < MINUS >  < STRING_LITERAL >  ]
   //
   public void visit(CharacterDescriptor n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }
}
