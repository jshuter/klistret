Ext.namespace('CMDB.Environment');
Ext.namespace('CMDB.Organization');
Ext.namespace('CMDB.Module');
Ext.namespace('CMDB.SoftwareLifecycle');
Ext.namespace('CMDB.Timeframe');



/**
 * Environment Editor 
 */
CMDB.Environment.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns8'  : 'http://www.klistret.com/cmdb/ci/commons',
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
				'@xsi:type' : 'ns10:Environment',
				'ns8:Ownership' : {
					'ns8:Contact' : {}
				}
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
					helpInfo    : 'An environment is a collection of logical systems that represent an entire production, test or development IT landspace.',
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
					xtype       : 'contactForm',
					helpInfo    : 'Contact or ownership information is not required but helpful in locating responsibility for the Environment CI.  The contact name may also be an organization.'
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
 * Environment Search
 */
CMDB.Environment.Search = Ext.extend(CMDB.Element.Search, {

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
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]',
					wildcard          : '%'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Tags',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration[commons:Tag = \"{0}\"]'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Contact Name',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration/commons:Ownership/commons:Contact[matches(commons:Name,\"{0}\")]',
					wildcard          : '.*'
				}
			]
		});
	
		var config = {
			title       : 'Environment Search',
			editor      : CMDB.Environment.Edit,
			
			elementType : '{http://www.klistret.com/cmdb/ci/element/context}Environment',

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
					name        : 'Contact Name', 
					mapping     : 'Element/configuration/Ownership/Contact/Name/$'
				},
				{
					name        : 'Tag', 
					mapping     : 'Element/configuration/Tag',
					formating   : function(values) {
						var formated = '';
						
						Ext.each(
							values, 
							function(value) {
								formated = Ext.isEmpty(formated) ? value['$'] : formated + ', ' + value['$'] ;
							}
						);
						return formated;
					}
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
					header      : "Contact Name", 
					width       : 120, 
					sortable    : true, 
					dataIndex   : 'Contact Name'
				}
			]
		}
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Environment.Search.superclass.initComponent.apply(this, arguments);
	}
});



/**
 * Organization Editor
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
					helpInfo    : 'An organisation is either a company, an individual, or simply any group of people.',
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
 * Organization Search
 */
CMDB.Organization.Search = Ext.extend(CMDB.Element.Search, {

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
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]',
					wildcard          : '%'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Tags',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration[commons:Tag = \"{0}\"]'
				}
			]
		});
	
		var config = {
			title       : 'Organization Search',
			editor      : CMDB.Organization.Edit,
			
			elementType : '{http://www.klistret.com/cmdb/ci/element/context}Organization',

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
					mapping     : 'Element/configuration/Tag',
					formating   : function(values) {
						var formated = '';
						
						Ext.each(
							values, 
							function(value) {
								formated = Ext.isEmpty(formated) ? value['$'] : formated + ', ' + value['$'] ;
							}
						);
						return formated;
					}
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
	}
});



/**
 * Module Editor
 */
CMDB.Module.Edit = Ext.extend(CMDB.Element.Edit, {
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
				'@xsi:type' : 'ns10:Module'
			}
		}
	},

	/**
	 *
	 */
	initComponent  : function() {
		var index = CMDB.ElementTypes.find('Name','Module'),
			type = CMDB.ElementTypes.getAt(index).get('ElementType');
		
		this.element['Element']['type']['id']['$'] = type['id']['$'];
		this.element['Element']['type']['name']['$'] = type['name']['$'];
		
		var config = {
			title       : 'Module Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm',
					helpInfo    : 'Modules are a self-contained, reusable unit of software that, as a whole unit, follows a revision control scheme.',
					tags        : [
						['Middleware'],
						['Educational'],
						['Simulation'],
						['Content access'],
						['Media devleopment'],
						['Enterprise']
					]
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Module.Edit.superclass.initComponent.apply(this, arguments);
	}
});



/**
 * Module Search
 */
CMDB.Module.Search = Ext.extend(CMDB.Element.Search, {

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
					'html'            : 'Search for Modules'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Name',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]',
					wildcard          : '%'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Tags',
					expression        : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration[commons:Tag = \"{0}\"]'
				}
			]
		});
	
		var config = {
			title       : 'Module Search',
			editor      : CMDB.Module.Edit,
			
			elementType : '{http://www.klistret.com/cmdb/ci/element/context}Module',

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
					mapping     : 'Element/configuration/Tag',
					formating   : function(values) {
						var formated = '';
						
						Ext.each(
							values, 
							function(value) {
								formated = Ext.isEmpty(formated) ? value['$'] : formated + ', ' + value['$'] ;
							}
						);
						return formated;
					}
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
		CMDB.Module.Search.superclass.initComponent.apply(this, arguments);
	}
});



/**
 * Software Lifecyle Editor
 */
CMDB.SoftwareLifecycle.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns9'  : 'http://www.klistret.com/cmdb/ci/element',
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
					'ns11' : 'http://www.klistret.com/cmdb/ci/element/context/lifecycle',
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
				'@xsi:type' : 'ns11:SoftwareLifecycle'
			}
		}
	},
	
	initComponent  : function() {
		var index = CMDB.ElementTypes.find('Name','SoftwareLifecycle'),
			type = CMDB.ElementTypes.getAt(index).get('ElementType');
		
		this.element['Element']['type']['id']['$'] = type['id']['$'];
		this.element['Element']['type']['name']['$'] = type['name']['$'];
		
		var config = {
			title       : 'Software Lifecycle Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm',
					helpInfo    : 'The software life cycle is composed of discrete phases that describe the software\'s maturity as it advances from planning and development to release and support phases.',
					tags        : []
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.SoftwareLifecycle.Edit.superclass.initComponent.apply(this, arguments);
	}
});



/**
 * Software Lifecycle (Search)
 */
CMDB.SoftwareLifecycle.Search = Ext.extend(CMDB.Element.Search, {

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
					'html'            : 'Search for Software lifecycles'
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
			title       : 'Software Lifecycle Search',
			editor      : CMDB.SoftwareLifecycle.Edit,
			
			elementType : '{http://www.klistret.com/cmdb/ci/element/context}SoftwareLifecycle',

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
		CMDB.SoftwareLifecycle.Search.superclass.initComponent.apply(this, arguments);
	}
});



/**
 * Timeframe (Editor)
 */
CMDB.Timeframe.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns9'  : 'http://www.klistret.com/cmdb/ci/element',
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
					'ns11' : 'http://www.klistret.com/cmdb/ci/element/context',
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
				'@xsi:type' : 'ns11:Timeframe'
			}
		}
	},
	
	initComponent  : function() {
		var index = CMDB.ElementTypes.find('Name','Timeframe'),
			type = CMDB.ElementTypes.getAt(index).get('ElementType');
		
		this.element['Element']['type']['id']['$'] = type['id']['$'];
		this.element['Element']['type']['name']['$'] = type['name']['$'];
		
		var config = {
			title       : 'Software Lifecycle Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm',
					helpInfo    : 'Time frames can be both short or long term representing organizational milestones.',
					tags        : []
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Timeframe.Edit.superclass.initComponent.apply(this, arguments);
	}
});



/**
 * Timeframe (Search)
 */
CMDB.Timeframe.Search = Ext.extend(CMDB.Element.Search, {

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
					'html'            : 'Search for timeframes'
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
			title       : 'Timeframe Search',
			editor      : CMDB.Timeframe.Edit,
			
			elementType : '{http://www.klistret.com/cmdb/ci/element/context}Timeframe',

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
		CMDB.Timeframe.Search.superclass.initComponent.apply(this, arguments);
	}
});