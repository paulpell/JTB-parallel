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
import EDU.purdue.jtb.misc.*;
import java.util.*;
import java.io.*;

/**
 * Class ClassGenerator creates a vector of ClassInfo objects describing
 * every class to be generated.
 *
 * Programming notes:
 *    - do not continue down the tree once a new field has been added to
 *      curClass--we only worry about top-level expansions 
 */
public class ClassGenerator extends DepthFirstVisitor {
   private boolean printToken = false;

   // Used to generate default constructors if a token has a constant regexpr
   private Hashtable tokenTable;

   // Variables to store the list of classes generated
   private ClassInfo curClass;
   private Vector classList = new Vector();

   // Used to generate field names (descriptive or not, depending on -f option)
   private FieldNameGenerator nameGen = new FieldNameGenerator();

   public Vector getClassList()  { return classList; }

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
      TokenTableBuilder builder = new TokenTableBuilder();

      n.accept(builder);
      tokenTable = builder.getTokenTable();

      n.f10.accept(this);
   }

   //
   // f0 -> < JAVACODE_TK > 
   // f1 -> ResultType()
   // f2 -> < IDENTIFIER > 
   // f3 -> FormalParameters()
   // f4 -> Block()
   //
   public void visit(JavaCodeProduction n) {
      // Don't visit
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
      nameGen.resetFieldNum();
      printToken = true;
      curClass = new ClassInfo(n.f8, n.f1.toString());
      classList.addElement(curClass);

      n.f8.accept(this);
      printToken = false;
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
      // Don't visit--don't want to generate NodeTokens inside RegularExpression
      // if it's visited from a RegularExpressionProduction
   }

   //
   // f0 -> < TOKEN_MGR_DECLS_TK > 
   // f1 -> < COLON > 
   // f2 -> Block()
   //
   public void visit(TokenManagerDecls n) {
      // Don't visit
   }

   //
   // f0 -> Expansion()
   // f1 -> ( < BIT_OR >  Expansion() )*
   //
   public void visit(ExpansionChoices n) {
      if ( !n.f1.present() )
         n.f0.accept(this);
      else
         curClass.addField(Globals.choiceName,
            nameGen.curFieldName(Globals.choiceName));
   }

   //
   // f0 -> ( ExpansionUnit() )*
   //
   public void visit(Expansion n) {
      n.f0.accept(this);
   }

   //
   // f0 -> LocalLookahead()
   //       | Block() 
   //       | < LPAREN >  ExpansionChoices() < RPAREN >  [ < PLUS > | < STAR >  | < HOOK >   ] 
   //       | < LBRACKET >  ExpansionChoices() < RBRACKET >  
   //       | [ PrimaryExpression() < ASSIGN >  ] ExpansionUnitTerm() 
   //
   public void visit(ExpansionUnit n) {
      switch ( n.f0.which ) {
         case 0: return;      // Lookahead
         case 1: return;      // Java block
         case 2:              // Parenthesized expansion
            NodeSequence seq = (NodeSequence)n.f0.choice;
            NodeOptional ebnfMod = (NodeOptional)seq.elementAt(3);

            if ( ebnfMod.present() ) {
               NodeChoice modChoice = (NodeChoice)ebnfMod.node;
               String mod = ((NodeToken)modChoice.choice).tokenImage;
               String name = getNameForMod(mod);
               curClass.addField(name, nameGen.curFieldName(name));
            }
            else {
               ExpansionChoices ec = (ExpansionChoices)seq.elementAt(1);

               if ( ec.f1.present() )
                  curClass.addField(Globals.choiceName,
                     nameGen.curFieldName(Globals.choiceName));
               else
                  curClass.addField(Globals.sequenceName,
                     nameGen.curFieldName(Globals.sequenceName));
            }

            break;
         case 3:              // Optional expansion
            curClass.addField(Globals.optionalName,
               nameGen.curFieldName(Globals.optionalName));
            break;
         case 4:              // Normal production
            n.f0.accept(this);
            break;
         default:
            Errors.hardErr("n.f0.which = " + String.valueOf(n.f0.which));
            break;
      }
   }

   private String getNameForMod(String mod) {
      if ( mod.equals("+") )        return Globals.listName;
      else if ( mod.equals("*") )   return Globals.listOptName;
      else if ( mod.equals("?") )   return Globals.optionalName;
      else {
         Errors.hardErr("Illegal EBNF modifier in " +
                        "ExpansionUnit: mod = " + mod);
         return "";
      }
   }

   //
   // f0 -> RegularExpression()
   //       | < IDENTIFIER >  Arguments() 
   //
   public void visit(ExpansionUnitTerm n) {
      switch ( n.f0.which ) {
         case 0 :
            n.f0.accept(this);
            break;
         case 1 :
            NodeSequence seq = (NodeSequence)n.f0.choice;
            String ident = ((NodeToken)seq.elementAt(0)).tokenImage;
            curClass.addField(ident, nameGen.curFieldName(ident));
            break;
         default:
            Errors.hardErr("n.f0.which = " + String.valueOf(n.f0.which));
      }
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
      // Don't visit...ignore lookaheads
   }

   //
   // f0 -> < STRING_LITERAL > 
   //       | < LT >  [ [ < POUND >  ] < IDENTIFIER >  < COLON >  ] ComplexRegularExpressionChoices() < GT >  
   //       | < LT >  < IDENTIFIER >  < GT >  
   //       | < LT >  < EOF_TK >  < GT >  
   //
   public void visit(RegularExpression n) {
      String regExpr = "";
      String initialValue = null;
      boolean isEOF = false;

      if ( !printToken )
         return;

      switch ( n.f0.which ) {
         case 0 :
            regExpr = ((NodeToken)n.f0.choice).tokenImage;
            break;
         case 1 :
            regExpr = "";
            break;
         case 2 :
            NodeSequence seq = (NodeSequence)n.f0.choice;
            NodeToken ident = (NodeToken)seq.elementAt(1);
            regExpr = (String)tokenTable.get(ident.tokenImage);

            if ( regExpr == null ) {
               Errors.softErr("Undefined token \"" + ident + "\".",
                  ident.beginLine);
               regExpr = "";
            }

            break;
         case 3 :
            regExpr = "";
            isEOF = true;
            break;
         default :
            Errors.hardErr("Unreachable code executed!");
      }

      if ( isEOF )
         initialValue = "new " + Globals.tokenName + "(\"\")";
      else if ( regExpr != "" )
         initialValue = "new " + Globals.tokenName + "(" + regExpr + ")";

      curClass.addField(Globals.tokenName,
         nameGen.curFieldName(Globals.tokenName), initialValue);
   }
}
