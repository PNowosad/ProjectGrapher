CDA_HOME=`dirname $0`
export CDA_HOME 

LCP=$CDA_HOME/config:$CDA_HOME/lib/*:$CDA_HOME/lib/ext/*
export LCP

# For remote control support by DARC add the following to JVM_OPTS
# -Dcom.sun.management.jmxremote.port=4242 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

# For debugging add the following to JVM_OPTS
# -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000

set JVM_OPTS=-Xmx512M
export JVM_OPTS

execCDA() 
{
	export JHOME
	$JHOME/bin/java $JVM_OPTS -cp $LCP org.pf.tools.cda.CDATool $*
}

error1() 
{
 	echo ====================================================
 	echo Error: JRE_HOME and JAVA_HOME not found !                                         
	echo Set the JRE_HOME or the JAVA_HOME path variable !
	echo ====================================================
}

if [ $JRE_HOME ] ; then 
 		JHOME=$JRE_HOME
		execCDA $*
	elif [ $JAVA_HOME ] ; then
  	JHOME=$JAVA_HOME
		execCDA $*
else
		error1
fi
