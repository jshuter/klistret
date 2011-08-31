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
ADMIN_NAME \
ADMIN_PORT \
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
MPIDADM="${MPIDDIR}/${ADMIN_NAME}.${ADMIN_PORT}"

if [ -f $MPIDADM ]
then
  MPID=`cat $MPIDADM |tail -1 |awk '{ print $1 }'`
  if [ `ps -ef |grep -v grep |grep $MPID |wc -l` -eq 0 ]
  then
     echo "File $MPIDADM exists however no process `cat $MPIDADM` exists. Exit"
     echo "Control or remove file"
     exit 1
  else
     echo "File $MPIDADM exists and executing process `cat $MPIDADM`. Exit"
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

if [ `$NScmd| egrep "${PMID}.$ADMIN_PORT.*LISTEN"| grep -v TIME_WAIT | wc -l` -gt 0 ]
then
   echo "Warning! Admin port ${ADMIN_PORT} is active. Exiting"
   exit 1
fi


##############################################################
# Main
##############################################################
nohup $JAVA_HOME/bin/java -cp ${WEBLOGIC_CLASSPATH} ${JAVA_VM} ${MEM_ARGS} ${JAVA_OPTIONS}  \
	-Dweblogic.Name=${ADMIN_NAME} \
	-Dweblogic.management.username=${WLS_USER} \
	-Dweblogic.management.password=${WLS_PW} \
	-Dweblogic.ProductionModeEnabled=${PRODUCTION_MODE} \
	-Djava.security.policy="${WL_HOME}/server/lib/weblogic.policy" \
	-Dweblogic.RootDirectory=${WEBLOGIC_ROOT} \
	weblogic.Server > "$DOMAIN_HOME/logs/${ADMIN_NAME}.start.log" 2>&1 &
echo $! > ${MPIDADM}
