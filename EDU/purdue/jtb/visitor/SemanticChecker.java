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
import EDU.purdue.jtb.misc.Globals;
import java.util.*;

/**
 * Semantic checking phase for JTB checks for the following conditions:
 *   - When productions have a return value other than void since JTB
 *     automatically alters the return value of all productions in the
 *     annotated grammar.
 *   - For blocks of Java code within productions since to our knowledge
 *     they are generally unnecessary in JTB grammars.
 *   - When any productions have a name reserved by an automatically
 *     generated JTB class (e.g. Node, NodeList, etc.)
 *   - Extraneous parentheses in a production
 *   - JavaCode productions must be handled specially, as stated in the
 *     JTB Release Notes page.
 */
public class SemanticChecker extends DepthFirstVisitor {
   String prod;            // name of the current production

   /**
    * f0 -> JavaCCOptions()
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
      n.f10.accept(this);           // only visit production subtree
   }

   /**
    * f0 -> [ <OPTIONS_TK> <LBRACE> ( OptionBinding() )* <RBRACE> ]
    */
   public void visit(JavaCCOptions n) {
   }

   /**
    * f0 -> ( <IDENTIFIER> | <LOOKAHEAD_TK> | <IGNORE_CASE_TK> | <STATIC> )
    * f1 -> <ASSIGN>
    * f2 -> ( <INTEGER_LITERAL> | BooleanLiteral() | <STRING_LITERAL> )
    * f3 -> <SEMICOLON>
    */
   public void visit(OptionBinding n) {
   }

   /**
    * f0 -> JavaCodeProduction()
    *       | RegularExprProduction()
    *       | BNFProduction()
    *       | TokenManagerDecls()
    */
   public void visit(Production n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> <JAVACODE_TK>
    * f1 -> ResultType()
    * f2 -> <IDENTIFIER>
    * f3 -> FormalParameters()
    * f4 -> Block()
    */
   public void visit(JavaCodeProduction n) {
      Errors.warning("Javacode block must be specially handled.  See JTB " +
         "Release Notes web page.", n.f0.beginLine);
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
      prod = n.f1.tokenImage;

      if ( n.f0.f0.which != 0 )           // return type isn't void
         Errors.warning("Non-void return type in " + prod + "().",
            n.f1.beginLine);

      if ( prod.equals(Globals.nodeName) ||
           prod.equals(Globals.listInterfaceName) ||
           prod.equals(Globals.listName) || prod.equals(Globals.listOptName) ||
           prod.equals(Globals.optionalName) ||
           prod.equals(Globals.sequenceName) ||
           prod.equals(Globals.tokenName) || prod.equals(Globals.choiceName) )
         Errors.softErr("Production \"" + prod + "\" has the same name as a " +
            "JTB-generated class.", n.f1.beginLine);

      n.f8.accept(this);
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
   }

   /**
    * f0 -> <TOKEN_MGR_DECLS_TK>
    * f1 -> <COLON>
    * f2 -> ClassBodyDeclaration()
    */
   public void visit(TokenManagerDecls n) {
   }

   /**
    * f0 -> <LT> <STAR> <GT>
    *       | <LT> <IDENTIFIER> ( <COMMA> <IDENTIFIER> )* <GT>
    */
   public void visit(LexicalStateList n) {
   }

   /**
    * f0 -> <TOKEN_TK>
    *       | <SPECIAL_TOKEN_TK>
    *       | <SKIP_TK>
    *       | <MORE_TK>
    */
   public void visit(RegExprKind n) {
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
    * f0 -> Expansion()
    * f1 -> ( <BIT_OR> Expansion() )*
    */
   public void visit(ExpansionChoices n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> ( ExpansionUnit() )*
    */
   public void visit(Expansion n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> LocalLookahead()
    *       | Block()
    *       | <LPAREN> ExpansionChoices() <RPAREN> [ <PLUS> | <STAR> | <HOOK> ]
    *       | <LBRACKET> ExpansionChoices() <RBRACKET>
    *       | [ PrimaryExpression() <ASSIGN> ] ExpansionUnitTerm()
    */
   public void visit(ExpansionUnit n) {
      if ( n.f0.which == 1 )           // unit is a block
         Errors.warning("Block of Java code in " + prod + "().",
            ((Block)n.f0.choice).f0.beginLine);

      if ( n.f0.which == 2 ) {         // parentheses present
         NodeSequence seq = (NodeSequence)n.f0.choice;
         ExpansionChoices choice = (ExpansionChoices)seq.elementAt(1);
         NodeOptional mod = (NodeOptional)seq.elementAt(3);

         if ( !mod.present() && !choice.f1.present() )
            Errors.warning("Extra parentheses in " + prod + "().",
               ((NodeToken)((NodeSequence)n.f0.choice).elementAt(0)).beginLine);
      }

      n.f0.accept(this);
   }

   /**
    * f0 -> RegularExpression()
    *       | <IDENTIFIER> Arguments()
    */
   public void visit(ExpansionUnitTerm n) {
   }

   /**
    * f0 -> <LOOKAHEAD_TK>
    * f1 -> <LPAREN>
    * f2 -> [ <INTEGER_LITERAL> ]
    * f3 -> [ <COMMA> ]
    * f4 -> ExpansionChoices()
    * f5 -> [ <COMMA> ]
    * f6 -> [ <LBRACE> Expression() <RBRACE> ]
    * f7 -> <RPAREN>
    */
   public void visit(LocalLookahead n) {
   }

   /**
    * f0 -> <STRING_LITERAL>
    *       | <LT> [ [ <POUND> ] <IDENTIFIER> <COLON> ] ComplexRegularExpressionChoices() <GT>
    *       | <LT> <IDENTIFIER> <GT>
    *       | <LT> <EOF_TK> <GT>
    */
   public void visit(RegularExpression n) {
   }

   /**
    * f0 -> ComplexRegularExpression()
    * f1 -> ( <BIT_OR> ComplexRegularExpression() )*
    */
   public void visit(ComplexRegularExpressionChoices n) {
   }

   /**
    * f0 -> ( ComplexRegularExpressionUnit() )*
    */
   public void visit(ComplexRegularExpression n) {
   }

   /**
    * f0 -> <STRING_LITERAL>
    *       | <LT> <IDENTIFIER> <GT>
    *       | CharacterList()
    *       | <LPAREN> ComplexRegularExpressionChoices() <RPAREN> [ <PLUS> | <STAR> | <HOOK> ]
    */
   public void visit(ComplexRegularExpressionUnit n) {
   }

   /**
    * f0 -> [ <TILDE> ]
    * f1 -> <LBRACKET>
    * f2 -> [ CharacterDescriptor() ( <COMMA> CharacterDescriptor() )* ]
    * f3 -> <RBRACKET>
    */
   public void visit(CharacterList n) {
   }

   /**
    * f0 -> <STRING_LITERAL>
    * f1 -> [ <MINUS> <STRING_LITERAL> ]
    */
   public void visit(CharacterDescriptor n) {
   }

}
