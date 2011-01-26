/**
 *
 */
Ext.namespace('CMDB.Environment');
Ext.namespace('CMDB.Organization');


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
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/context',
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
						['Sandbox'],
						['POC']
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
					'html'            : 'Search for Environments'
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
					fieldLabel        : 'Tags',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration[matches(commons:Tag,\"{0}\")]'
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
					name        : 'Description', 
					mapping     : 'Element/configuration/Description/$'
				},
				{
					name        : 'Tag', 
					mapping     : 'Element/configuration/Tag/$'
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
					header      : "Tags", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Tag'
				},
				{
					header      : "Description", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Description'
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
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]'});
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[matches(pojo:name,\"Environment\")]'});
	}
});



/**
 *
 */
CMDB.Organization.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns9'  : 'http://www.klistret.com/cmdb/ci/element',
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/context',
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
				'@xsi:type' : 'ns10:Organization'
			}
		}
	},

	/**
	 *
	 */
	initComponent  : function() {
		var index = CMDB.ElementTypes.find('Name','Organization'),
			type = CMDB.ElementTypes.getAt(index).get('ElementType');
		
		this.element['Element']['type']['id']['$'] = type['id']['$'];
		this.element['Element']['type']['name']['$'] = type['name']['$'];
		
		var config = {
			title       : 'Organization Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm',
					tags        : [
						['Project'],
						['Section'],
						['Department'],
						['Activity'],
						['Team']
					]
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Organization.Edit.superclass.initComponent.apply(this, arguments);
	}
});



/**
 *
 */
CMDB.Organization.Search = Ext.extend(CMDB.Element.Search, {

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
					'html'            : 'Search for Organizations'
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
					fieldLabel        : 'Tags',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration[matches(commons:Tag,\"{0}\")]'
				}
			]
		});
	
		var config = {
			title       : 'Organization Search',
			editor      : CMDB.Organization.Edit,

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
					name        : 'Description', 
					mapping     : 'Element/configuration/Description/$'
				},
				{
					name        : 'Tag', 
					mapping     : 'Element/configuration/Tag/$'
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
					header      : "Tags", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Tag'
				},
				{
					header      : "Description", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Description'
				}
			]
		}
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Organization.Search.superclass.initComponent.apply(this, arguments);
	},
	
	
	/**
	 * Apply extra filters
	 */
	beforeSearch   : function() {
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]'});
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[matches(pojo:name,\"Organization\")]'});
	}
});
