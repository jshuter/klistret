The Core component is in layman's terms a wrapper of services around the CI XML hierarchy.

## Overview ##
A CMDB is a collection of configuration elements and relationship between elements.  John Singer's [article](http://www.tdan.com/view-articles/6904) does a good job of explaining the CMDB model.  Distilled John explains the CIs are the building blocks of a CMDB and the hardest thing is categorizing CIs into logical groups.  The Core component in Klistret does not place any hard coded restrictions on the CI hierarchy only pluggable identification and relationship controls.  The Core essentially provides [CRUD](http://en.wikipedia.org/wiki/Create,_read,_update_and_delete) services and the capability to search with XPath expressions.  Outside of CRUD services and a search motor the Core is intended to work in any Java container (even without thanks to great mock frameworks latent to Spring and Resteasy), deployable with minimal configuration plus integrate to [CMDB federations](http://www.informit.com/articles/article.aspx?p=1329141&seqNum=4).

## Road map ##
This section will be updated quarterly with information about what is happing with Klistret the coming 6 months and a year from now.

### 6 months ###
The Core has undergone performance testing and the first round of functional testing.  Database calls in general and deeper queries against non-indexed XML data are under 100ms.  Marshalling POJOs through [Hibernate](http://www.hibernate.org) via [Spring](http://www.springsource.org) services and the [RestEasy](http://www.jboss.org/resteasy) gateway adds another 100ms on average.  A client can except turn around times between 200ms to 300ms depending on network latency and identification controls.  There is an unresolved issue with thread safe validation within the custom UserType class brokering String values to JAXB objects.  Otherwise, the Core is bug free and lacks features not fixes before being ready for a beta release.

The Core module like other Klistret modules use [Ivy](http://ant.apache.org/ivy) as a dependency manager.  No distribute site exists at the moment other than setting up a URL resolver in Ivy to Google Code.

Deployment is still dependent on manual configuration of an array of XML files to setup up both Spring and Hibernate in relation to the container.  Configuration should be done [without an overflow of XML](http://nurkiewicz.blogspot.com/2011/01/spring-framework-without-xml-at-all.html) and driven with Klistret centric properties hiding framework settings.  The goal is to pass deployment without manually adjusting the configuration like with [Web fragments](http://www.youtube.com/watch?v=4BMLITXukdY).

The bindings between Hibernate proxies for Elements and Relations have a collection of source and destination Relations to an Element.  This binding is necessary to inhibit querying as joins between elements via relationships.  But the Element and Relation proxies are meant to be disjointed.  Lazy initialize is true to postpone collection selection and inside the DAOs Elements proxies are tossed in favor for a new object without copying over the relationship collections.  The dirty handling off Hibernate proxies ought to be replaced by utilizing [Tuplizers](http://java-world-id.blogspot.com/2010_04_07_archive.html).  There is an unnecessary amount of object creation just to eliminate lazy loading exceptions.

A generic interface for identification and relationship management is a condition for starting up the Blueprint module.

### Year ahead ###
Integration with other CMDB federations is a priority.  The [CI model](CI.md) has several so called context CIs such as organizations, people and so forth that are good candidates for federating (i.e. outsourcing to another CMDB).  Companies that get into the ITIL swing usually put in place a mainstream CMDB to drive processes which craves a high degree of organizational material to direct RFC and incidents to the right group.  Contextual data should be federated into Klistret and this leads to a robust import/export mechanism.  A nice feature with Resteasy is [messaging](http://www.jboss.org/hornetq/rest.html) that fits neatly into a batch oriented data exchange.  In a broader perspective mass data loads/unloads would benefit from messaging.

Security is another layer relevant for Core.  The [Acegi](http://www.acegisecurity.org) now [Spring Security](http://static.springsource.org/spring-security/site) is a viable route.  The biggest concern is either integrating a security extension into existing user stores or providing a simple store without platform dependencies.

## Design ##
CIs are modeled with XML schema.  Storing CIs in something else than XML wasn't even an option during the design of Klistret.  The major database vendors ([www.ibm.com/software/data/db2/xml IBM], [Oracle](http://www.oracle.com/technetwork/database/features/xmldb/index.html), and [SQL Server](http://msdn.microsoft.com/en-us/library/ms189887.aspx)) support XML data types and the XQuery syntax.  Furthermore, most commercial databases have an Express version free of change without support.  XML motors intertwined in relational databases are obtainable.  The consolidation of an XML motor into rational mainstream databases means the existing integration to object relational mapping frameworks like Hibernate or [TopLink](http://www.oracle.com/technetwork/middleware/toplink/overview/index.html) adhering to [JPA](http://en.wikipedia.org/wiki/Java_Persistence_API) can roll XML to and from POJO.  The key to rolling is being able to write a user type that does the marshaling between the XML in the database and the JPA objects.  Here Hibernate still [outshines](http://relation.to/Bloggers/ATypesafeCriteriaQueryAPIForJPA) the JPA specification.

The first logical layer around the CI hierarchy is [Hibernate](Hibernate.md).  What Klistret does is handle the serialization of POJOs representing CI metadata into a format acceptable to the database managers XML type and translate XPath queries into SQL/XQuery hybrids supported by the database dialect.

[JAXB](http://en.wikipedia.org/wiki/Java_Architecture_for_XML_Binding) ([JSR222](http://jcp.org/en/jsr/detail?id=222)) is used to map XML to POJO.  The XJC tool creates the [CI module](CI.md).  Klistret has utilities to simplify the creation of the JAXB context into a singleton and access the schema meta information buried inside the schema descriptors.  [Using JAXB](JAXB.md) over other XML binding kits (like [XMLBeans](http://xmlbeans.apache.org)) boiled down to ease-of-use with RestEasy and preserving the XML schema extensions in the CI model as subclasses in Java.

[Saxon](http://saxon.sourceforge.net) is a killer XPath/Xslt processor that furnishes an exceptional XPath model along side it's [XDM](http://www.w3.org/TR/xpath-datamodel).  A critical piece to searching CIs in Klistret is the use of [XPath expressions](Saxon.md) which later are translated to database calls peppered with XQuery.  Only relative paths are allowed.  Saxon does the hard stuff in parsing XPath statements to an object model.  Klistret recursively analyses the Saxon path representation into an ordered set of XPath steps (i.e. axises with or without predicates).  When a relative path is broken down to steps the original XPath statement can be mapped to a Hibernate criteria.

The interplay to serialize XML/POJO through Hibernate and the XPath querying thanks to the Saxon parser ports the entire CI hierarchy into a well-defined object model.  [Spring](Spring.md) and [RestEasy](RestEasy.md) are just the service glue to connect the CI object model to the outside world.  And that is the nuts-and-bolts behind Klistret.