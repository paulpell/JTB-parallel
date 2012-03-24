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
 * Indiana by Kevin Tao, Wanjun Wang and Jens Palsberg.  No charge may 
 * be made for copies, derivations, or distributions of this material
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

import java.io.*;

public class CopyCat {

	private boolean classMissing = false;

	/*
	 * The manager of the duplication of gj packages.
	 *
	 */
	public void copy() throws FileNotFoundException, SecurityException {
		try {
			File rootPath = getInputDir(System.getProperty("install.root"));
		
			File gjPath = getInputDir(rootPath,"gj");
			File outPath = getOutputDir("gj");

			File utilPath = getInputDir(gjPath,"util");
			File outUtilPath = getOutputDir(outPath,"util");

			copy("Dictionary.class",utilPath,outUtilPath);
			copy("Enumeration.class",utilPath,outUtilPath);
			copy("Hashtable.class",utilPath,outUtilPath);
			copy("HashtableEntry.class",utilPath,outUtilPath);
			copy("KeyEnumerator.class",utilPath,outUtilPath);
			copy("Stack.class",utilPath,outUtilPath);
			copy("ValueEnumerator.class",utilPath,outUtilPath);
			copy("Vector.class",utilPath,outUtilPath);
			copy("VectorEnumerator.class",utilPath,outUtilPath);
		
			File langPath = getInputDir(gjPath,"lang");
			File outLangPath = getOutputDir(outPath,"lang");

			File reflectPath = getInputDir(langPath,"reflect");
			File outReflectPath = getOutputDir(outLangPath,"reflect");

			copy("Array.class",reflectPath,outReflectPath);
		}
		catch (NullPointerException e) {
			Errors.softErr("\"install.root\" property is not set " +
						   "to indicate the root directory of JTB.");
			throw new FileNotFoundException();
		}

		if ( classMissing )
			throw new FileNotFoundException();

	}
	
	/*
	 * Duplicate the class file from sourcePath directory to 
	 * destinationPath directory, according to the given className.
	 *
	 */
	public void copy(String className, File sourcePath, File destinationPath) {
		try {
			InputStream in = new FileInputStream(new File(sourcePath,className));
			OutputStream out = new FileOutputStream(new File(destinationPath,className));
			for (int i=in.read(); i>=0; i=in.read())
				out.write(i);
			out.close(); in.close();
		}
		catch (IOException e) {
			classMissing = true;
			Errors.softErr(className + " not found in directory " + sourcePath.getPath());
		}
	}

	/*
	 * Generate File object for source directory. "name" argument indicates
	 * the path name.
	 *
	 */
	public File getInputDir(String name) throws FileNotFoundException {
		File ret = new File(name);
		if ( !ret.exists() ) {
			Errors.softErr(name + " is not the root directory of JTB.");
			throw new FileNotFoundException();
		}
		return ret;
	}

	/* 
	 * Generate File object for source subdirectory. "parent" is the
	 * parent directory and "name" is the subdircetory name.
	 *
	 */
	public File getInputDir(File parent, String name) throws FileNotFoundException {
		File ret = new File(parent,name);
		if ( !ret.exists() ) {
			Errors.softErr(name + " subdirectory not found in " +
						   parent.getPath() + " of JTB.");
			throw new FileNotFoundException();
		}
		return ret;
	}

	/*
	 * Generate File object for destination directory. "name" argument
	 * indicates the path name.
	 *
	 */
	public File getOutputDir(String name) throws FileNotFoundException {
		File ret = new File(name);
		if ( !ret.exists() )
			ret.mkdir();
		else if ( !ret.isDirectory() ) {
			Errors.softErr(name + " exists but is not a directory.");
			throw new FileNotFoundException();
		}
		return ret;
	}

	/* 
	 * Generate File object for destination subdirectory. "parent" is
	 * the parent directory and "name" is the subdircetory name.
	 *
	 */
	public File getOutputDir(File parent, String name) throws FileNotFoundException {
		File ret = new File(parent,name);
		if ( !ret.exists() )
			ret.mkdir();
		else if ( !ret.isDirectory() ) {
			Errors.softErr(name + " exists but is not a subdirectory in " +
						   parent.getPath() + ".");
			throw new FileNotFoundException();
		}
		return ret;
	}
}