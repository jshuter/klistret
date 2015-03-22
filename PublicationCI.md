If Software CIs are logical then Publication CIs are their physical counterparts.

## Description ##
A Publication CI is the delivery mechanism for software.  It is similar to the DMTF Software Element:
> _a collection of one or more files and associated details that are individually deployed and managed on a particular platform_
Publications map to Ivy publications and have an obligatory type to tell how the package is used by the organization.  Typing is paramount for the receiver of publications to understand either how to deploy or integrate a deliverable.  But typing shouldn't be confused with extensions.  Every publication ought to have an extension standing for how the deliverable is bundled (i.e. if it is zipped or compressed or plain text like HTML).

Publications in regard to software can be anything.  Normal types are Java on a particular platform, source code, documentation, configuration files and so forth.  Typically software has a type for one or more binaries that can be run on, for example, Java or on C libraries or within a Perl interpreter plus a type for source code.  Nothing hinders one from making a publication for configuration settings and calling it _Spring configuration_.  The extension on such a Spring configuration file if singular could be _cfg.xml_ or multiple files as _zip_.

The important thing with publications is the typing.  Typing assumes an audience and that receiver has to known instantaneously what to do with a publication.  Earmarking something as _jar_ (extension will be the same) automatically flags the publication as a Java file full with classes.  Unfortunately, the type _jar_ doesn't reveal if the deployment works with Java 1.3 or only Java 1.6 and higher.  That is left to the end user.  The universal types like _jar_ may not suffice depending on how content is managed within an organization.  Internal projects have a predefined run time container (ex. Weblogic, JBoss, etc.).  Sometimes it is easier to zip together all of a projects jar, ear, and configuration files into a structure fitting the container (ex. [OSGi bundles](http://en.wikipedia.org/wiki/OSGi)).  In these cases, it might be more beneficial to type the publication as _Weblogic Application_ if the audience is an administrator setting up a Weblogic server.

## Characteristics ##
The Publication CI is a direct descendant of the [Software CI](SoftwareCI.md).

| **Property** | **Description** | **Required** | **Unbounded** | **Key** | **Context** |
|:-------------|:----------------|:-------------|:--------------|:--------|:------------|
| Type | Typing suggests how a publication ought to be received and integrated.  The property (element) is part of the _http://www.klistret.com/cmdb/ci/element/component_ namespace. | Yes | No | No | Yes |
| Extension | A suffix explicitly stating the format of the publication as a physical document.  The property (element) is part of the _http://www.klistret.com/cmdb/ci/element/component_ namespace. | Yes | No | No | No |
  * **Context** earmarks properties as having a corresponding contextual CI.

## Usage ##
Publications seldom exist without a relation to one or more software components.  The configure function (or indicator) in Ivy is overloaded temporarily in the Usage property (part of the Base CI).

The purpose of a publication is to be a physical representation of software.  The intention for a publication is to received (pulled) by others and easily understood.  Bundling software is the responsibility of either the development or a CM ([configuration manager](http://en.wikipedia.org/wiki/Configuration_management)).  They decide how many publications and the associated extensions.  Before setting a type the intended receivers should be consulted to hammer out a label that is clear and consistent describing how the organization uses each publication.

## Relations ##

### General Basic ###
| **Type** | **CI** | **Direction** | **Description** |
|:---------|:-------|:--------------|:----------------|
| Dependency | Software | Destination | Publications are always the target of a Software CI dependency. |

## Blueprint ##
Publications inherit the identifications of the Software CI.