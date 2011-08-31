#!/bin/sh


##############################################################
# Control shell settings
##############################################################
if [ $# -ne 1 ]; then
        echo 1>&2 "Usage: ${0} <absolute path to appserver.env>"
        exit 1
fi


##############################################################
# Load general weblogic settings
##############################################################
if [ ! -f "${1}" ]; then
        echo
        echo "Application general settings file [${1}] not found in current directory"
        return 1
fi
#. "${1}" > /dev/null 2>&1
. "${1}"


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
ADMIN_URL \
SERVER_NAME \
SERVER_PORT \
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
  MPID=`cat $MPIDSRV |tail -1 |awk '{ print $1 }'`
  if [ `ps -ef |grep -v grep |grep $MPID |wc -l` -eq 0 ]
  then
     echo "File $MPIDSRV exists however no process `cat $MPIDSRV` exists. Exit"
     echo "Control or remove file"
     exit 1
  else
     echo "File $MPIDSRV exists and executing process `cat $MPIDSRV`. Exit"
     ps -fp $MPID
     exit 0
  fi
fi

if [ `uname -s` = "SunOS" ]
then
   NScmd="netstat -a -P tcp"
else
   NScmd="netstat -a"
fi

if [ `$NScmd| egrep "${PMID}.$SERVER_PORT.*LISTEN"| grep -v TIME_WAIT | wc -l` -gt 0 ]
then
   echo "Warning! Application port ${SERVER_PORT} is active.  Exit"
   exit 1
fi


##############################################################
# Main
##############################################################
nohup $JAVA_HOME/bin/java -cp ${CLASSPATH} ${JAVA_VM} ${MEM_ARGS} ${JAVA_OPTIONS}  \
	-Dweblogic.Domain=${DOMAIN_NAME} \
	-Dweblogic.Name=${SERVER_NAME} \
	-Dweblogic.management.username=${WLS_USER} \
	-Dweblogic.management.password=${WLS_PW} \
	-Dweblogic.management.server=${ADMIN_URL} \
	-Dweblogic.ProductionModeEnabled=${PRODUCTION_MODE} \
	-Djava.security.policy="${WL_HOME}/server/lib/weblogic.policy" \
	-Dweblogic.RootDirectory=${WEBLOGIC_ROOT} \
	weblogic.Server > "${DOMAIN_HOME}/logs/${SERVER_NAME}.start.log" 2>&1 &
echo $! > ${MPIDSRV}
