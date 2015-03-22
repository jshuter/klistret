The core module exposes querying mechanism made up of XPath filters.

## Description ##
The DMTF group has a [CMDBf specification](http://www.dmtf.org/standards/cmdbf) that predicates to major services.  One for querying from CMDBs and the other for registration of CIs.  Querying according to DMTF should:
> _...select and return items, relationships, or graphs containing items and relationships, and the data records associated with each item and relationship_

The 1.1 specification goes on to break down how querying ought to be implemented.  Unfortunately, the query interface keys off how records are stored (i.e. all of the constraint variations).  Klistret takes a different approach by only using XPath expression (close to the _xpathConstrain_ CMDBf syntax) as filters.  Only a fraction of the [XPath 2.0](http://www.w3.org/TR/xpath20/) functions and expression (here expressions means a Comparison expression or Step expression assuming a decent level of familiarity with XPath).  These limitations described below are done in consideration to perform and directly related to what databases are supported (currently DB2, Oracle, and Postgresql).

Future versions of Klistret may support the CMDBf Query syntax.

## XPath 2.0 Support ##
XPath expressions are explained by the [Saxon framework](http://saxonica.com/welcome/welcome.xml).  Saxon as of 9.3 swallows almost everything in XQuery 3.0 and is an amazing open-source project.  Parsing a single XPath expression into a hierarchy of it's basic expressions is no easy task.  Saxon does this quick and reliable.  Klistret piggybacks onto Saxon by putting an path expression (which may only flow forward) into a series of steps with restrictions on the construction of step predicates.  In other words, only XPath 2.0 **relative** path expressions are allowed and each axis moves forward one step.  A step may have one or more predicates (what is enclosed in square brackets).  Steps traverse the XMl document while predicates restrict like the where clause in SQL.  Currently, general and value comparisons are viable predicates.

The reduction of what is possible with XPath 2.0 in Klistret is forced into play because the XPath has to perform well as an [XMLEXISTS](http://publib.boulder.ibm.com/infocenter/db2luw/v9/index.jsp?topic=%2Fcom.ibm.db2.udb.admin.doc%2Fdoc%2Fr0022228.htm) clause against the database.  Pre-compiled queries run best at least against DB2.  The ability to compile a query with [explicit casting](http://publib.boulder.ibm.com/infocenter/db2luw/v9r7/index.jsp?topic=%2Fcom.ibm.db2.luw.xml.doc%2Fdoc%2Fc0023901.html) of variables (i.e. switching out comparison values as a variable to increase query reuse) heavily impacts performance.  Everything in Klistret is cast as a VARCHAR in XPath queries.

Only the following operators are allowed for comparison expressions (i.e. majority of expressions in predicates):
| **Operator** | **Supported** | **Usage** | **Example** |
|:-------------|:--------------|:----------|:------------|
| eq | Yes | Equals operator | [a:ball eq "round"] |
| nq | Yes | Not equals | [a:ball nq "square" or b:block nq "soft"] |
| lt | Yes | Less than | [a:small lt 10 and a:tiny lt 1] |
| le | Yes | Less than or equals | [a:small le 10] |
| gt | Yes | Greater than | [a:big gt 10 and a:giant gt 100] |
| ge | Yes | Greater than or equals | [a:giant ge 100] |

With general expressions:
| **Operator** | **Supported** | **Usage** | **Example** |
|:-------------|:--------------|:----------|:------------|
| = | Yes | Equality between operand sequences of any length (even sequences that contain atomic entries) | [a:ball = ('round','bounce')] |
| != | No | Non equality | n/a |
| < | No | Less than | n/a |
| <= | No | Less than or equals | n/a |
| > | No | Greater than | n/a |
| >= | No | Greater than or equals | n/a |

[Functions](http://www.w3schools.com/xpath/xpath_functions.asp) that are available:
| **Function** | **Usage** | **Example** |
|:-------------|:----------|:------------|
| exists | Returns true if the value of the arguments IS NOT an empty sequence, otherwise it returns false | [exists(a:ball)] |
| empty | Returns true if the value of the arguments IS an empty sequence, otherwise it returns false | [empty(a:ball)] |
| matches | Returns true if the string argument matches the pattern, otherwise, it returns false | [matches(a:ball,'round|bounce')] |

## Examples ##
This section show typical XPath expressions.  Although the best way to see how filters are used live is to run the Simple Web GUI in Google Chrome and look at the XHR calls (the same process works in [Firebug](http://getfirebug.com/) or [Fiddler](http://www.fiddler2.com/fiddler2/)).  Another good source of examples are the [Perl](PerlClient.md) and [Java](JavaClient.md) pages.

### Getting Software ###
The first filter selects only active elements having an element type of Software.  Any element has does not have a _toTimeStamp_ property equal to null is considered inactive.  Nothing is ever deleted from Klistret rather tombstones in the form om time stamps mark records as dormant.  This is a common filter added to identification (Blueprints).  The second filter gets Software named _INF_ while the third filter restricts by _Availability_.  Finally, the forth filter removes anything older than the 9th of December, 2011 by casting a string value to a date.  This cast is only needed when dealing with _http://www.klistret.com/cmdb/ci/pojo_ namespace since the properties of an Element map directly to a physical table in the database (i.e. everything outside that namespace is XML data).
```
declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; /pojo:Element[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq "{http://www.klistret.com/cmdb/ci/element/component}Software"]
declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; /pojo:Element[pojo:name = ("INF")]
declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; declare namespace component="http://www.klistret.com/cmdb/ci/element/component"; /pojo:Element/pojo:configuration[component:Availability = ("Nov2009R")]
declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; /pojo:Element[pojo:fromTimeStamp gt "2007-12-09T00:00:00.000 01:00" cast as xs:dateTime]
```
  * **Warning:** It is not possible at the moment to merge the 2 predicates in the first filter into one predicate with the _AND_ or _OR_ operators.  Predicates containing an operand with a relative path (i.e. multiple steps) has to be segregated from other predicates on the axis.

### Going through relations ###
Traversing relationships is another common type of query.  Getting at all of the publications that are destination dependencies to a sole software version is something typical:
```
declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; /pojo:Element[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq "{http://www.klistret.com/cmdb/ci/element/component}Publication"]
declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; declare namespace component="http://www.klistret.com/cmdb/ci/element/component"; /pojo:Element/pojo:destinationRelations[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq "{http://www.klistret.com/cmdb/ci/relation}Dependency"]/pojo:source[pojo:name = ("INF")][pojo:type/pojo:name eq "{http://www.klistret.com/cmdb/ci/element/component}Software"]/pojo:configuration[component:Version eq "0068_A01"]'
declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; declare namespace commons="http://www.klistret.com/cmdb/ci/commons"; /pojo:Element/pojo:configuration[commons:Tag = ("Important") ]
```
The first filter gets the active elements of type Publication.  The second filter despite being hard to read is fairly simple restricting via a destination relationship the _Version_ value of the sourced element being of type Software with the name _INF_.  The expression includes only relationships that are of type _Dependency_ and active.  Publications are always the destination in relationships to Source CIs.  The tailing filter just is an extra inclusion av elements with a _Tag_ equal to _Important_.