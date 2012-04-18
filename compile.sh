#/bin/bash

TARGET_DIR=../bin
ORIG_GRAM_FILE=EDU/purdue/jtb/jtbgram.jj
NEW_GRAM_FILE=jtb.out.jj
JAVACC_PATH=~/Downloads/javacc-5.0/bin/
JAVACC_FILES="JTBParser*.java Token*.java ParseException.java JavaCharStream.java"

PATH=$PATH:$JAVACC_PATH

if [  -e $TARGET_DIR  -a  ! -d $TARGET_DIR  ]; then
	echo "target dir already exists; exiting"
	exit
fi

if [ ! -d $TARGET_DIR ]; then
	mkdir $TARGET_DIR
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
check javac
check java
check javacc
check perl

#compile JTB
echo "Compiling JTB..."
javac -d $TARGET_DIR EDU/iitm/jtb/threaded/JTBParallel.java

cd $TARGET_DIR 

#call JTB on the original grammar file
echo; echo; echo
echo "Running JTB..."
java EDU/purdue/jtb/JTB $ORIG_GRAM_FILE

# run javacc 
echo; echo; echo
echo "Removing files $JAVACC_FILES"
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
echo "Compiling the parser"
javac Main.java
if [ ! 0 -eq $? ];then
	exit
fi
echo "Running the parser"
cd $TARGET_DIR; java Main Main.java
