/**
 * 
*/
Ext.namespace('CMDB.System');


CMDB.System.StateStore = new Ext.data.ArrayStore({
	fields       : ['Name', 'Description'],
    data         : [
        ['Online', 'System is online or active'],
        ['Offline', 'System is offline or inactive'],
        ['Transition', 'System is in transation either to an online or offline state']
    ]
});


CMDB.System.EnvironmentStore = new Ext.data.Store({
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
	})
});


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
					xtype             : 'combo',
					elementdata       : true,
					fieldLabel        : 'Environment',
					allowBlank        : true,
					store             : CMDB.System.EnvironmentStore,
					queryParam        : 'expressions',
					displayField      : 'Name',
					mode              : 'remote',
					forceSelection    : true,
					
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"' + e.query + '%\")]';
						}
					},
					
					marshall          : function(element) {
					},
					unmarshall        : function(element) {
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
