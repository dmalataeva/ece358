JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

default: classes
	
CLASSES = \
        PersistentCSMACD.java \
        NonpersistentCSMACD.java 

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
