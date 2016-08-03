MEMORYBASED = memorybased/*/*.java
#DATABASE = memorybased.database/*.java
#MBMEMREADER = memorybased/memreader/*.java
#MEMORYBASED = $(DATABASE) $(MBMEMREADER) memorybased/rectree/Centroid.java memorybased/rectree/ClusterCollection.java memorybased/rectree/RecTree2.java
MODELBASED = modelbased/*.java
ALGORITHMS = algorithms/$(MEMORYBASED) algorithms/$(MODELBASED) algorithms/modelbased/*/*.java

DB = db/*.java

MEMREADER = memreader/*.java

RECOMMENDER = recommender/*.java

UTILITIES = utilities/*.java

RMSE = rmse/*.java

ALL = $(ALGORITHMS) $(DB) $(MEMREADER) $(RECOMMENDER) $(UTILITIES) $(RMSE)

CPATH = /recommender/colt.jar:.

all : $(ALL)
	javac -classpath $(CPATH) $(ALL)

algorithmS : $(ALGORITHMS)
	javac -classpath $(CPATH) $(ALGORITHMS)

dB : $(DB)
	javac -classpath $(CPATH) $(DB)

memreadeR : $(MEMREADER)
	javac -classpath $(CPATH) $(MEMREADER)

recommendeR : $(RECOMMENDER) 
	javac -classpath $(CPATH) $(RECOMMENDER)

utilitieS : $(UTILITIES)
	javac -classpath $(CPATH) $(UTILITIES)

rmsE : $(RMSE)
	javac -classptah $(CPATH) $(RMSE)

clean :
	rm */*.class */*/*.class */*/*/*.class
