Installation steps for DB2 9.5 on Ubuntu 8 (8.10 intrepid)

# Introduction #

Using DB2 9.5 as the default relational/xml motor enabling xpath/xquery/flowr statements within SQL thereby providing search capabilities.


# Details #

[Instructions](http://www.ibm.com/developerworks/wikis/display/im/Ubuntu+8.04+-+DB2+9.5) detail the major steps for running DB2 9.5 on Ubuntu intrepid.  A repository [package](http://www.ubuntu.com/partners/ibm/db2) exists for Ubuntu feisty (7.10).  Please note that this installation was done on an Ubuntu installation off a USB stick.

## Requirements ##
Verified with the [apt-get](http://www.debian.org/doc/manuals/apt-howto) utility that the following packages exist: ibaio1, ksh and libstdc++5.

Ignored that the referenced page is intended for Ubuntu 8.04.

Created a new user to perform the installation (for example, expc).  Note that that user needs the DISPLAY set and a copy of the .Xauthority file.

## Steps ##
[Page](http://itsrohith.com/db2.aspx) and as well the [forum post](http://www.ibm.com/developerworks/forums/thread.jspa?threadID=187514&tstart=0) outline the installation process.

The forum post was followed since the installation is done outside of a GUI, the DB2 software is located under /opt (centrally), and the installer can determine which use is the instance owner.