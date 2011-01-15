/**
 * Name-spaces
*/
Ext.namespace('CMDB.System');


CMDB.System.StateStore = new Ext.data.ArrayStore({
        fields       : ['name', 'description'],
    data         : [
        ['Online', 'System is online or active'],
        ['Offline', 'System is offline or inactive'],
        ['Transition', 'System is in transation either to an online or offline state']
    ]
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
					fieldLabel        : 'State',
					allowBlank        : false,
					blankText         : 'State is required',
					store             : CMDB.System.StateStore,
					displayField      : 'name',
					mode              : 'local',
					forceSelection    : true,
					
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element');
							element['Element']['configuration'][prefix+':State'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/State/$');
						}
					},
					unmarshall        : function(element) {
					}
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.System.GeneralForm.superclass.initComponent.apply(this, arguments);
	}
});

Ext.reg('systemGeneralForm', CMDB.System.GeneralForm);