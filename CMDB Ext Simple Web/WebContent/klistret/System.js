/**
 * 
*/
Ext.namespace('CMDB.System');
Ext.namespace('CMDB.Environment');


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
 * Extends Element editor by defining an element template
 * and initializes the component with predefined forms.
 */
CMDB.Environment.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns9'  : 'http://www.klistret.com/cmdb/ci/element',
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/system',
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
				'@xsi:type' : 'ns10:Environment'
			}
		}
	},

	/**
	 *
	 */
	initComponent  : function() {
		var index = CMDB.ElementTypes.find('Name','Environment'),
			type = CMDB.ElementTypes.getAt(index).get('ElementType');
		
		this.element['Element']['type']['id']['$'] = type['id']['$'];
		this.element['Element']['type']['name']['$'] = type['name']['$'];
		
		var config = {
			title       : 'Environment Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm',
					tags        : [
						['Production'],
						['Test'],
						['Development'],
						['Verification'],
						['Sandbox']
					]
				},
				{
					xtype       : 'systemGeneralForm'
				},
				{
					xtype       : 'destRelationForm',
					relations   : [
						{
							'ApplicationSoftware' : 'Aggregation'
						}
					]
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Environment.Edit.superclass.initComponent.apply(this, arguments);
	}
});



/**
 *
 */
CMDB.Environment.Search = Ext.extend(CMDB.Element.Search, {

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
					'html'            : 'Search criteria for this CI (Configuration Item)'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Name',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]'
				},
				{
					xtype             : 'datefield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Created after',
					format            : 'Y-m-d',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"{0}\" cast as xs:dateTime]'
				},
				{
					xtype             : 'datefield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Created before',
					format            : 'Y-m-d',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp lt \"{0}\" cast as xs:dateTime]'
				}
			]
		});
	
		var config = {
			title       : 'Environment Search',
			editor      : CMDB.Environment.Edit,

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
					name        : 'Watermark', 
					mapping     : 'Element/configuration/@Watermark'
				},
				{
					name        : 'Element',
					mapping     : 'Element'
				}
			],
			
			columns        : [
				{
					header      : "Name", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Name'
				},
				{
					header      : "Watermark", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Watermark'
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
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[matches(pojo:name,\"Environment\")]'});
	}
});
