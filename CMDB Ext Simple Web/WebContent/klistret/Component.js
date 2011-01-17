/**
 *
*/
Ext.namespace('CMDB.ApplicationSoftware');


CMDB.ApplicationSoftware.OrganizationStore = new Ext.data.ArrayStore({
	fields       : ['name', 'description'],
    data         : [
        ['Försäkringskassan', 'Swedish Social Insurance agency'],
        ['Skatteverket', 'Swedish Tax agency'],
        ['Pensionsmyndigheten', 'Swedish Pension agency']
    ]
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
					displayField      : 'name',
					mode              : 'local',
					forceSelection    : true,
					
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
});