Configuration items (CI) are not flat objects with a bunch of properties in Klistret.  Instead, a CI is initially a XML schema and later realized as POJO classes.

## Overview ##
Most definitions of a configuration item say an entity is made up of a attributes and entities are linked together by relationships.  [DMTF](http://dmtf.org/standards/cim) common information model is better since classes can have properties rather than simple attributes.  Structuring CIs as XML has advantages with inheritance and search through the XPath syntax.  This page outlines how CIs are categorized.

## Building blocks ##
John Singer does a good job of [grouping](http://www.tdan.com/view-articles/6904) CIs into major building blocks.  Singer basically buckets CIs into 3 layers: service, system, and component each deriving from a common [base CI](BaseCI.md) passed inside a [wrapper](PojoCI.md) ([Pojo](http://en.wikipedia.org/wiki/Plain_Old_Java_Object)).  Surrounding that stack of conceptual CIs are what Singer defines as contextual CIs.  What is the difference?  Conceptual CIs are the meat-and-potatoes of the CMDB whereas contextual CI do exactly what the classification suggests by providing a context for conceptual CIs to reside.  A great example of this is the need for organizational information that usually is housed in other management tools.  People and roles are useful when writing change requests but defined as full-fledged entities would be overkill.  Environments are another good case where an environment definition could be either done as conceptual or contextual.  Here the deciding factor for or against a contextual representation is ease of implementation.

Singer packed in ITIL processes under the contextual umbrella.  ITIL processes are change requests, incidents, problems plus other tasks that typically result in an effect against an CMDB entity.  Contextual entities in Klistret act more like views to segregate conceptual entities from one another.  Processes imply an action that changes conceptual as well contextual entities.  Klistret elevates processes to the same logical level as contextual and conceptual entities.

## Conceptual ##
Again conceptual entities are conglomeration of services, systems, and components.  Together these entities describe what functionality an organization offers in a runtime landscape configured out of a flora of hardware and software deliverables.

### Service ###
An organization supplies functionality to users or other systems as services.  Another way to express [services](ServiceCI.md) is that they are logical end-points for what should be done by the underlying computer system.  Services build on other services in scope and are related to a runtime system.

### System ###
Congruent with services there is substantial layering inherient to systems whereby one system encompasses smaller child systems of a more unnormalized type.  Since DMTF and commercial CMDB frameworks are outfitted as discovery tools a central system component is [computer system](ComputerSystemCI.md).  Large aggregations of other system entities to a computer system are a way of packaging functionality into large units.  Klistret differs here growing in complexity with an origin in the perspective of applications.  The major system entities are [applications](ApplicationCI.md) and [application systems](ApplicationSystemCI.md) or [application infrastructures](ApplicationInfrastructureCI.md).

### Component ###
Inanimate items signalize [components](ComponentCI.md).  [Software](SoftwareCI.md) is inoperative until it is configured and deployed.  [Hardware](HardwareCI.md) is just wires and bolts until it is booted up.  Components are inert while systems are what components become when active.  Components are inventory.

## Contextual ##
One of the driving forces behind Klistret is the idea of describing how stuff should be configured and associated.  Contextual entities help filter inventory meant for installation in systems screened by their contexts whom do work for services also widdled down by context.  Contexts are filters generally sourced from external tools.  Major contextual entities are [organizations](OrganizationCI.md), [software (i.e. modules)](SoftwareContextCI.md), [publication types](PublicationTypeCI.md), [environments](EnvironmentCI.md), [lifecyles](LifecycleCI.md) and [timeframes](TimeframeCI.md) to name a few.

## Process ##
ITIL has cornerstone processes such as change, incident, and problem.  Klistret is geared more to the changes process but in the future may even encapsulate (via federation) incident and problem.