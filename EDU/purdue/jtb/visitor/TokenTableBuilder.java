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
import EDU.purdue.jtb.misc.Errors;
import java.util.*;

/**
 * Generates a symbol lookup table of tokens which have a constant regular
 * expression, e.g. < PLUS : "+" >, which will be used to generate a default
 * constructor.
 */
public class TokenTableBuilder extends DepthFirstVisitor {
   private Hashtable table = new Hashtable();
   private String regExpr = "";

   /**
    * The returned Hashtable has the names of the tokens as the keys and the
    * constant regular expressions as the values or "" if the regular
    * expression is not constant.
    */
   public Hashtable getTokenTable() { return table; }

   /**
    * f0 ->JavaCCOptions()
    * f1 -> <PARSER_BEGIN_TK>
    * f2 -> <LPAREN>
    * f3 -> <IDENTIFIER>
    * f4 -> <RPAREN>
    * f5 -> CompilationUnit()
    * f6 -> <PARSER_END_TK>
    * f7 -> <LPAREN>
    * f8 -> <IDENTIFIER>
    * f9 -> <RPAREN>
    * f10 -> ( Production() )*
    * f11 -> <EOF>
    */
   public void visit(JavaCCInput n) {
      n.f10.accept(this);
   }

   /**
    * f0 -> JavaCodeProduction()
    *       | RegularExprProduction()
    *       | BNFProduction()
    *       | TokenManagerDecls()
    */
   public void visit(Production n) {
      if ( n.f0.which == 1 )  // only visit regular expression productions
         n.f0.accept(this);
   }

   /**
    * f0 -> [ LexicalStateList() ]
    * f1 -> RegExprKind()
    * f2 -> [ <LBRACKET> <IGNORE_CASE_TK> <RBRACKET> ]
    * f3 -> <COLON>
    * f4 -> <LBRACE>
    * f5 -> RegExprSpec()
    * f6 -> ( <BIT_OR> RegExprSpec() )*
    * f7 -> <RBRACE>
    */
   public void visit(RegularExprProduction n) {
      n.f5.accept(this);
      n.f6.accept(this);
   }

   /**
    * f0 -> RegularExpression()
    * f1 -> [ Block() ]
    * f2 -> [ <COLON> <IDENTIFIER> ]
    */
   public void visit(RegExprSpec n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> <STRING_LITERAL>
    *       | <LT> [ [ <POUND> ] <IDENTIFIER> <COLON> ] ComplexRegularExpressionChoices() <GT>
    *       | <LT> <IDENTIFIER> <GT>
    *       | <LT> <EOF_TK> <GT>
    */
   public void visit(RegularExpression n) {
      if ( n.f0.which == 1 ) {
         NodeSequence seq = (NodeSequence)n.f0.choice;
         NodeOptional opt = (NodeOptional)seq.elementAt(1);
         NodeSequence seq1;
         String name;

         if ( opt.present() ) {
            seq1 = (NodeSequence)opt.node;
            name = ((NodeToken)seq1.elementAt(1)).tokenImage;

            //
            // regExpr will be set further down the tree
            //
            seq.elementAt(2).accept(this);
            table.put(name, regExpr);
         }

         regExpr = "";      // reset for next pass
      }
   }

   /**
    * f0 -> ComplexRegularExpression()
    * f1 -> ( <BIT_OR> ComplexRegularExpression() )*
    */
   public void visit(ComplexRegularExpressionChoices n) {
      if ( !n.f1.present() )  // if f1 is present, this isn't a constant regexpr
         n.f0.accept(this);
      else
         regExpr = "";
   }

   /**
    * f0 -> ( ComplexRegularExpressionUnit() )*
    */
   public void visit(ComplexRegularExpression n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> <STRING_LITERAL>
    *       | <LT> <IDENTIFIER> <GT>
    *       | CharacterList()
    *       | <LPAREN> ComplexRegularExpressionChoices() <RPAREN> [ <PLUS> | <STAR> | <HOOK> ]
    */
   public void visit(ComplexRegularExpressionUnit n) {
      if ( n.f0.which != 0 )
         regExpr = "";
      else
         regExpr = ((NodeToken)n.f0.choice).tokenImage;
   }
}
