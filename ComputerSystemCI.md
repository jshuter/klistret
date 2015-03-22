A computer system is a concrete system entity.

## Description ##
Computer systems are derived from [managed systems](ManagedSystemCI.md) and model operative computers.  The fundamental idea with computers is that they can serve several purposes.  It is not uncommen that servers get overloaded.  A computer can be a printer, file system, and a firewall or other system at the same time.  Dedicated capabilities make a computer system concrete rather than abstract.  A computer system is not inherited.  It is a hub of associations to other more specific systems.

The open DMTF model subclasses the computer system into virtual computer systems, clusters and unitary (ie exhaustive) computer systems.  [BMC](http://www.javasystemsolutions.com/documentation/thirdparty/cdm/) splits the computer system into virtual, mainframe and file systems.  The concept of subclassing an aggreation point that is generically tagged by functionality available through hosted services is messy.  Klistret tries to ingore the notion of computer system until a good use case is presented.

### Specific ###
This section contains characteristics that make the entity specific.

#### Capability ####
The capability of a computer system is a wrapper for functionality offered by the computer system with weight or order in regard to secondary functions.

## Usage ##
This CI is not convention in Klistet.  Describing applications are the main focus with systems leaving computers system more to system management.

## Relations ##

## Blueprint ##
Compute systems fall into the [complex](ComplexBlueprint.md) category.