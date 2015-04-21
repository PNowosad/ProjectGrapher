@echo off

if "%JRE_HOME%" == "" goto USE_JDK
set EXEC_JAVA=%JRE_HOME%\bin\java
goto SET_JVM_OPTS

:USE_JDK
if "%JAVA_HOME%" == "" goto ERROR1
set EXEC_JAVA=%JAVA_HOME%\bin\java
goto SET_JVM_OPTS

:SET_JVM_OPTS
set JVM_OPTS=-Xmx512M

:CHECK_REMOTE
if "%1" == "remote" goto DEF_JMX 
if "%2" == "remote" goto DEF_JMX 
goto CHECK_DEBUG

:DEF_JMX
set JVM_OPTS=%JVM_OPTS% -Dcom.sun.management.jmxremote.port=4242 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
goto CHECK_DEBUG

:CHECK_DEBUG
if "%1" == "debug" goto DEF_DEBUG 
if "%2" == "debug" goto DEF_DEBUG 
goto DEF_CLASSPATH

:DEF_DEBUG
set JVM_OPTS=%JVM_OPTS% -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
goto DEF_CLASSPATH

:DEF_CLASSPATH
set CDA_HOME=%~dp0
set LCP=%CDA_HOME%config;%CDA_HOME%lib\*;%CDA_HOME%lib\ext\*
goto EXECUTE

:EXECUTE
"%EXEC_JAVA%" %JVM_OPTS% -cp %LCP% org.pf.tools.cda.CDATool %*
goto END

:ERROR1
@echo Cannot find Java. Either JRE_HOME or JAVA_HOME must be defined.
goto END

:END
