Description
==========

Parallelization of Java Tree Builder, see [their homepage](http://compilers.cs.ucla.edu/jtb/). JTB is a tool to build a syntax tree when parsing. In order to achieve that, it reads a grammar file (javaCC syntax), and creates another one (to feed javaCC), as well as all the classes needed to represent the syntax tree.


Requirements
------------

JTB is of course built for java-written parsers, you will need the JDK. javaCC is needed too.
In order to simplify the final compilation, perl is used to remove some lines from the created classes.

Usage
-----

A compile.sh script is provided, which 
