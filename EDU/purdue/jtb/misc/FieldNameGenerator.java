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

package EDU.purdue.jtb.misc;

import java.util.Hashtable;

/**
 * Class FieldNameGenerator generates the names of the fields of node classes
 * depending on whether the "-f" parameter for descriptive field names has
 * been used or not.
 *
 * By default, field will be named "fX" where X ascends from 0 to the number of
 * children - 1.
 *
 * If the "-f" parameter is used, the names will be based on the classes of
 * the children.  For example, a child of class "WhileStatement" will be
 * called "whileStatementX" (note the lowercase first letter), where X is
 * either nothing if this is the first WhileStatement in this production or
 * 1 through the number of children - 1 for any additional children of the
 * same type.
 */
public class FieldNameGenerator {
   private int fieldNum = 0;

   // nameTable is used for the "-f" option--Descriptive field names.
   // Key = field names in use in current production
   // Value = int value of the last suffix used
   private Hashtable nameTable;

   public FieldNameGenerator() {
      if ( Globals.descriptiveFieldNames )
         nameTable = new Hashtable();
   }

   public void resetFieldNum() {
      fieldNum = 0;

      if ( Globals.descriptiveFieldNames )
         nameTable.clear();
   }

   public String curFieldName(String fieldName) {
      if ( !Globals.descriptiveFieldNames )
         return "f" + String.valueOf(fieldNum++);
      else {
         String prefix = varNameForClass(fieldName);
         Integer suffix = (Integer)nameTable.get(prefix);

         if ( suffix == null ) {
            suffix = new Integer(0);
            nameTable.put(prefix, suffix);
            return prefix;
         }
         else {
            suffix = new Integer(suffix.intValue() + 1);
            nameTable.put(prefix, suffix);
            return prefix + suffix.toString();
         }
      }
   }

   /**
    * Returns a variable name for the name of the given class.
    */
   public String varNameForClass(String className) {
      StringBuffer buf = new StringBuffer(
         String.valueOf(Character.toLowerCase(className.charAt(0))));
      buf.append(className.substring(1, className.length()));
      return buf.toString();
   }

   public String getNameForMod(String mod) {
      if ( mod.equals("+") )        return Globals.listName;
      else if ( mod.equals("*") )   return Globals.listOptName;
      else if ( mod.equals("?") )   return Globals.optionalName;
      else {
         Errors.hardErr("Illegal EBNF modifier in " +
                        "ExpansionUnit: mod = " + mod);
         return "";
      }
   }
}
