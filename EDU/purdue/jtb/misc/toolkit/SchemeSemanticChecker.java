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

package EDU.purdue.jtb.misc.toolkit;

import EDU.purdue.jtb.syntaxtree.*;
import EDU.purdue.jtb.visitor.DepthFirstVisitor;
import EDU.purdue.jtb.visitor.ExpansionUnitTypeCounter;
import EDU.purdue.jtb.misc.Errors;
import java.util.*;

/*
 * Class SchemeSemanticChecker checks over a grammar to make sure it conforms
 * to the following constraints imposed by the Scheme tree builder:
 *
 *   -Choices (i.e. ( x | y )) may only occur at the top level of a production,
 *    between nonterminals.
 *   -Whatever goes within a list, optional list, or optional must be a
 *    single non-terminal.
 */
public class SchemeSemanticChecker extends DepthFirstVisitor {
   private boolean errorReported = false; // Only report errors in a prod. once
   private boolean topLevel = true;
   private String curProd;

   //
   // f0 -> JavaCCOptions()
   // f1 -> <PARSER_BEGIN_TK>
   // f2 -> <LPAREN>
   // f3 -> <IDENTIFIER>
   // f4 -> <RPAREN>
   // f5 -> CompilationUnit()
   // f6 -> <PARSER_END_TK>
   // f7 -> <LPAREN>
   // f8 -> <IDENTIFIER>
   // f9 -> <RPAREN>
   // f10 -> ( Production() )*
   // f11 -> <EOF>
   //
   public void visit(JavaCCInput n) {
      n.f10.accept(this);
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
   // f0 -> <JAVACODE_TK>
   // f1 -> ResultType()
   // f2 -> <IDENTIFIER>
   // f3 -> FormalParameters()
   // f4 -> Block()
   //
   public void visit(JavaCodeProduction n) {
      // Don't visit
   }

   //
   // f0 -> ResultType()
   // f1 -> <IDENTIFIER>
   // f2 -> FormalParameters()
   // f3 -> <COLON>
   // f4 -> Block()
   // f5 -> <LBRACE>
   // f6 -> ExpansionChoices()
   // f7 -> <RBRACE>
   //
   public void visit(BNFProduction n) {
      curProd = n.f1.tokenImage;
      n.f6.accept(this);
      errorReported = false;
   }

   //
   // f0 -> [ LexicalStateList() ]
   // f1 -> RegExprKind()
   // f2 -> [ <LBRACKET> <IGNORE_CASE_TK> <RBRACKET> ]
   // f3 -> <COLON>
   // f4 -> <LBRACE>
   // f5 -> RegExprSpec()
   // f6 -> ( <BIT_OR> RegExprSpec() )*
   // f7 -> <RBRACE>
   //
   public void visit(RegularExprProduction n) {
      // Don't visit
   }

   //
   // f0 -> <TOKEN_MGR_DECLS_TK>
   // f1 -> <COLON>
   // f2 -> ClassBodyDeclaration()
   //
   public void visit(TokenManagerDecls n) {
      // Don't visit
   }

   //
   // f0 -> Expansion()
   // f1 -> ( <BIT_OR> Expansion() )*
   //
   public void visit(ExpansionChoices n) {
      if ( n.f1.present() ) {
         if ( !errorReported && !topLevel ) {
            Errors.softErr("In " + curProd + "()--choices may occur " +
               "only at the top level.");
            errorReported = true;
         }
         else {
            topLevel = false;
            n.f0.accept(this);
            n.f1.accept(this);
            topLevel = true;
         }
      }
      else {
         n.f0.accept(this);
         n.f1.accept(this);
      }
   }

   //
   // f0 -> ( ExpansionUnit() )*
   //
   public void visit(Expansion n) {
      if ( !errorReported && !topLevel && n.f0.size() > 1 ) {
         ExpansionUnitTypeCounter v = new ExpansionUnitTypeCounter();
         n.accept(v);

         if ( v.getNumNormals() > 1 ) {
            Errors.softErr("In " + curProd + "()--only single " +
               "nonterminals may appear below the top level.");
            errorReported = true;
         }
      }
      else
         n.f0.accept(this);
   }

   //
   // f0 -> LocalLookahead()
   //       | Block()
   //       | <LPAREN> ExpansionChoices() <RPAREN> [ <PLUS> | <STAR> | <HOOK> ]
   //       | <LBRACKET> ExpansionChoices() <RBRACKET>
   //       | [ PrimaryExpression() <ASSIGN> ] ExpansionUnitTerm()
   //
   public void visit(ExpansionUnit n) {
      switch ( n.f0.which ) {
         case 0: return;
         case 1: return;
         case 2: // fall through to 3
         case 3:
            if ( topLevel ) {
               topLevel = false;
               n.f0.accept(this);
               topLevel = true;
            }
            else n.f0.accept(this);
         case 4: return;
         default:
            Errors.hardErr("n.f0.which = " + String.valueOf(n.f0.which));
            break;
      }
   }

   //
   // f0 -> <LOOKAHEAD_TK>
   // f1 -> <LPAREN>
   // f2 -> [ <INTEGER_LITERAL> ]
   // f3 -> [ <COMMA> ]
   // f4 -> ExpansionChoices()
   // f5 -> [ <COMMA> ]
   // f6 -> [ <LBRACE> Expression() <RBRACE> ]
   // f7 -> <RPAREN>
   //
   public void visit(LocalLookahead n) {
      // Don't visit
   }
}
