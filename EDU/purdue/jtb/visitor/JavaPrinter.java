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

//
// Modified for use with JTB bootstrap.
//
// Pretty printer for the Java grammar.
// Author: Kevin Tao, taokr@cs
//
// (reminders for myself):
// - spc.spc should be printed after every "\n" or println().
// - println() should not be the last thing printed in a visit method.
// - always copy this file from the JTB source and remove the comments around
//   Spacing at the bottom.  Remove the import for misc.Spacing.
//

package EDU.purdue.jtb.visitor;

import EDU.purdue.jtb.misc.Spacing;
import EDU.purdue.jtb.syntaxtree.*;
import java.util.*;
import java.io.*;

public class JavaPrinter extends DepthFirstVisitor {
   protected PrintWriter out;
   protected Spacing spc = new Spacing(3);   // see below for class Spacing

   public JavaPrinter()       { out = new PrintWriter(System.out, true); }
   public JavaPrinter(Writer o)        { out = new PrintWriter(o, true); }
   public JavaPrinter(OutputStream o)  { out = new PrintWriter(o, true); }

   public void flushWriter()     { out.flush(); }

   //
   // A few convenience methods
   //
   public void visit(NodeList n, String sep) {
      for ( Enumeration e = n.elements(); e.hasMoreElements(); ) {
         ((Node)e.nextElement()).accept(this);
         if ( e.hasMoreElements() ) out.print(sep);
      }
   }

   public void visit(NodeListOptional n, String sep) {
      if ( n.present() )
         for ( Enumeration e = n.elements(); e.hasMoreElements(); ) {
            ((Node)e.nextElement()).accept(this);
            if ( e.hasMoreElements() ) out.print(sep);
         }
   }

   public void visit(NodeOptional n, String sep) {
      if ( n.present() ) {
         n.node.accept(this);
         out.print(sep);
      }
   }

   //
   // For convenience, trusts that the node passed to it is a NodeSequence.
   // (of course, will throw an exception if it isn't).
   //
   public void visit(Node n1, String sep) {
      NodeSequence n = (NodeSequence)n1;
      for ( Enumeration e = n.elements(); e.hasMoreElements(); ) {
         ((Node)e.nextElement()).accept(this);
         if ( e.hasMoreElements() ) out.print(sep);
      }
   }

   public void visit(NodeToken n, String sep) {
      out.print(n.tokenImage + sep);
   }

   //
   // Auto class visitors--probably don't need to be overridden.
   //
   public void visit(NodeToken n) { out.print(n.tokenImage); }

   //
   // User-generated visitor methods below
   //

   //
   // f0 -> [ PackageDeclaration() ]
   // f1 -> ( ImportDeclaration() )*
   // f2 -> ( TypeDeclaration() )*
   // f3 -> < EOF >
   //
   public void visit(CompilationUnit n) {
      out.print(spc.spc);
      if ( n.f0.present() ) {
         visit(n.f0, "\n\n");
         out.print(spc.spc);
      }
      if ( n.f1.present() ) {
         visit(n.f1, "\n" + spc.spc);
         out.print("\n\n" + spc.spc);
      }
      if ( n.f2.present() )
         visit(n.f2, "\n" + spc.spc);

      out.println();
   }

   //
   // f0 -> "package"
   // f1 -> Name()
   // f2 -> ";"
   //
   public void visit(PackageDeclaration n) {
      out.print(n.f0 + " ");
      n.f1.accept(this);
      out.print(n.f2);
   }

   //
   // f0 -> "import"
   // f1 -> Name()
   // f2 -> [ "." "*" ]
   // f3 -> ";"
   //
   // uses the toString() method of the NodeTokens to print manually
   // (rather than visiting the tokens).
   //
   public void visit(ImportDeclaration n) {
      out.print(n.f0 + " ");
      n.f1.accept(this);
      n.f2.accept(this);
      out.print(n.f3);
   }

   //
   // f0 -> ClassDeclaration()
   //       | InterfaceDeclaration()
   //       | ";"
   //
   public void visit(TypeDeclaration n) {
      n.f0.accept(this);
   }

   //
   // f0 -> ( "abstract"| "final" | "public"  )*
   // f1 -> UnmodifiedClassDeclaration()
   //
   public void visit(ClassDeclaration n) {
      if ( n.f0.present() ) {
         visit(n.f0, " ");
         out.print(" ");
      }
      n.f1.accept(this);
   }

   //
   // f0 -> "class"
   // f1 -> < IDENTIFIER >
   // f2 -> [ "extends" Name() ]
   // f3 -> [ "implements" NameList() ]
   // f4 -> ClassBody()
   //
   // continued from ClassDeclaration
   //
   public void visit(UnmodifiedClassDeclaration n) {
      out.print(n.f0 + " " + n.f1 + " ");
      if ( n.f2.present() ) { visit(n.f2.node, " "); out.print(" ");}
      if ( n.f3.present() ) { visit(n.f3.node, " "); }
      out.println();
      out.print(spc.spc);
      n.f4.accept(this);
   }

   //
   // f0 -> "{"
   // f1 -> ( ClassBodyDeclaration() )*
   // f2 -> "}"
   //
   public void visit(ClassBody n) {
      out.println(n.f0);
      if ( n.f1.present() ) {
         spc.updateSpc(+1);
         out.print(spc.spc);
         visit(n.f1, "\n" + spc.spc);
         out.println();
         spc.updateSpc(-1);
      }
      out.print(spc.spc + n.f2);
   }

   //
   // f0 -> ("static"|"abstract"|"final"|"public"|"protected"|"private")*
   // f1 -> UnmodifiedClassDeclaration()
   //
   public void visit(NestedClassDeclaration n) {
      visit(n.f0, " ");
      out.print(" ");
      n.f1.accept(this);
   }

   //
   // f0 -> Initializer()
   //       | NestedClassDeclaration()
   //       | NestedInterfaceDeclaration()
   //       | ConstructorDeclaration()
   //       | MethodDeclaration()
   //       | FieldDeclaration()
   //
   public void visit(ClassBodyDeclaration n) {
      n.f0.accept(this);
   }

   //
   // f0 -> ( "public"| "protected" | "private" | "static" | "abstract" | "final" | "native" | "synchronized"  )*
   // f1 -> ResultType()
   // f2 -> < IDENTIFIER >
   // f3 -> "("
   //
   // For lookahead only, so don't need to do this
   public void visit(MethodDeclarationLookahead n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
   }

   //
   // f0 -> ( "abstract"| "public"  )*
   // f1 -> UnmodifiedInterfaceDeclaration()
   //
   public void visit(InterfaceDeclaration n) {
      visit(n.f0, " ");
      out.print(" ");
      n.f1.accept(this);
   }

   //
   // f0 -> ("static"|"abstract"|"final"|"public"|"protected"|"private")*
   // f1 -> UnmodifiedInterfaceDeclaration()
   //
   public void visit(NestedInterfaceDeclaration n) {
      visit(n.f0, " ");
      out.print(" ");
      n.f1.accept(this);
   }

   //
   // f0 -> "interface"
   // f1 -> < IDENTIFIER >
   // f2 -> [ "extends" NameList() ]
   // f3 -> "{"
   // f4 -> ( InterfaceMemberDeclaration() )*
   // f5 -> "}"
   //
   public void visit(UnmodifiedInterfaceDeclaration n) {
      out.print(n.f0 + " " + n.f1 + " ");
      if ( n.f2.present() ) { visit(n.f2.node, " "); }
      out.println("\n" + spc.spc + n.f3);
      spc.updateSpc(+1);
      out.print(spc.spc);
      visit(n.f4, "\n" + spc.spc);
      spc.updateSpc(-1);
      out.print("\n" + spc.spc + n.f5);
   }

   //
   // f0 -> NestedClassDeclaration()
   //       | NestedInterfaceDeclaration()
   //       | MethodDeclaration()
   //       | FieldDeclaration()
   //
   public void visit(InterfaceMemberDeclaration n) {
      n.f0.accept(this);
   }

   //
   // f0 -> ("public"|"protected"|"private"|"static"|"final"|"transient"|"volatile")*
   // f1 -> Type()
   // f2 -> VariableDeclarator()
   // f3 -> ( "," VariableDeclarator() )*
   // f4 -> ";"
   //
   public void visit(FieldDeclaration n) {
      if ( n.f0.present() ) { visit(n.f0, " "); out.print(" "); }
      n.f1.accept(this);
      out.print(" ");
      n.f2.accept(this);
      n.f3.accept(this);
      out.print(n.f4);
   }

   //
   // f0 -> VariableDeclaratorId()
   // f1 -> [ "=" VariableInitializer() ]
   //
   public void visit(VariableDeclarator n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         visit(n.f1.node, " ");
      }
   }

   //
   // f0 -> < IDENTIFIER >
   // f1 -> ( "[" "]" )*
   //
   public void visit(VariableDeclaratorId n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> ArrayInitializer()
   //       | Expression()
   //
   public void visit(VariableInitializer n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "{"
   // f1 -> [ VariableInitializer() ( "," VariableInitializer() )* ]
   // f2 -> [ "," ]
   // f3 -> "}"
   //
   public void visit(ArrayInitializer n) {
      out.print(n.f0 + " ");
      n.f1.accept(this);
      n.f2.accept(this);
      out.print(" " + n.f3);
   }

   //
   // f0 -> ( "public"| "protected" | "private" | "static" | "abstract" | "final" | "native" | "synchronized"  )*
   // f1 -> ResultType()
   // f2 -> MethodDeclarator()
   // f3 -> [ "throws" NameList() ]
   // f4 -> ( Block()| ";"  )
   //
   public void visit(MethodDeclaration n) {
      if ( n.f0.present() ) {
         visit(n.f0, " ");
         out.print(" ");
      }
      n.f1.accept(this);
      out.print(" ");
      n.f2.accept(this);
      if ( n.f3.present() ) {
         out.print(" ");
         visit(n.f3.node, " ");
      }
      out.println();
      out.print(spc.spc);
      n.f4.accept(this);
   }

   //
   // f0 -> < IDENTIFIER >
   // f1 -> FormalParameters()
   // f2 -> ( "[" "]" )*
   //
   public void visit(MethodDeclarator n) {
      out.print(n.f0);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   //
   // f0 -> "("
   // f1 -> [ FormalParameter() ( "," FormalParameter() )* ]
   // f2 -> ")"
   //
   public void visit(FormalParameters n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   //
   // f0 -> [ "final" ]
   // f1 -> Type()
   // f2 -> VariableDeclaratorId()
   //
   public void visit(FormalParameter n) {
      if ( n.f0.present() ) out.print(n.f0.node + " ");
      n.f1.accept(this);
      out.print(" ");
      n.f2.accept(this);
   }

   //
   // f0 -> [ "public"| "protected" | "private"  ]
   // f1 -> < IDENTIFIER >
   // f2 -> FormalParameters()
   // f3 -> [ "throws" NameList() ]
   // f4 -> "{"
   // f5 -> [ ExplicitConstructorInvocation() ]
   // f6 -> ( BlockStatement() )*
   // f7 -> "}"
   //
   public void visit(ConstructorDeclaration n) {
      if ( n.f0.present() ) {
         n.f0.accept(this);
         out.print(" ");
      }
      out.print(n.f1);
      n.f2.accept(this);
      if ( n.f3.present() ) { visit(n.f3.node, " "); }
      out.print("\n" + spc.spc + n.f4 + "\n");
      spc.updateSpc(+1);
      out.print(spc.spc);
      if ( n.f5.present() ) {
         n.f5.accept(this);
         out.print("\n" + spc.spc);
      }
      visit(n.f6, "\n" + spc.spc);
      spc.updateSpc(-1);
      out.print("\n" + spc.spc);
      n.f7.accept(this);
   }

   //
   // f0 -> "this" Arguments() ";"
   //       | [ PrimaryExpression() "." ] "super" Arguments() ";"
   //
   public void visit(ExplicitConstructorInvocation n) {
      //
      // This code may not be appropriate if "which" is removed from NodeChoice
      //
      if ( n.f0.which == 0 ) {
         n.f0.accept(this);
      }
      else {
         NodeSequence seq = (NodeSequence)n.f0.choice;
         seq.elementAt(0).accept(this);
         out.print(" ");
         seq.elementAt(1).accept(this);
         seq.elementAt(2).accept(this);
         seq.elementAt(3).accept(this);
      }
   }

   //
   // f0 -> [ "static" ]
   // f1 -> Block()
   //
   public void visit(Initializer n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> ( PrimitiveType()| Name()  )
   // f1 -> ( "[" "]" )*
   //
   public void visit(Type n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> "boolean"
   //       | "char"
   //       | "byte"
   //       | "short"
   //       | "int"
   //       | "long"
   //       | "float"
   //       | "double"
   //
   public void visit(PrimitiveType n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "public void"
   //       | Type()
   //
   public void visit(ResultType n) {
      n.f0.accept(this);
   }

   //
   // f0 -> < IDENTIFIER >
   // f1 -> ( "." < IDENTIFIER >  )*
   //
   public void visit(Name n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> Name()
   // f1 -> ( "," Name() )*
   //
   // Add spaces?
   //
   public void visit(NameList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> Assignment()
   //       | ConditionalExpression()
   //
   public void visit(Expression n) {
      n.f0.accept(this);
   }

   //
   // f0 -> PrimaryExpression()
   // f1 -> AssignmentOperator()
   // f2 -> Expression()
   //
   public void visit(Assignment n) {
      n.f0.accept(this);
      out.print(" ");
      n.f1.accept(this);
      out.print(" ");
      n.f2.accept(this);
   }

   //
   // f0 -> "="
   //       | "*="
   //       | "/="
   //       | "%="
   //       | "+="
   //       | "-="
   //       | "<<="
   //       | ">>="
   //       | ">>>="
   //       | "&="
   //       | "^="
   //       | "|="
   //
   public void visit(AssignmentOperator n) {
      n.f0.accept(this);
   }

   //
   // f0 -> ConditionalOrExpression()
   // f1 -> [ "?" Expression() ":" ConditionalExpression() ]
   //
   public void visit(ConditionalExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         visit(n.f1.node, " ");
      }
   }

   //
   // f0 -> ConditionalAndExpression()
   // f1 -> ( "||" ConditionalAndExpression() )*
   //
   public void visit(ConditionalOrExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> InclusiveOrExpression()
   // f1 -> ( "&&" InclusiveOrExpression() )*
   //
   public void visit(ConditionalAndExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> ExclusiveOrExpression()
   // f1 -> ( "|" ExclusiveOrExpression() )*
   //
   public void visit(InclusiveOrExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> AndExpression()
   // f1 -> ( "^" AndExpression() )*
   //
   public void visivisit(ExclusiveOrExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> EqualityExpression()
   // f1 -> ( "&" EqualityExpression() )*
   //
   public void visit(AndExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> InstanceOfExpression()
   // f1 -> ( ( "=="| "!="  ) InstanceOfExpression() )*
   //
   public void visit(EqualityExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> RelationalExpression()
   // f1 -> [ "instanceof" Type() ]
   //
   public void visit(InstanceOfExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         visit(n.f1.node, " ");
      }
   }

   //
   // f0 -> ShiftExpression()
   // f1 -> ( ( "<"| ">" | "<=" | ">="  ) ShiftExpression() )*
   //
   public void visit(RelationalExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> AdditiveExpression()
   // f1 -> ( ( "<<"| ">>" | ">>>"  ) AdditiveExpression() )*
   //
   public void visit(ShiftExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> MultiplicativeExpression()
   // f1 -> ( ( "+"| "-"  ) MultiplicativeExpression() )*
   //
   public void visit(AdditiveExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> UnaryExpression()
   // f1 -> ( ( "*"| "/" | "%"  ) UnaryExpression() )*
   //
   public void visit(MultiplicativeExpression n) {
      n.f0.accept(this);
      if ( n.f1.present() ) {
         out.print(" ");
         for ( Enumeration e = n.f1.elements(); e.hasMoreElements(); )
            visit((Node)e.nextElement(), " ");
      }
   }

   //
   // f0 -> ( "+"| "-"  ) UnaryExpression()
   //       | PreIncrementExpression()
   //       | PreDecrementExpression()
   //       | UnaryExpressionNotPlusMinus()
   //
   public void visit(UnaryExpression n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "++"
   // f1 -> PrimaryExpression()
   //
   public void visit(PreIncrementExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> "--"
   // f1 -> PrimaryExpression()
   //
   public void visit(PreDecrementExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> ( "~"| "!"  ) UnaryExpression()
   //       | CastExpression()
   //       | PostfixExpression()
   //
   public void visit(UnaryExpressionNotPlusMinus n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "(" PrimitiveType()
   //       | "(" Name() "[" "]"
   //       | "(" Name() ")" ( "~" | "!" | "(" | < IDENTIFIER >  | "this" | "super" | "new" | Literal()  )
   //
   public void visit(CastLookahead n) {
      n.f0.accept(this);
   }

   //
   // f0 -> PrimaryExpression()
   // f1 -> [ "++"| "--"  ]
   //
   public void visit(PostfixExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> "(" Type() ")" UnaryExpression()
   //       | "(" Type() ")" UnaryExpressionNotPlusMinus()
   //
   public void visit(CastExpression n) {
      n.f0.accept(this);
   }

   //
   // f0 -> PrimaryPrefix()
   // f1 -> ( PrimarySuffix() )*
   //
   public void visit(PrimaryExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> Literal()
   //       | Name()
   //       | "this"
   //       | "super" "." < IDENTIFIER >
   //       | "(" Expression() ")"
   //       | AllocationExpression()
   //
   public void visit(PrimaryPrefix n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "." "this"
   //       | "." "class"
   //       | "." AllocationExpression()
   //       | "[" Expression() "]"
   //       | "." < IDENTIFIER >
   //       | Arguments()
   //
   public void visit(PrimarySuffix n) {
      n.f0.accept(this);
   }

   //
   // f0 -> < INTEGER_LITERAL >
   //       | < FLOATING_POINT_LITERAL >
   //       | < CHARACTER_LITERAL >
   //       | < STRING_LITERAL >
   //       | BooleanLiteral()
   //       | NullLiteral()
   //
   public void visit(Literal n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "true"
   //       | "false"
   //
   public void visit(BooleanLiteral n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "null"
   //
   public void visit(NullLiteral n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "("
   // f1 -> [ ArgumentList() ]
   // f2 -> ")"
   //
   public void visit(Arguments n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   //
   // f0 -> Expression()
   // f1 -> ( "," Expression() )*
   //
   public void visit(ArgumentList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> "new" PrimitiveType() ArrayDimensions() [ ArrayInitializer() ]
   //       | "new" Name() ( ArrayDimensions() [ ArrayInitializer() ]| Arguments() [ ClassBody() ]  )
   //
   public void visit(AllocationExpression n) {
      NodeSequence seq = (NodeSequence)n.f0.choice;

      if ( n.f0.which == 0 ) {
         seq.elementAt(0).accept(this);
         out.print(" ");
         seq.elementAt(1).accept(this);
         seq.elementAt(2).accept(this);
         if ( ((NodeOptional)seq.elementAt(3)).present() ) {
            out.print(" ");
            seq.elementAt(3).accept(this);
         }
      }
      else {   // needs a little tweaking
         seq.elementAt(0).accept(this);
         out.print(" ");
         seq.elementAt(1).accept(this);
         seq.elementAt(2).accept(this);
      }
   }

   //
   // f0 -> ( "[" Expression() "]" )+
   // f1 -> ( "[" "]" )*
   //
   public void visit(ArrayDimensions n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> LabeledStatement()
   //       | Block()
   //       | EmptyStatement()
   //       | StatementExpression() ";"
   //       | SwitchStatement()
   //       | IfStatement()
   //       | WhileStatement()
   //       | DoStatement()
   //       | ForStatement()
   //       | BreakStatement()
   //       | ContinueStatement()
   //       | ReturnStatement()
   //       | ThrowStatement()
   //       | SynchronizedStatement()
   //       | TryStatement()
   //
   public void visit(Statement n) {
      n.f0.accept(this);
   }

   //
   // f0 -> < IDENTIFIER >
   // f1 -> ":"
   // f2 -> Statement()
   //
   public void visit(LabeledStatement n) {
      out.print(n.f0 + " " + n.f1 + " ");
      n.f2.accept(this);
   }

   //
   // f0 -> "{"
   // f1 -> ( BlockStatement() )*
   // f2 -> "}"
   //
   public void visit(Block n) {
      out.println(n.f0);
      if ( n.f1.present() ) {
         spc.updateSpc(+1);
         out.print(spc.spc);
         visit(n.f1, "\n" + spc.spc);
         out.println();
         spc.updateSpc(-1);
      }
      out.print(spc.spc + n.f2);
   }

   //
   // f0 -> LocalVariableDeclaration() ";"
   //       | Statement()
   //       | UnmodifiedClassDeclaration()
   //
   public void visit(BlockStatement n) {
      n.f0.accept(this);
   }

   //
   // f0 -> [ "final" ]
   // f1 -> Type()
   // f2 -> VariableDeclarator()
   // f3 -> ( "," VariableDeclarator() )*
   //
   public void visit(LocalVariableDeclaration n) {
      visit(n.f0, " ");
      n.f1.accept(this);
      out.print(" ");
      n.f2.accept(this);
      n.f3.accept(this);
   }

   //
   // f0 -> ";"
   //
   public void visit(EmptyStatement n) {
      n.f0.accept(this);
   }

   //
   // f0 -> PreIncrementExpression()
   //       | PreDecrementExpression()
   //       | Assignment()
   //       | PostfixExpression()
   //
   public void visit(StatementExpression n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "switch"
   // f1 -> "("
   // f2 -> Expression()
   // f3 -> ")"
   // f4 -> "{"
   // f5 -> ( SwitchLabel() ( BlockStatement() )* )*
   // f6 -> "}"
   //
   public void visit(SwitchStatement n) {
      out.print(n.f0 + " " + n.f1);
      n.f2.accept(this);
      out.println(n.f3);
      out.println(spc.spc + n.f4);
      spc.updateSpc(+1);
      for ( Enumeration e = n.f5.elements(); e.hasMoreElements(); ) {
         NodeSequence seq = (NodeSequence)e.nextElement();
         out.print(spc.spc);
         seq.elementAt(0).accept(this);
         spc.updateSpc(+1);
         if ( ((NodeListOptional)seq.elementAt(1)).present() ) {
            if ( ((NodeListOptional)seq.elementAt(1)).size() == 1 )
               out.print(" ");
            else {
               out.println();
               out.print(spc.spc);
            }
            visit((NodeListOptional)seq.elementAt(1), "\n" +
                                 spc.spc);
         }
         out.println();
         spc.updateSpc(-1);
      }
      spc.updateSpc(-1);
      out.println(spc.spc + n.f6);
   }

   //
   // f0 -> "case" Expression() ":"
   //       | "default" ":"
   //
   public void visit(SwitchLabel n) {
      visit(n.f0.choice, " ");
   }

   //
   // f0 -> "if"
   // f1 -> "("
   // f2 -> Expression()
   // f3 -> ")"
   // f4 -> Statement()
   // f5 -> [ "else" Statement() ]
   //
   public void visit(IfStatement n) {
      out.print(n.f0 + " " + n.f1 + " ");
      n.f2.accept(this);
      out.println(" " + n.f3);

      if ( n.f4.f0.which != 1 )     // only indent if it's not a Block
         spc.updateSpc(+1);

      out.print(spc.spc);
      n.f4.accept(this);
      
      if ( n.f4.f0.which != 1 )
         spc.updateSpc(-1);

      if ( n.f5.present() ) {
         out.println();
         out.print(spc.spc);

         if (((Statement)((NodeSequence)n.f5.node).elementAt(1)).f0.which != 1)
            spc.updateSpc(+1);

         visit(n.f5.node, "\n" + spc.spc);

         if (((Statement)((NodeSequence)n.f5.node).elementAt(1)).f0.which != 1)
            spc.updateSpc(-1);
      }
   }

   //
   // f0 -> "while"
   // f1 -> "("
   // f2 -> Expression()
   // f3 -> ")"
   // f4 -> Statement()
   //
   public void visit(WhileStatement n) {
      out.print(n.f0 + " " + n.f1 + " ");
      n.f2.accept(this);
      out.println(" " + n.f3);

      if ( n.f4.f0.which != 1 )
         spc.updateSpc(+1);

      out.print(spc.spc);
      n.f4.accept(this);

      if ( n.f4.f0.which != 1 )
         spc.updateSpc(-1);
   }

   //
   // f0 -> "do"
   // f1 -> Statement()
   // f2 -> "while"
   // f3 -> "("
   // f4 -> Expression()
   // f5 -> ")"
   // f6 -> ";"
   //
   public void visit(DoStatement n) {
      out.println(n.f0);

      if ( n.f1.f0.which != 1 )
         spc.updateSpc(+1);

      out.print(spc.spc);
      n.f1.accept(this);

      if ( n.f1.f0.which != 1 )
         spc.updateSpc(-1);

      out.println();
      out.print(spc.spc + n.f2 + " " + n.f3 + " ");
      n.f4.accept(this);
      out.print(" " + n.f5 + n.f6);
   }

   //
   // f0 -> "for"
   // f1 -> "("
   // f2 -> [ ForInit() ]
   // f3 -> ";"
   // f4 -> [ Expression() ]
   // f5 -> ";"
   // f6 -> [ ForUpdate() ]
   // f7 -> ")"
   // f8 -> Statement()
   //
   public void visit(ForStatement n) {
      out.print(n.f0 + " " + n.f1 + " ");
      n.f2.accept(this);
      out.print(n.f3 + " ");
      n.f4.accept(this);
      out.print(n.f5 + " ");
      n.f6.accept(this);
      out.println(" " + n.f7);

      if ( n.f8.f0.which != 1 )
         spc.updateSpc(+1);

      out.print(spc.spc);
      n.f8.accept(this);

      if ( n.f8.f0.which != 1 )
         spc.updateSpc(-1);
   }

   //
   // f0 -> LocalVariableDeclaration()
   //       | StatementExpressionList()
   //
   public void visit(ForInit n) {
      n.f0.accept(this);
   }

   //
   // f0 -> StatementExpression()
   // f1 -> ( "," StatementExpression() )*
   //
   public void visit(StatementExpressionList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   //
   // f0 -> StatementExpressionList()
   //
   public void visit(ForUpdate n) {
      n.f0.accept(this);
   }

   //
   // f0 -> "break"
   // f1 -> [ < IDENTIFIER >  ]
   // f2 -> ";"
   //
   public void visit(BreakStatement n) {
      n.f0.accept(this);
      if ( n.f1.present() ) out.print(" " + n.f1.node);
      n.f2.accept(this);
   }

   //
   // f0 -> "continue"
   // f1 -> [ < IDENTIFIER >  ]
   // f2 -> ";"
   //
   public void visit(ContinueStatement n) {
      n.f0.accept(this);
      if ( n.f1.present() ) out.print(" " + n.f1.node);
      n.f2.accept(this);
   }

   //
   // f0 -> "return"
   // f1 -> [ Expression() ]
   // f2 -> ";"
   //
   public void visit(ReturnStatement n) {
      out.print(n.f0);
      if ( n.f1.present() ) {
         out.print(" ");
         n.f1.accept(this);
      }
      n.f2.accept(this);
   }

   //
   // f0 -> "throw"
   // f1 -> Expression()
   // f2 -> ";"
   //
   public void visit(ThrowStatement n) {
      out.print(n.f0 + " ");
      n.f1.accept(this);
      n.f2.accept(this);
   }

   //
   // f0 -> "synchronized"
   // f1 -> "("
   // f2 -> Expression()
   // f3 -> ")"
   // f4 -> Block()
   //
   public void visit(SynchronizedStatement n) {
      out.print(n.f0 + " " + n.f1 + " ");
      n.f2.accept(this);
      out.println(" " + n.f3);
      
      spc.updateSpc(+1);
      out.print(spc.spc);
      n.f4.accept(this);
      spc.updateSpc(-1);
   }

   //
   // f0 -> "try"
   // f1 -> Block()
   // f2 -> ( "catch" "(" FormalParameter() ")" Block() )*
   // f3 -> [ "finally" Block() ]
   //
   public void visit(TryStatement n) {
      out.println(n.f0);
      out.print(spc.spc);
      n.f1.accept(this);
      for ( Enumeration e = n.f2.elements(); e.hasMoreElements(); ) {
         NodeSequence seq = (NodeSequence)e.nextElement();
         out.println();
         out.print(spc.spc);
         seq.elementAt(0).accept(this);
         out.print(" ");
         seq.elementAt(1).accept(this);
         seq.elementAt(2).accept(this);
         seq.elementAt(3).accept(this);
         out.println();
         out.print(spc.spc);
         seq.elementAt(4).accept(this);
      }

      if ( n.f3.present() ) {
         NodeSequence seq = (NodeSequence)n.f3.node;
         out.println();
         seq.elementAt(0).accept(this);
         out.println();
         out.print(spc.spc);
         seq.elementAt(1).accept(this);
      }
   }
}

//
// For indentation
//
/*
class Spacing {
   final int INDENT_AMT;

   String spc = "";
   int indentLevel = 0;

   public Spacing(int indentAmt) {
      INDENT_AMT = indentAmt;
   }

   public String toString() {
      return spc;
   }

   public void updateSpc(int numIndentLvls) {
      indentLevel += numIndentLvls;

      if ( numIndentLvls < 0 )
         spc = spc.substring(-1 * numIndentLvls * INDENT_AMT);
      else if ( numIndentLvls > 0 ) {
         StringBuffer buf = new StringBuffer(spc);

         for ( int i = 0; i < numIndentLvls * INDENT_AMT; ++i )
            buf.append(" ");

         spc = buf.toString();
      }
   }
}
*/
