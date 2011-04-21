Ext.namespace('CMDB.Change');
Ext.namespace('CMDB.Installation');



/**
 * System states as store
 */
CMDB.Change.StateStore = new Ext.data.ArrayStore({
	fields       : ['Name', 'Description'],
    data         : [
    	['Planned', 'No activity as yet, still in planning phase'],
        ['In Progress', 'Change is being worked on'],
        ['Waiting', 'Change is on hold'],
        ['Completed', 'All activities regarding the changed have successfully been completed'],
        ['Failed', 'Failure to handle the change'],
        ['Canceled', 'Changed has been discarded or canceled']
    ]
});


/**
 * Installation (general form)
 *
 */
CMDB.Installation.GeneralForm = Ext.extend(Ext.form.FormPanel, {
	initComponent  : function() {
		var config = {
			title       : 'General',
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
					'html'            : 'An installation targets usually a component to either a context (such as an environment) or logical system for integration.'
				},
				{
					xtype             : 'combo',
					ref               : 'Environment',
					elementdata       : true,
					fieldLabel        : 'Targeted context or system',
					allowBlank        : false,
					blankText         : 'Target is required',
					store             : CMDB.EnvironmentStore,
					displayField      : 'Name',
					valueField        : 'Id',
					mode              : 'remote',
					queryParam        : 'expressions',
					forceSelection    : true,
					
					allowAddNewData   : true,
					
					// Edit the query for the combo into an expression
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"' + e.query + '%\")]';
						}
					},
					
					// Marshall combo into the element
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var change = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/process/change'),
								commons = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/commons'),
								record = this.getStore().getById(this.getValue());
								
							element['Element']['configuration'][change+':Source'] = {};
							element['Element']['configuration'][change+':Source'][commons + ':Id'] = {
								'$' : record.get('Id')
							};
							element['Element']['configuration'][change+':Source'][commons + ':Name'] = {
								'$' : record.get('Name')
							};
							element['Element']['configuration'][change+':Source'][commons + ':QName'] = {
								'$' : CMDB.Badgerfish.get(record.get('Element'), 'type/name/$')
							};
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Source');
						}
					},
					
					// Unmarshall element value into the combo
					unmarshall        : function(element) {
						var id = CMDB.Badgerfish.get(element, 'Element/configuration/Source/Id/$'), 
							data = {
								'Id' : id,
								'Name' : CMDB.Badgerfish.get(element, 'Element/configuration/Source/Name/$'),
								'Element' : CMDB.Badgerfish.get(element, 'Element')
							};
						
						var record = new (this.store.reader).recordType(data, id);	
						this.getStore().insert(0, record);
						
						this.setValue(id);
					}
				},
				{
					xtype             : 'combo',
					ref               : 'Software',
					elementdata       : true,
					fieldLabel        : 'Application software to be installed',
					allowBlank        : false,
					blankText         : 'Component is required',
					store             : CMDB.ApplicationSoftwareStore,
					displayField      : 'Name',
					valueField        : 'Id',
					mode              : 'remote',
					queryParam        : 'expressions',
					forceSelection    : true,
					
					allowAddNewData   : true,
					
					// Edit the query for the combo into an expression
					listeners         : {
						'beforequery'       : function(e) {
							e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"' + e.query + '%\")]';
						}
					},
					
					// Marshall combo into the element
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var change = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/process/change'),
								commons = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/commons'),
								record = this.getStore().getById(this.getValue());
								
							element['Element']['configuration'][change+':Destination'] = {};
							element['Element']['configuration'][change+':Destination'][commons + ':Id'] = {
								'$' : record.get('Id')
							};
							element['Element']['configuration'][change+':Destination'][commons + ':Name'] = {
								'$' : record.get('Name')
							};
							element['Element']['configuration'][change+':Destination'][commons + ':QName'] = {
								'$' : CMDB.Badgerfish.get(record.get('Element'), 'type/name/$')
							};
							
							element['Element']['name'] = {
								'$' : record.get('Name')
							};
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Destination');
						}
					},
					
					// Unmarshall element value into the combo
					unmarshall        : function(element) {
						var id = CMDB.Badgerfish.get(element, 'Element/configuration/Destination/Id/$'), 
							data = {
								'Id' : id,
								'Name' : CMDB.Badgerfish.get(element, 'Element/configuration/Destination/Name/$'),
								'Element' : CMDB.Badgerfish.get(element, 'Element')
							};
						
						var record = new (this.getStore().reader).recordType(data, id);	
						this.getStore().insert(0, record);
						
						this.setValue(id);
					}
				},
				{
					xtype             : 'combo',
					
					ref               : 'State',
					
					elementdata       : true,
					fieldLabel        : 'State',
					allowBlank        : false,
					blankText         : 'State is required',
					store             : CMDB.Change.StateStore,
					displayField      : 'Name',
					mode              : 'local',
					value             : 'Planned',
					forceSelection    : true,
					
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var prefix = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/element/process');
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
		CMDB.Installation.GeneralForm.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('installationGeneralForm', CMDB.Installation.GeneralForm);



/**
 * Installation (Editor Form)
 */
CMDB.Installation.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns9'  : 'http://www.klistret.com/cmdb/ci/element',
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
					'ns8'  : 'http://www.klistret.com/cmdb/ci/element/process',
					'ns11' : 'http://www.klistret.com/cmdb/ci/element/process/change',
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
				'@xsi:type' : 'ns11:Installation'
			}
		}
	},
	
	initComponent  : function() {
		var index = CMDB.ElementTypes.find('Name','Installation'),
			type = CMDB.ElementTypes.getAt(index).get('ElementType');
		
		this.element['Element']['type']['id']['$'] = type['id']['$'];
		this.element['Element']['type']['name']['$'] = type['name']['$'];
		
		var config = {
			title       : 'Installation Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'installationGeneralForm',
					ref         : 'InstallationGeneralForm'
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Installation.Edit.superclass.initComponent.apply(this, arguments);
	},
	
	afterLoad : function(a,b,c) {
		if (this.element && CMDB.Badgerfish.get(this.element,"Element/id/$")) {
			this.InstallationGeneralForm.Environment.disable();
			this.InstallationGeneralForm.Software.disable();
			this.InstallationGeneralForm.State.enable();
		} else {
			this.InstallationGeneralForm.Environment.enable();
			this.InstallationGeneralForm.Software.enable();
			this.InstallationGeneralForm.State.disable();
		}
	}
});



/** 
 * Installation (Search Form)
 */
CMDB.Installation.Search = Ext.extend(CMDB.Element.Search, {

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
					'html'            : 'Search criteria for Installation changes'
				},
				{
					xtype             : 'superboxselect',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Environment',
					expression        : 'declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace change=\"http://www.klistret.com/cmdb/ci/element/process/change\"; /pojo:Element/pojo:configuration/change:Source[commons:Name = {0}]',
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
					}
				},
				{
					xtype             : 'superboxselect',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Application Software',
					expression        : 'declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace change=\"http://www.klistret.com/cmdb/ci/element/process/change\"; /pojo:Element/pojo:configuration/change:Destination[commons:Name = {0}]',
					store             : CMDB.ApplicationSoftwareStore,
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
					}
				},
				{
					xtype             : 'superboxselect',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'State',
					expression        : 'declare namespace process=\"http://www.klistret.com/cmdb/ci/element/process\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:configuration[process:State = {0}]',
					displayField      : 'Name',
					valueField        : 'Name',
					mode              : 'local',

					store             : CMDB.Change.StateStore,

					addNewDataOnBlur  : true,
					
					extraItemCls: 'x-tag',
					
					listeners         : {
						newitem             : function(bs, v, f) {
							var newObj = {
								Name: v
							};
							bs.addItem(newObj);
                    	}			
					}
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
			title       : 'Installation Search',
			editor      : CMDB.Installation.Edit,
			
			elementType : '{http://www.klistret.com/cmdb/ci/element/process/change}Installation',

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
					name        : 'Source', 
					mapping     : 'Element/configuration/Source/Name/$'
				},
				{
					name        : 'Destination',
					mapping     : 'Element/configuration/Destination/Name/$'
				},
				{
					name        : 'State',
					mapping     : 'Element/configuration/State/$'
				},
				{
					name        : 'Created', 
					mapping     : 'Element/createTimeStamp/$'
				},
				{
					name        : 'Updated', 
					mapping     : 'Element/updateTimeStamp/$'
				},
				{
					name        : 'Element',
					mapping     : 'Element'
				}
			],
			
			columns        : [
				{
					header      : 'Environment', 
					width       : 200, 
					sortable    : true, 
					dataIndex   : 'Source'
				},
				{
					header      : 'Application Software', 
					width       : 200, 
					sortable    : true, 
					dataIndex   : 'Destination'
				},
				{
					header      : 'State', 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'State'
				},
				{
					header      : "Created", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Created'
				},
				{
					header      : "Last Updated", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Updated'
				}
			]
		}
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Installation.Search.superclass.initComponent.apply(this, arguments);
	}
});