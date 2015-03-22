JAXB implementation from the [Metro project](https://jaxb.dev.java.net/) maps the Configuration Item (CI) hierarchy plus Hibernate transport definitions between XML schemas and Java code.

# Design #

# Details #

## XJC ##

### Spring Scanning ###
Spring has provided since 2.0 a great way to scan for classes using filters.  Rather writing a homegrown variation or using another third party library the [ClassPathScanningCandidateComponentProvider](http://blog.larsvonkconsultancy.nl/2009/01/the-power-of-classpathscanningcandidatecomponentprovider) scanner is used to find all CI extensions either of the Element or Relation CIs.  This worked fine when the complex types where not defined as abstract.  The scanner failed to follow the hierarchy with abstractions because the conditional logic only looks for concrete beans.  Not surprising since Spring is a bean injector.  Found through the Spring forums somebody else whom ran into this ["limitation"](http://forum.springsource.org/showthread.php?t=51732&highlight=isCandidateComponent+abstract) and extended the scanner to deal with abstractions.  The same thing is done in the namespace plugin for XJC.