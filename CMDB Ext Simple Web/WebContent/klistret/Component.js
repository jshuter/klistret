Ext.namespace('CMDB.ApplicationSoftware');



/**
 * Application Software - Software form forcing associations
 * to organizations, modules and a timeframe.
 *
 */
CMDB.ApplicationSoftware.SoftwareForm = Ext.extend(Ext.form.FormPanel, {
	initComponent  : function() {
		var config = {
			title       : 'Software',
			autoScroll  : true,
			labelAlign  : 'top',
			bodyStyle   : 'padding:10px; background-color:white;',
			defaults    : {
				width             : 300
			},
			
			items       : [
				{
					xtype             : 'displayfield',
					width             : 'auto',
					'html'            : 'Similar to Ivy and Maven appliation software is produced by an organization (group) and ships as a module thereafter further decorated with version number, artifact ids and so forth.'
				},
				{
					xtype             : 'combo',
					elementdata       : true,
					fieldLabel        : 'Organization',
					allowBlank        : false,
					blankText         : 'Organization is required',
					store             : CMDB.OrganizationStore,
					displayField      : 'Name',
					mode              : 'remote',
					queryParam        : 'expressions',
					forceSelection    : true,
					
					// Edit the query for the combo into an expression
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"' + e.query + '%\")]';
						}
					},
					
					// Marshall combo into the element
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Organization'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Organization');
						}
					},
					
					// Unmarshall element value into the combo
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/Organization/$');
						this.setValue(value);
					}
				},
				{
					xtype             : 'combo',
					elementdata       : true,
					fieldLabel        : 'Module',
					allowBlank        : false,
					blankText         : 'Module is required',
					store             : CMDB.ModuleStore,
					displayField      : 'Name',
					mode              : 'remote',
					queryParam        : 'expressions',
					forceSelection    : true,
					
					// Edit the query for the combo into an expression
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"' + e.query + '%\")]';
						}
					},
					
					// Marshall combo into the element
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Module'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Module');
						}
					},
					
					// Unmarshall element value into the combo
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/Module/$');
						this.setValue(value);
					}
				},
				{
					xtype             : 'textfield',
					elementdata       : true,
					fieldLabel        : 'Version',
					allowBlank        : false,
					blankText         : 'Enter a version',
					
					// Marshall combo into the element
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Version'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Version');
						}
					},
					
					// Unmarshall element value into the combo
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/Version/$');					
						this.setValue(value);
					}
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.ApplicationSoftware.SoftwareForm.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('applicationSoftwareForm', CMDB.ApplicationSoftware.SoftwareForm);



/**
 *
 */
CMDB.ApplicationSoftware.LifecycleForm = Ext.extend(Ext.form.FormPanel, {
	initComponent  : function() {
		var config = {
			title       : 'Lifecycle',
			autoScroll  : true,
			labelAlign  : 'top',
			bodyStyle   : 'padding:10px; background-color:white;',
			defaults    : {
				width             : 300
			},
			
			items       : [
				{
					xtype             : 'displayfield',
					width             : 'auto',
					'html'            : 'All software goes through different stages in a lifecycle and this form establishes the phase, availability plus how an organization regards this software by type.'
				},
				{
					xtype             : 'combo',
					elementdata       : true,
					fieldLabel        : 'Phase',
					allowBlank        : true,
					store             : CMDB.SoftwareLifecycleStore,
					displayField      : 'Name',
					mode              : 'remote',
					queryParam        : 'expressions',
					forceSelection    : true,
					
					// Edit the query for the combo into an expression
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"' + e.query + '%\")]';
						}
					},
					
					// Marshall combo into the element
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Phase'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Phase');
						}
					},
					
					// Unmarshall element value into the combo
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/Phase/$');
						this.setValue(value);
					}
				},
				{
					xtype             : 'combo',
					elementdata       : true,
					fieldLabel        : 'Availability',
					allowBlank        : true,
					store             : CMDB.TimeframeStore,
					displayField      : 'Name',
					mode              : 'remote',
					queryParam        : 'expressions',
					forceSelection    : true,
					
					// Edit the query for the combo into an expression
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"' + e.query + '%\")]';
						}
					},
					
					// Marshall combo into the element
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Availability'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Availability');
						}
					},
					
					// Unmarshall element value into the combo
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/Availability/$');
						this.setValue(value);
					}
				},
				{
					xtype             : 'textfield',
					elementdata       : true,
					fieldLabel        : 'Organizational type',
					allowBlank        : true,
					
					// Marshall combo into the element
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Type'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Type');
						}
					},
					
					// Unmarshall element value into the combo
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/Type/$');					
						this.setValue(value);
					}
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.ApplicationSoftware.LifecycleForm.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('applicationLifecycleForm', CMDB.ApplicationSoftware.LifecycleForm);



/**
 * Application Software (Editor Form)
 */
CMDB.ApplicationSoftware.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns9'  : 'http://www.klistret.com/cmdb/ci/element',
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
					'ns11' : 'http://www.klistret.com/cmdb/ci/element/component/software',
					'ns2'  : 'http://www.klistret.com/cmdb/ci/commons',
					'$'    : 'http://www.klistret.com/cmdb/ci/pojo'
				},
			
			'type' : {
				'id' : {
					'$' : null
				},
				'name' : {
					'$' : null
				}
			},
			'fromTimeStamp' : {
				'$' : new Date()
			},
			'createTimeStamp' : {
				'$' : new Date()
			},
			'updateTimeStamp' : {
				'$' : new Date()
			},
			'configuration' : { 
				'@xmlns' : {
					'xsi' : 'http://www.w3.org/2001/XMLSchema-instance'
				},
				'@xsi:type' : 'ns11:ApplicationSoftware'
			}
		}
	},
	
	initComponent  : function() {
		var index = CMDB.ElementTypes.find('Name','ApplicationSoftware'),
			type = CMDB.ElementTypes.getAt(index).get('ElementType');
		
		this.element['Element']['type']['id']['$'] = type['id']['$'];
		this.element['Element']['type']['name']['$'] = type['name']['$'];
		
		var config = {
			title       : 'Application Software Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm',
					helpInfo    : 'Application software is software designed to help users or even business processes to perform singular or multiple related specific tasks.  This CI is what makes up logical applications.',
					tags        : [
						['Third party'],
						['Open source'],
						['Commercial'],
						['Homegrown'],
						['Freeware'],
						['Firmware']
					]
				},
				{
					xtype       : 'applicationSoftwareForm'
				},
				{
					xtype       : 'applicationLifecycleForm'
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.ApplicationSoftware.Edit.superclass.initComponent.apply(this, arguments);
	}
});



/** 
 * Application Software (Search Form)
 */
CMDB.ApplicationSoftware.Search = Ext.extend(CMDB.Element.Search, {

	initComponent  : function() {
		var form = new Ext.form.FormPanel({
			border          : false,
			bodyStyle       : 'padding:10px; background-color:white;',
			baseCls         : 'x-plain',
			labelAlign      : 'top',        	
			defaults        : {
				width            : 300
			},
			
			items           : [
				{
					xtype             : 'displayfield',
					width             : 'auto',
					'html'            : 'Search criteria for Application Software items'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Name',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Organization',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[matches(sw:Organization,\"{0}\")]'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Module',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[matches(sw:Module,\"{0}\")]'
				},
				{
					xtype             : 'datefield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Created after',
					format            : 'Y-m-d',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"{0}\" cast as xs:dateTime]'
				},
				{
					xtype             : 'datefield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Created before',
					format            : 'Y-m-d',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp lt \"{0}\" cast as xs:dateTime]'
				}
			]
		});
	
		var config = {
			title       : 'Application Software Search',
			editor      : CMDB.ApplicationSoftware.Edit,
			
			elementType : 'ApplicationSoftware',

			items       : form,
		
			fields      : [
				{
					name        : 'Id', 
		 			mapping     : 'Element/id/$'
		 		},
				{
					name        : 'Name', 
					mapping     : 'Element/name/$'
				},
				{
					name        : 'Organization', 
					mapping     : 'Element/configuration/Organization/$'
				},
				{
					name        : 'Module',
					mapping     : 'Element/configuration/Module/$'
				},
				{
					name        : 'Version',
					mapping     : 'Element/configuration/Version/$'
				},
				{
					name        : 'Element',
					mapping     : 'Element'
				}
			],
			
			columns        : [
				{
					header      : 'Name', 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Name'
				},
				{
					header      : 'Organization', 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Organization'
				},
				{
					header      : 'Module', 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Module'
				},
				{
					header      : 'Version', 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Version'
				}
			]
		}
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Environment.Search.superclass.initComponent.apply(this, arguments);
	}
});
