/**
 * Necessary namespaces
*/
Ext.namespace('CMDB.Environment');
Ext.namespace('CMDB.EnvironmentType');


/**
 * Test-only
*/
CMDB.EnvironmentType.Empty = {
	"com.klistret.cmdb.ci.pojo.id" : 1,
	"com.klistret.cmdb.ci.pojo.name" : "{http://www.klistret.com/cmdb/ci/element/system}Environment",
	"com.klistret.cmdb.ci.pojo.fromTimeStamp" : "2009-08-05T11:20:12.471+02:00",
	"com.klistret.cmdb.ci.pojo.createTimeStamp" : "2009-08-05T11:20:12.471+02:00",
	"com.klistret.cmdb.ci.pojo.updateTimeStamp" : "2009-08-05T11:20:12.471+02:00"
};


CMDB.Environment.Search = Ext.apply(CMDB.Element.Search, {
	type           : 'Environment',
	
	id             : 'EnvironmentSearch',
	
	title          : 'Environment Search',

	// Form panel (criteria)
	items          : {
		xtype           : 'form',
		ref             : 'Form', // Window.Form
		
		border          : false,
	
		bodyStyle       : 'padding:10px; background-color:white;',
		baseCls         : 'x-plain',
	
		labelAlign      : 'top',
        	
		defaults        : {
			xtype            : 'textfield',
        		
			width            : 300
		},
		
		items           : [
			{
				xtype             : 'hidden',
				
				expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]'
			},
			{
				xtype             : 'hidden',
				
				expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[matches(pojo:name,\"Environment\")]'
			},
			{
				xtype             : 'displayfield',
				ref               : '../Information',  // Window.Information
			
				width             : 'auto',
				'html'            : 'Search criteria for this CI (Configuration Item)'
			},
			{
				xtype             : 'textfield',
				fieldLabel        : 'Name',
        					
				expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]'
			},
			{
				xtype             : 'datefield',
				fieldLabel        : 'Created after',
				format            : 'Y-m-d',
							
				expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"{0}\" cast as xs:dateTime]'
			},
			{
				xtype             : 'datefield',
				fieldLabel        : 'Created before',
				format            : 'Y-m-d',
							
				expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp lt \"{0}\" cast as xs:dateTime]'
			}
			
		]
	},
	
	// Reader fields
	fields         : [
		{
			name        : 'Id', 
		 	mapping     : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.id'
		 },
		{
			name        : 'Name', 
			mapping     : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.name'
		},
		{
			name        : 'Watermark', 
			mapping     : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.configuration/@Watermark'
		},
		{
			name        : 'Payload',
			mapping     : 'com.klistret.cmdb.ci.pojo.Element'
		}
	],
	
	// Grid columns
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
});


/**
*/
CMDB.Environment.Edit = Ext.apply(CMDB.Element.Edit, {
	title          : 'Environment Editing',
	
	// Default element
	element        : {
		"com.klistret.cmdb.ci.pojo.Element"  : {
			"com.klistret.cmdb.ci.pojo.fromTimeStamp"     : new Date(),
			"com.klistret.cmdb.ci.pojo.createTimeStamp"   : new Date(),
			"com.klistret.cmdb.ci.pojo.updateTimeStamp"   : new Date(),
			"com.klistret.cmdb.ci.pojo.type"              : CMDB.EnvironmentType.Empty,
					"com.klistret.cmdb.ci.pojo.configuration"     : {
						"@www.w3.org.2001.XMLSchema-instance.type"       : "com.klistret.cmdb.ci.element.system:Environment",
						"@Watermark"                                     : "Test"
					}
		}
	},
	
	// Accordian forms
	// Form panel (criteria)
	items          : [
		// General information
		{
			title       : 'General',
			
			autoScroll  : true,
			
			xtype       : 'form',
					
            border      : false,
					
			labelAlign  : 'top',
			
			bodyStyle   : 'padding:10px; background-color:white;',
			
			defaults    : {
				width             : 300
			},
			
			items       : [
				{
					xtype                : 'textfield',
					fieldLabel           : 'Name',
					allowBlank           : false,
					blankText            : 'Enter a unique environment name',
					elementMapping       : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.name'
				},
				{
					xtype                : 'combo',
					fieldLabel           : 'Category',
					store                : CMDB.CategoryStore,
					displayField         : 'name',
					mode                 : 'local',
					forceSelection       : true,
					elementMapping       : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.configuration/@Watermark'
				},
				{
					xtype                : 'combo',
					fieldLabel           : 'Ownership',
					store                : CMDB.OwnershipStore,
					displayField         : 'name',
					mode                 : 'local',
					forceSelection       : true
				}
			]
		}
	]
});