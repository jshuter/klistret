/**
 *
*/
Ext.namespace('CMDB.ApplicationSoftware');


CMDB.ApplicationSoftware.OrganizationStore = new Ext.data.Store({
	proxy        : new Ext.data.HttpProxy({
		url            : (CMDB.URL || '') + '/CMDB/resteasy/element',
		method         : 'GET',
                                        
		headers        : {
			'Accept'          : 'application/json,application/xml,text/html',
			'Content-Type'    : 'application/json'
		}
	}),
	
	reader      : new CMDB.JsonReader({
		totalProperty       : 'total',
		successProperty     : 'successful',
		idProperty          : 'Element/id/$',
		root                : 'rows',
		fields              : [
			{
				name             : 'Id',
				mapping          : 'Element/id/$'
			},
			{
				name             : 'Name',
				mapping          : 'Element/name/$'
			}
		]
	}),
	
	listeners         : {
		'beforeload'       : function(store, options) {
			var expressions;
			
			expressions = Ext.urlEncode({
				expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]'
			});
			expressions = expressions + "&" + Ext.urlEncode({
				expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[matches(pojo:name,\"Environment\")]'
			});
			expressions = expressions + "&" + Ext.urlEncode({
				expressions : store.baseParams.expressions
			});
			
			options.params = "start=0&limit=10&"+expressions;	
		}
	}
});


CMDB.ApplicationSoftware.ModuleStore = new Ext.data.ArrayStore({
	fields       : ['name', 'description'],
    data         : [
        ['KND', 'Kund'],
        ['KUI', 'Kunskapsinfo'],
        ['JUnit', 'Java Unit Testing']
    ]
});


CMDB.ApplicationSoftware.GeneralForm = Ext.extend(Ext.form.FormPanel, {
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
					xtype             : 'combo',
					elementdata       : true,
					fieldLabel        : 'Organization',
					allowBlank        : false,
					blankText         : 'Organization is required',
					store             : CMDB.ApplicationSoftware.OrganizationStore,
					displayField      : 'Name',
					mode              : 'remote',
					queryParam        : 'expressions',
					forceSelection    : true,
					
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"%' + e.query + '%\")]';
						}
					},
					
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Organization'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Organization');
						}
					},
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
					store             : CMDB.ApplicationSoftware.ModuleStore,
					displayField      : 'name',
					mode              : 'local',
					forceSelection    : true,
					
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Module'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Module');
						}
					},
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
					// Read from object into JSON
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/component/software');
							element['Element']['configuration'][prefix+':Version'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Version');
						}
					},
					// Read from JSON into object
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/Version/$');					
						this.setValue(value);
					}
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.ApplicationSoftware.GeneralForm.superclass.initComponent.apply(this, arguments);
	}
});

Ext.reg('applicationSoftwareGeneralForm', CMDB.ApplicationSoftware.GeneralForm);



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
					xtype       : 'applicationSoftwareGeneralForm'
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


CMDB.ApplicationSoftware.Search = Ext.extend(CMDB.Element.Search, {
	/**
	 *
	 */
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
	},
	
	
	/**
	 * Apply extra filters
	 */
	beforeSearch   : function() {
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]'});
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[matches(pojo:name,\"ApplicationSoftware\")]'});
	}
});