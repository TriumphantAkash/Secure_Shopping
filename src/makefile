# You can use this makefile to compile the java files
# and clean the .class files

JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        Broker.java \
        Client.java \
        ECOM1.java \
	ECOM2.java \
	ECOM3.java \
	Hash.java \
	Writer.java \
	DESEncryption.java \

default: classes

all: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class