JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

lab2: classes
	
CLASSES = \
        PersistentCSMACD.java \
        NonpersistentCSMACD.java 

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
