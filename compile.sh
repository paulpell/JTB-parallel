#/bin/bash

TARGET_DIR=build

ORIG_GRAM_FILE=EDU/purdue/jtb/jtbgram.jj
#ORIG_GRAM_FILE=testGrammars/Java.jj
NEW_GRAM_FILE=jtb.out.jj

JAVACC_PATH=~/Downloads/javacc-5.0/bin/
JAVACC_FILES="JTBParser*.java Token*.java ParseException.java JavaCharStream.java"

JTB_MAIN="EDU/iitm/jtb/threaded/JTBParallel"

PATH=$PATH:$JAVACC_PATH

if [  -e $TARGET_DIR  -a  ! -d $TARGET_DIR  ]; then
	echo "target dir already exists; exiting"
	exit
fi

if [ ! -d $TARGET_DIR ]; then
	mkdir $TARGET_DIR
fi

if [ ! -e $TARGET_DIR/Main.java ]; then
	cp Main.java $TARGET_DIR
fi

function check {
	which $1 2> /dev/null
	res=$?
	if [ ! 0 -eq $res ]; then
		echo "Could not find $1 in \$PATH, is it installed?"
		echo "Exiting"
		exit 0
	fi
}
# checks whether the programs can be found
echo; echo
echo "Checking whether the needed programs are there.."
check javac
check java
check javacc
check perl

#compile JTB
echo; echo; echo
echo "Compiling JTB..."
echo "skipping, eclipse does the job"
#javac -d $TARGET_DIR $JTB_MAIN.java

if [ ! -e $TARGET_DIR/$ORIG_GRAM_FILE ]; then
	cp $ORIG_GRAM_FILE $TARGET_DIR/$ORIG_GRAM_FILE
fi

cd $TARGET_DIR 

#call JTB on the original grammar file
echo; echo; echo
echo "Running JTB..."
java $JTB_MAIN $ORIG_GRAM_FILE

# run javacc 
echo; echo; echo
echo "Removing javacc files: $JAVACC_FILES"
rm $JAVACC_FILES
echo "Running javacc..."
javacc $NEW_GRAM_FILE

# we want to remove the 'package blah.blah' line in all the .java
# file created by javacc
for i in *.java
do
	cat $i | perl -pe 's/package/\/\//' > "$i"2
done
for i in *.java2
do
	mv $i $(echo $i | perl -pe 's/2//')
done


echo; echo; echo
echo "Do you have the right Main.java?"
echo "You can just change it, and call 'javac Main.java'"
echo "Compiling the parser (Main.java)"
javac Main.java 
if [ ! 0 -eq $? ];then
	exit
fi
echo "Running the parser"
java Main Main.java
