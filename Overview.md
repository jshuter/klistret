The page is an overview of the major components inside Klistret.

## Overview ##
Klistret provides in the initial release only core services but subsequent versions will expand to include extensions for: change management, automatic installation, and test management.

The basic idea is to reverse the existing paradigm with CMDB repositories from a discovery warehouse to a configuration generator.  Medium and large organizations often have a landscape composed of several test levels before entering production.  Klistret intends to minimize the negative impact of human error in conjunction with installations or configuration of developed IT applications by generating the runtime environment automatically.

At the heart of Klistret is a hierarchy of configuration items constructed in a series of extending XML schema.  This tree structure of CIs represents configuration elements, their relations and their transportation proxies.  The XML schema is rolled into POJOs suitable for marshaling from the database model through the [REST](http://en.wikipedia.org/wiki/Representational_State_Transfer) services externally into a wide variety of media formats (types).  Since the underlying data model is XML [searching](QueryingCI.md) is implemented with [XPath](http://www.w3.org/TR/xpath20).  CI metadata and relationships can be quite complex and the simplicity of XPath makes searching straightforward as well clearly readable.  The ability to expose CIs through REST services with any reasonable media type eases integration without changing the service layer.  The following sections describe the major Klistret components and how they facilitate CIs to end users after an end user's needs.

## CI ##
Identical to commercial solutions there is a hierarchy of configuration items representing well established element and there interconnected relationships.  The [CI](CI.md) module is a set XML schema documents that embody configuration items as POJO objects. [Rob England](http://www.itsmwatch.com/itil/article.php/3702086/A-CMDB-Can-Be-Done-But-Why-Would-You-Want-To.htm) captures in his blog the evolution of CIs within Klistret.  The return value on starting a CMDB through Klistret is the tradeoff in how much effort the management of CIs (often manually) takes against making the management of the runtime environment more effective (ie. faster installations, secure configurations etc).

## Core ##
The [Core](Core.md) is responsible for marshaling the XML hierarchy of CIs not only from the database but out through the REST gateway, an interface for searching, CI identification as well relationships rules and integration with other CMDB solutions.

## Blueprints ##
[Blueprints](Blueprints.md) are views of particular elements and specific relationships to throttle levels of complexity against user interests in the CMDB.

## Web Interface ##
Here is where the majority of end users engage Klistret.  The goal of the [web interface](Extjs.md) is be browser non-specific, provide a desktop like interaction and have a variety of graphical representations of CIs to give a visual picture of the CMDB.

## Spin-off projects ##
Klistret is meant to describe how entities are _supposed to be_ setup.  There will be spin-off projects that integrate with Klistret that perform platform specific configurations.  For example, the configuration of Weblogic domains based on Klistret metadata will shortly be published.