The configuration of Klistret on Weblogic (10+) is described below and the majority is applicable to other containers like Websphere or JBoss.

# Introduction #
[Weblogic](http://www.oracle.com/technetwork/middleware/weblogic/overview/index.html) is a popular Java container that can even be run without cost (i.e. no support) in production.  Klistret has been tested mainly in this container and all of the default settings are geared to Weblogic on Unix/Linux.

# Required Software #
All that is necessary is a Klistret web download (i.e. a web bundle that supplies the graphic interface and the REST services), database drivers, and a database installation (either [DB2](http://www-01.ibm.com/software/data/db2/express), [Oracle](http://www.oracle.com/technetwork/database/express-edition/overview/index.html), or [Postgresql](http://www.postgresql.org)).  None of the Klistret packages (archives) ships with JDBC drivers.  Instead these should be pulled from the database installation.  Future editions of Klistret will provide an open source alternative.

## Database ##
TO-DO: Link or instruction on database setup

# Configuration #

## Unix (Linux) ##
Directly under the home directory create a domain map called **dm1**.
```
  mkdir dm1
```

This is where the entire setup will be housed.  Create the following sub-directories:
```
  mkdir applications .downloads logs managedpid properties scripts settings weblogic-root wlst
```

The **applicatiosn** map will contain all of the links to downloaded code (helpful for managing versions) which is unpacked under **.downloads**.  Log files are steered to the **logs** map as defined by the LOG4J configuration under the **properties** directory where the other property files are kept.  Start and stop scripts lye in **scripts** and their corresponding settings files next door under **settings**.  Everything pertaining Weblogic's configuration is directed to the **weblogic-root** catalog while how to configuration with the WLST interface under **wlst**.  The last catalog is **managedpid** where the PIDs for the administration and application servers reside.

The first thing to do is unpack the .downloads:
```
  cd $HOME/dm1/.downloads
  mkdir klistret.cmdb.ext-simple-0.1
  gunzip klistret.cmdb.ext-simple-0.1.tar.gz
  cd klistret.cmdb.ext-simple-0.1
  tar xvf ../klistret.cmdb.ext-simple-0.1.tar
  rm ../klistret.cmdb.ext-simple-0.1.tar
```

If you are using DB2 then create a JDBC directory och link in the archives from the DB2 installation:
```
  cd $HOME/dm1/.downloads
  mkdir db2-jdbc-drivers
  cd db2-jdbc-drivers
  ln -s <DB2 HOME>/sqllib/java/db2jcc_license_cu.jar .
  ln -s <DB2 HOME>/sqllib/java/db2jcc.jar .
```

Then create links from the **applications** map:
```
  cd $HOME/dm1/applications
  ln -s ../.downloads/klistret.cmdb.ext-simple-0.1 cmdb
  ln -s ../.downloads/db2-jdbc-drivers db2-jdbc-drivers
```

Remember that these instructions are for Unix/Linux so at the moment the only scripts or settings are for non-Windows platforms.  Example scripts are shipped with the web package and are tagged with _edit_ text:
```
  cd $HOME/dm1/scripts
  cp ../applications/cmdb/example/weblogic/*.ksh .
  cd $HOME/dm1/settings
  cp ../applications/cmdb/example/weblogic/*.env .
```

The start/stop scripts should work right out of the box.  They are run as follows:
```
  $HOME/dm1/scripts/startAdminserver.ksh $HOME/dm1/settings/weblogic.env
  $HOME/dm1/scripts/startAppserver.ksh $HOME/dm1/settings/appserver1.env
```

Inside each settings file (**.env**) are _-- edit --_ comments flagging that something needs to be edited.  For example, in the _appserver1.env_ file one must edit the application server name plus port number.  The last thing to do before starting the servers is to gather the property files and edit a couple of settings.
```
  cd $HOME/dm1/properties
  cp ../applications/cmdb/propertes/* .
```

The major property file is the **CMDB.properties**.  When running in a container a DataSource is used so make sure to edit the **cmdb.datasource.name** property.  As well the Hibernate dialect corresponding to what underlying database is active.  Even the Hibernate transaction manager has to be defined (right now) dependent on the container.

The LOG4J file (**log4j.properties**) is configured by default for development or test and needs several changes in a real container.  Replace the log file handle with the following:
```
log4j.appender.logfile=org.apache.log4j.RollingFileAppender
log4j.appender.logfile.File=<HOME DIRECTORY>/dm1/logs/cmdb.log
log4j.appender.logfile.MaxFileSize=10000KB
log4j.appender.logfile.MaxBackupIndex=1
log4j.appender.logfile.layout=org.apache.log4j.PatternLayout
log4j.appender.logfile.layout.ConversionPattern=%d{ABSOLUTE} %5p (%c:%L) - %m%n
```

Replace all of the **debug** settings to **info**.

The main spring configuration file is **Spring.cfg.xml** to which the **Blueprint.cfg.xml** has to be added as an import:
```
  ...
   <import resource="DAO.cfg.xml" />
   <import resource="Manager.cfg.xml" />
   <import resource="Service.cfg.xml" />
   <import resource="Integration.cfg.xml" />
   <import resource="Blueprint.cfg.xml" />
```

There is nothing within these Spring configuration files that is Weblogic specific or container specific.

# Container #

## Weblogic ##
TODO: Describe the basic resources and show a sample WLST script.

# Test #