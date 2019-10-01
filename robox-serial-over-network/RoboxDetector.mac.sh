DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAVA="$DIR/../../AutoMaker/java/bin/java"
PROBE="$DIR/robox-serial-over-network-0.1-SNAPSHOT.jar"


if [ $# -ne 1 -o "$1" = "" ]; then
	echo "ERROR - Wrong number of arguments"
else
	comport=`ioreg -p IOService -n $1 -rl | grep IOCalloutDevice | sed 's/[^\/]*\([^\"]*\).*/\1/'`

	if [ "$comport" = "" ]; then
#        	echo "serialproxy:192.168.1.161/9990"
#        	echo "NOT_CONNECTED"
#            $JAVA -jar $PROBE serialproxy:192.168.1.161/9990 serialproxy:127.0.0.1/9990
            $JAVA -jar $PROBE serialproxy:127.0.0.1/9990
	else
			echo >> /Users/daniel/executed "SOMETHING"
			echo >> /Users/daniel/executed $comport
        	echo $comport
	fi
fi
