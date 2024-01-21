# Dear Prof. Chilingaryan,
#
#	My Linux subsystem decided not to work today, so I do not know whether my makefile works currently or not.
#	So, if some commands will not work, please check the folder locations and run commands again.
#
#	Thank you very much in advance.
#
#	Sincerly,
# 	Aleksandr Hayrapetyan

BIN_DIR = ./bin

all:
	java -jar $(BIN_DIR)/Coco.jar -frames Compiler.atg
	javac *.java #"-Xlint:unchecked"

clean:
	rm *.class

coco-clean: clean
	rm Parser.java Scanner.java
	rm Parser.java.old Scanner.java.old	


test:
	java Compiler test.pas
	echo $?
