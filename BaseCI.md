Every CI has the Base CI as an ancestor.

## Description ##
Commons for both the [element](ElementCI.md) and [relation](RelationCI.md) CIs are defined in this base class.

### Characteristics ###
Every property below has the _http://www.klistret.com/cmdb/ci/commons_ namespace.  The Base CI is abstract.

| **Property** | **Description** | **Required** | **Unbounded** | **Key** | **Context** |
|:-------------|:----------------|:-------------|:--------------|:--------|:------------|
| Name | Name given to the base instance (usually human readable). | No | No | Maybe | Maybe |
| Namespace | Namespaces in the traditional sense allow unique instances despite sharing the same identifier. An identifier defined in a namespace is associated only with that namespace. The same identifier can be independently defined in multiple namespaces. For example, it might useful to have a software CI in a namespace CMDB.EXISTING and an identical CI under CMDB.PLANNED during reconciliation. Within any CMDB there are always multiple worlds. The majority of stuff are things that can be discovered and things undergoing planing. The bridge between CMDB worlds are processes. | No | No | Maybe | No |
| Collection | Another word for collection is dataset. This like the namespace attribute this is a singular attribute. Collections denote a group of base instances rather than doing identification. CMDB.PRODUCTION would be a viable candidate for a collection. | No | No | Maybe | No |
| Tag | A non-hierarchical keyword or term assigned to a base instance. Tags enrich search criteria and may be judiciously applied by end users. A good usage for Tags is to bookmark one or more CIs for private tracking. A CM (change manager) can put his or her same on a Software CI to track it's flow through the system. | No | Yes | No | No |
| Mark | Identical to the _Tag_ property except marks are only intended for internal processing.  Marks are helpful to back end processes that the end user doesn't need to known about to hold state.  For example, a process reacting to an _Update_ event may itself update the same CI and thereby fall into an infinite loop.  A marker serves to tag the CI so the process knows it has already handled the CI. | No | Yes | No | No |
| Usage | A non-hierarchical keyword or term assigned to a base instance. Usage defines purpose. | No | Yes | No | No |
| Description | Description of the base instance (human readable). | No | Yes | No | No |
| Annotation | Notes or shorter comments placed on the base instance. | No | Yes | No | No |
| Property | A complex type composed of a _Name_ / _Value_ pairs.  The idea with this _property_ is to dump settings in CIs off the organization's needs.  Contextual Software CIs are great places to stuff with extra settings about where component Software CIs are stored, names for links, etc. | No | Yes | No | No |
| Origin | A complex type composed either just an attribute _Name_ (the origin) or either an _Identification_ value or _Composite_ made of a _Name_ / _Value_ pair. Origins are helpful when data is migrated or federated into Klistret. | No | No | No | No |
| Watermark | An attribute (not an child element) acting as a signature (MD5 etc.) | No | No | No | No |
| Revision | Version information | No | No | No | No |

## Usage ##
There is a good amount of customization baked into the Base CI.  Namespace, Collection, Tag and Origin properties can funnel CIs down by necessity from the organisation's perspective and the individual's view.  The hard part is that every property is optional so necessity isn't guarantied through XML validation.  Even with rudimentary usage of Klistret the recommendation is to assign suitable values to at least Namespace and Collection plus encourage people to experiment med Tags.

The _Property_ value can't understated.  Any number of _Property_ can be added to a CI.  Organizations tend to have substantial amount of meta data and isn't easily modeled unless everything is modeled.  For example, an organization in the upswing of ITIL may not either understand how to or be able to model enough CI to properly store it's metadata.  Until the CMDB maturely populated the _Property_ can house metadata usually in contextual CIs.  The contextual Environment CI is super spot to save metadata about DNS addresses, search paths, aliases etc.