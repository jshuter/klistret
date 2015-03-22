Software is a central player for inventory both for what an organization consumes from other organizations och what produces itself.

## Description ##
Components are inventory.  Software being a component derivative is also inventory.  To limit confusion it is important that the initial definition
of software keeps itself inside the conceptual boundaries of inventory.  The [DMTF CIM](http://www.dmtf.org/standards/cim) has no obvious schema for software despite it being the first thing organizations either produce or allocate.  The [DMTF Application](http://www.wbemsolutions.com/tutorials/CIM/cim-model-app.html) encapsulates the general concepts of software by looking at it from the angle of systems that need features and functionality.  All of sudden the basic need for modeling inventory explodes into a bear hug around services and systems.  [Ivy](http://ant.apache.org/ivy) (similarly Maven underneath the monolith of project theory) has a simpler approach.  There is nothing within the Ivy documentation which compares Ivy as a dependency manager and modelling software as a CI.  However, Ivy like Maven unknowingly is a modern representation of software as inventory.

Ivy talks about modules rather than using the word _software_.  Software is more inline with CMDB terminology.  Modules and software are logically equivalent.  Regardless if software is open source or commercial there is an organization which produces the software.  The identification of software as a module normally consists of an organization, a module name, a version and potentially an organizational type.

Modules are stuff that is deliverable.  Modules can have intertwined dependencies to each other.  Ivy terms modules as _reusable unit of software_ that _follows a revision control scheme_.  There is no talk about packaging software according to function.  The birth and growth of modules depends more on how the software is developed not how it is used.  This is why a module is not the actual object being delivered rather a logical container that may house no so called artifacts ([Publication CI](PublicationCI.md)) but only dependencies to other modules or many artifacts with no dependencies.  There is enormous flexibility.  It is possible to define an application software (take Microsoft Office) as a module with artifacts just dependencies (to Microsoft Word or Powerpoint ).

Ivy doesn't ignore totally how software is used.  The whole concept of module configurations is paramount when testing software or running it in a development container or packaging it for production.  Module configurations live in the definition of dependencies and in Klistret inside relations.  Configurations aid in filtering module hierarchies often during the installation and configuration of software in a targeted system.

### Characteristics ###
Below are the properties that constitute a Software CI.  The CI is a direct descendant of the Component CI.

| **Property** | **Description** | **Required** | **Unbounded** | **Key** | **Context** |
|:-------------|:----------------|:-------------|:--------------|:--------|:------------|
| Organization | There is always a producer behind software.  This property stores the name of the organization.  An organization hierarchy is flattened into usually a dot delimited namespace (ex. the Resteasy project at JBoss is named _org.jboss.resteasy_).  The property (element) is part of the _http://www.klistret.com/cmdb/ci/element/component_ namespace. | Yes | No | Yes | Yes |
| Version | A version identifier often is an increasing series that denotes a snapshot of the software primarily by time aspects.  The property (element) is part of the _http://www.klistret.com/cmdb/ci/element/component_ namespace. | Yes | No | Yes | No |
| Availability | The version property places the software in an ordered context thereby indirectly a time frame the availability property directly specifics a specific time frame.  The property (element) is part of the _http://www.klistret.com/cmdb/ci/element/component_ namespace.  The natural weak association to meta data is the contextual TimeFrame CI. | No | No | No | Like |
| Phase | Phase is similar to the contextual Lifecycle CI representing [software lifecycles](http://en.wikipedia.org/wiki/Software_development_process) (requirements, design, so forth).  This is an additional aspect pinpointing the maturity of software (nearly identical to the **status** attribute of the **info** element in Ivy).  The property (element) is part of the _http://www.klistret.com/cmdb/ci/element/component_ namespace | No | Yes | No | Like |
| Label | A label is an alias which might be an unique tag for the software element.  Not every organization is come to the point of describing their software only in terms of organizations and versions.  Labels make for easy unique names with internal projects.  The property (element) is part of the _http://www.klistret.com/cmdb/ci/element/component_ namespace | No | No | Maybe | No |

  * **Key** means that the property is required and the property is a candidate for blueprint identification.  A _Maybe_ value says that the property is not required but definitely a candidate for identification.
  * **Context** earmarks properties as having a corresponding contextual CI by the same name if _Yes_ or like potentially many contextual CIs if _Like_ is given.

## Usage ##
Given how important Ivy and Maven are to development without question the Software CI will be a heavily used CI in any organization.  Even though Ivy is used predominately with Java development the Software CI is not restricted to a particular coding language or target container.  Software is a logical construct palpable only via published artifacts.  Representing Tuxedo server binaries as Software isn't a problem and the dependencies to Tuxedo are defined as relations to a Tuxedo Sofware CI.

Klistret does not intend to compliment or replace meta data housed in package repositories (like Sonatype's [OSS](http://nexus.sonatype.org/oss-repository-hosting.html) repository).  Instead Klistret aims to federate this meta data so that it can be related to other stuff inside Klistret (i.e. functionality in service CIs).  Klistret will provide services to map Ivy and Maven descriptors into Software and Publication CIs inclusive relationship definitions.

## Relations ##

### General Basic ###
| **Type** | **CI** | **Direction** | **Description** |
|:---------|:-------|:--------------|:----------------|
| Dependency | Software | Both | Dependencies to other Software CI are just that Dependency CIs in Klistret.  The configuration concept part of Ivy is temporarily recommended to be stored in the Usage property (defined in the Base CI, _http://www.klistret.com/cmdb/ci/commons_ namespace). |
| Dependency | Publication | Source | The Software CI is always the owner or source of the dependency relationship to publications.  Here again the Usage property can be utilized to denote configuration usage. |
  * **Direction** is the vector of the relationship and is either _Destination_ (target), _Source_ (owner), or _Both_.

## Blueprint ##
The recommendation is to have a composite identification on the Organization, Version and Name (equivalent to a module name in Ivy) properties.  The XPath expressions for this rule is:
```
<Criterion Name="Software">
  <Expression>declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; declare namespace commons="http://www.klistret.com/cmdb/ci/commons"; /pojo:Element/pojo:configuration/commons:Name</Expression>
  <Expression>declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; declare namespace component="http://www.klistret.com/cmdb/ci/element/component"; /pojo:Element/pojo:configuration/component:Organization</Expression>
  <Expression>declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; declare namespace component="http://www.klistret.com/cmdb/ci/element/component"; /pojo:Element/pojo:configuration/component:Version</Expression>
</Criterion>
```

An organization may even want to as a failover identify by label:
```
<Criterion Name="SoftwareLabelOnly">
  <Expression>declare namespace pojo="http://www.klistret.com/cmdb/ci/pojo"; declare namespace component="http://www.klistret.com/cmdb/ci/element/component"; /pojo:Element/pojo:configuration/component:Label</Expression>
</Criterion>
```

Then these 2 rules can be ordered against all Software CI (plus their descendants):
```
<Identification Type="{http://www.klistret.com/cmdb/ci/element/component}Software">
  <CriterionRule Name="SoftwareLabelOnly" Order="1"/>
  <CriterionRule Name="Software" Order="2"/>
</Identification>
```