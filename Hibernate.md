Hibernate (rather than JPA 2.0) is the data model manager.

# Hibernate #
[Hibernate](http://www.hibernate.org) has been around for many years and is stable.  Plus the integration with Spring is straightforward (show later on).

## JPA ##
Until there is a way to write [user types](http://www.bashanblog.com/2009/10/using-user-type-enumeration-in.html) in JPA 2.0 annotations the Hibernate configuration must remain in external XML files.  A better solution would be to translate these XML directives into JPA 2.0 annotations during XJC generation with [HyperJAXB](https://hyperjaxb.dev.java.net).

### Spring 3.0 ###
[Spring 3.0](http://paulszulc.wordpress.com/2010/01/09/jpa-2-0-and-spring-3-0-with-maven) hooks in JPA with Hibernate as the underlying implementation.

# Details #

## XJC integration ##

### Java Type ###
[Customization](http://fusesource.com/docs/framework/2.2/jaxws/JAXWSCustomTypeMappingJavaType.html) of Java types is necessary to transform long into Long as well handle date formats with JAXB.

### Identity ###
[Identity](http://onjava.com/pub/a/onjava/2006/09/13/dont-let-hibernate-steal-your-identity.html?page=1) is established with the **equals** and **hashcode** methods that are injected into the generated POJO classes with XJC.  There is an [equals plug-in](http://confluence.highsource.org/display/J2B/Equals+plugin) for XJC but the syntax is messy.  It is easier to use the [code injector](http://weblogs.java.net/blog/2005/06/01/writing-plug-jaxb-ri-really-easy) part of the JAXB commons framework.  Another solution is to extend every Hibernate class with predefined generic methods.

### User Type ###
The major Pojo classes Element and Relation have CI properties defined by the pojo.xsd schema.  These CI properties are mapped by Hibernate through a user type which translates the XML sql-column between a string representation and JAXB generated class.  The user type extends the [ParameterizedType](http://docs.jboss.org/hibernate/core/3.5/javadocs/org/hibernate/usertype/ParameterizedType.html) which lets parameters be passed to the class at creation time.  Temporarily parameters are used to construct a JAXBContext.  Spring provides a way to inject dependencies into objects constructed outside of the application context with the [Configurable annotation](http://chris-richardson.blog-city.com/migrating_to_spring_2_part_3__injecting_dependencies_into_en.htm).  However [Load Time Weaving](http://www.gridshore.nl/2009/01/27/injecting-domain-objects-with-spring/) requires a Java agent during JVM start up that isn't such a clean solution.