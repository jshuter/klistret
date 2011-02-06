Ext.namespace('CMDB.System');
Ext.namespace('CMDB.Application');



/**
 * System states as store
 */
CMDB.System.StateStore = new Ext.data.ArrayStore({
	fields       : ['Name', 'Description'],
    data         : [
        ['Online', 'System is online or active'],
        ['Offline', 'System is offline or inactive'],
        ['Transition', 'System is in transation either to an online or offline state']
    ]
});



/**
 * System (general form)
 */
CMDB.System.GeneralForm = Ext.extend(Ext.form.FormPanel, {

	initComponent  : function() {
		var config = {
			title       : 'System',
			autoScroll  : true,
			labelAlign  : 'top',
			bodyStyle   : 'padding:10px; background-color:white;',
			defaults    : {
				width             : 300
			},
			
			items       : [
				{
					xtype             : 'superboxselect',
					elementdata       : true,
					fieldLabel        : 'Environments',
					allowBlank        : true,
					store             : CMDB.EnvironmentStore,
					queryParam        : 'expressions',
					displayField      : 'Name',
					valueField        : 'Name',
					mode              : 'remote',
					forceSelection    : true,
					
					extraItemCls: 'x-tag',
								
					// Edit the query for the combo into an expression
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"' + e.query + '%\")]';
						}
					},
					
					// Read from object into JSON
					marshall          : function(element) {
						if (!Ext.isEmpty(this.getValueEx()) && element['Element']['configuration']) {
							var ele = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element')
								environments = [];
								
							Ext.each(
								this.getValueEx(), 
								function(value) {
									var environment = {
										'$' : value['Name']
									};
								
									environments[environments.length] = environment;
								}
							);
							
							element['Element']['configuration'][ele+":Environment"] = environments;
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Environment');
						}
					},
					
					// Read from JSON into object
					unmarshall        : function(element) {
						var environments = CMDB.Badgerfish.get(element, 'Element/configuration/Environment'),
							formated = [];
						
						if (Ext.isArray(environments)) {
							Ext.each(
								environments,
								function(environment) {
									formated[formated.length] = {
										'Name' : environment['$']
									};
								}
							);
						}
						
						if (Ext.isObject(environments)) {
							formated[formated.length] = {
								'Name' : environments['$']
							};
						}
						
						this.setValueEx(formated);
					}    
				},
				{
					xtype             : 'combo',
					elementdata       : true,
					fieldLabel        : 'State',
					allowBlank        : false,
					blankText         : 'State is required',
					store             : CMDB.System.StateStore,
					displayField      : 'Name',
					mode              : 'local',
					forceSelection    : true,
					
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element');
							element['Element']['configuration'][prefix+':State'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/State');
						}
					},
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/State/$');
						this.setValue(value);
					}
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.System.GeneralForm.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('systemGeneralForm', CMDB.System.GeneralForm);



/**
 * Application (Editor Form)
 */
CMDB.Application.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns9'  : 'http://www.klistret.com/cmdb/ci/element',
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
					'ns11' : 'http://www.klistret.com/cmdb/ci/element/system',
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
				'@xsi:type' : 'ns11:Application'
			}
		}
	},
	
	initComponent  : function() {
		var index = CMDB.ElementTypes.find('Name','Application'),
			type = CMDB.ElementTypes.getAt(index).get('ElementType');
		
		this.element['Element']['type']['id']['$'] = type['id']['$'];
		this.element['Element']['type']['name']['$'] = type['name']['$'];
		
		var config = {
			title       : 'Application Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm',
					helpInfo    : 'An application is a runtime conglomeration of software within an application system and is a managed object.',
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
					xtype       : 'systemGeneralForm'
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Application.Edit.superclass.initComponent.apply(this, arguments);
	}
});
