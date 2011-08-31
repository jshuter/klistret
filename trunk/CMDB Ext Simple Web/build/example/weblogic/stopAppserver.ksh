#!/bin/sh


##############################################################
# Control shell settings  
##############################################################
if [ $# -ne 1 ]; then
	echo 1>&2 "Usage: ${0} <absolute path to weblogic.env>"
	exit 1
fi


##############################################################
# Load general weblogic settings
##############################################################
if [ ! -f "${1}" ]; then
        echo
        echo "Weblogic general settings file [${1}] not found in current directory"
        return 1
fi
. "${1}" > /dev/null 2>&1


##############################################################
# Control shell settings  
##############################################################
settings="\
WL_HOME \
DOMAIN_HOME \
JAVA_HOME \
JAVA_VM \
DOMAIN_NAME \
WLS_USER \
WLS_PW \
PRODUCTION_MODE \
PMID \
WEBLOGIC_ROOT \
SERVER_NAME \
SERVER_PORT \
ADMIN_URL \
"

for variable in $settings
do
  eval value='$'$variable
  if [ "z$value" = "z" ]
  then
    echo "Variable $variable is not set."
    exit 1
  fi
done


##############################################################
# Managed PID
##############################################################
MPIDDIR="${DOMAIN_HOME}/managedpid"
MPIDSRV="${MPIDDIR}/${SERVER_NAME}.${SERVER_PORT}"

if [ -f $MPIDSRV ]
then
   rm $MPIDSRV
fi


##############################################################
# Main
##############################################################
connect="connect(username='${WLS_USER}',password='${WLS_PW}',url='${ADMIN_URL}')\n"
shutdown="shutdown('${SERVER_NAME}','Server',force='true',block='true')\n"

echo "${connect} ${shutdown} exit()" | nohup $JAVA_HOME/bin/java -cp ${WEBLOGIC_CLASSPATH} ${JAVA_VM} ${MEM_ARGS} ${JAVA_OPTIONS}  \
	weblogic.WLST > "$DOMAIN_HOME/logs/${SERVER_NAME}.stop.log" 2>&1 &

