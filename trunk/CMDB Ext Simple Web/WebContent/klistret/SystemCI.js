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
					fieldLabel        : 'State',
					allowBlank        : false,
					blankText         : 'State is required',
					store             : CMDB.System.StateStore,
					displayField      : 'name',
					mode              : 'local',
					forceSelection    : true,
					
					plugins           : [new Ext.Element.EditParameterPlugin()],
					mapping           : 'Element/configuration/State/$',
					builder           : function(element) {
						var configuration = CMDB.Badgerfish.get(element, 'Element/configuration')
							elementP = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element');
						
						if (!configuration.hasOwnProperty(elementP+":State")) {
							configuration[elementP+":State"] = {
								'$' : ''
							};
						}
					}
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.System.GeneralForm.superclass.initComponent.apply(this, arguments);
	}
});

Ext.reg('systemGeneralForm', CMDB.System.GeneralForm);