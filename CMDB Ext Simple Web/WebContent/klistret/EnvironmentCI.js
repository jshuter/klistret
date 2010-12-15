Ext.namespace('CMDB.Environment');
Ext.namespace('CMDB.EnvironmentType');


CMDB.Environment.CategoryStore = new Ext.data.ArrayStore({
	fields       : ['shortName', 'name', 'description'],
    data         : [
        ['QA', 'Quality Assurance', 'Quality assurance is ....'],
        ['Prod', 'Production', 'Production ownership is the ....']
    ]
});


CMDB.Environment.OwnershipStore = new Ext.data.ArrayStore({
	fields       : ['shortName', 'name', 'description'],
    data         : [
        ['ITA', 'ITA', 'Development ....'],
        ['ITP', 'ITP', 'Production ownership is the ....'],
        ['ITT', 'ITT', 'Service ....']
    ]
});


CMDB.EnvironmentType.Empty = {
	"com.klistret.cmdb.ci.pojo.id" : 1,
	"com.klistret.cmdb.ci.pojo.name" : "{http://www.klistret.com/cmdb/ci/element/logical/collection}Environment",
	"com.klistret.cmdb.ci.pojo.fromTimeStamp" : "2009-08-05T11:20:12.471+02:00",
	"com.klistret.cmdb.ci.pojo.createTimeStamp" : "2009-08-05T11:20:12.471+02:00",
	"com.klistret.cmdb.ci.pojo.updateTimeStamp" : "2009-08-05T11:20:12.471+02:00"
};


// Environment search configuration
CMDB.Environment.Search = {
	// Title shown
	title : "Environment Search",
	
	// Border layout
	layout : 'border',
	
	// Width
	width : 640,
	
	// Height
	height : 480,
	
	// Default styles 
	defaults :	{
		bodyStyle  : 'padding:10px; background-color:white;', 
		baseCls    : 'x-plain',
		
		padding    : 10
	},
	
	// Results
	resultsWindows : new Array(),
	
	// Children
	items : [
		// Eastern panel (help)
		{
			// layout/width
			region     : 'east',
			width      : 200,
			
			// children
			items      : {
				baseCls     : 'x-plain',
				html        : '<p style="color:#556677;">Everything inside this window is automated so some criteria fields will give suggestions just by typing the first letters of a suggestion and the results are opened in a new window</p><p style="color:#556677;">Remember the Environment CI is a collection of application infrastructure components representing production, a level of test, development, a sandbox and so forth.</p>'
			}		
		},
		
		// Central panel (criteria)
		{
			// layout
			region     : 'center',
			
			// children
			items      : [
				{
					baseCls     : 'x-plain',
					html        : '<p style="color:#556677;">Search for environment CIs through the available criteria below.</p>'
				},
				{
					xtype       : 'form',
					
					border      : false,
					
					labelStyle  : 'font-size:10; color:#556677;',
					labelAlign  : 'top',
					
					defaults    : {
						width     : 300
					},			
					
					items       : [
						{
							xtype                : 'textfield',
							fieldLabel           : 'Name',
        					
        					expression           : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]'
						},
						{
							xtype                : 'datefield',
							fieldLabel           : 'Created after',
							format               : 'Y-m-d',
							
							expression           : '{0}'
						},
						{
							xtype                : 'datefield',
							fieldLabel           : 'Created before',
							format               : 'Y-m-d',
							
							expression           : '{0}'
						},
						{
							xtype                : 'combo',
							fieldLabel           : 'Category',
							store                : CMDB.Environment.CategoryStore,
							displayField         : 'name',
							mode                 : 'local',
                            forceSelection       : true,
                            
                            expression           : '{0}'
						}
					]
				}
			]
		}
	],
	
	doSearch : function(desktop) {
		var formPanel = this.findByType('form')[0];
		var form = formPanel.getForm();
		
		var expressions = new Array();
		
		form.items.each(function(item) {
			if (!Ext.isEmpty(item.getValue())) {
				expressions[expressions.length] = String.format(item.expression, item.getValue());
			}
        });
        
		var reader = new CMDB.JsonReader(
			{
				totalProperty       : 'com.klistret.cmdb.ci.pojo.count',
    			successProperty     : 'com.klistret.cmdb.ci.pojo.successful',
    			idProperty          : 'com.klistret.cmdb.ci.pojo.id',
    			root                : 'com.klistret.cmdb.ci.pojo.elements'
			}, 
			[
				{name: 'Id', mapping: 'com.klistret.cmdb.ci.pojo.id'},
    			{name: 'Name', mapping: 'com.klistret.cmdb.ci.pojo.name'},
    			{name: 'Watermark', mapping: 'com.klistret.cmdb.ci.pojo.configuration/@Watermark'}
			]
		);
		
        
        Ext.Ajax.defaultHeaders = {
 			'Accept'        : 'application/json,application/xml,text/html',
 			'Content-Type'  : 'application/json'
		};
        
        var ds = new Ext.data.Store({
        	proxy      : new Ext.data.HttpProxy({
            	url      : 'http://sadbmatrix2:55167/CMDB/resteasy/element',
            	method   : 'GET'
        	}),
        	
        	reader     : reader 
    	});
    	
    	ds.load({
    		params   :  {
        		start    : 0,
        		limit    : 20,
        		expressions : expressions[0]
        	}
    	});
    	
    	var grid = new Ext.grid.GridPanel({
    		border       : false,
    				
    		store         : ds,
    				
    		columns       : [
    			{header: "Id", width: 40, sortable: true, dataIndex: 'Id'},
				{header: "Name", width: 120, sortable: true, dataIndex: 'Name'},
				{header: "Watermark", width: 120, sortable: true, dataIndex: 'Watermark'}
			],
			
			loadMask     : true,
    				
    		viewConfig   : {
				forceFit   : true
			}
    	});
    	
    	var win = desktop.createWindow({
    		id           : 'environmentResults'+this.resultsWindows.length,
    		title        : 'Search results (environment)',
    		
    		width        : 740,
    		height       : 480,
    		
    		iconCls      : 'icon-grid',
    		
    		layout       : 'fit',
    		items        : grid 
    	});
    	
    	this.resultsWindows[this.resultsWindows.length] = win;
    	win.show();
	},
	
	// Buttons
	buttons : [
		// Search
		{
			text         : 'Search',
			handler      : function(b, e) {
				var win = b.findParentByType('window');
				win.doSearch(this.CMDB.Desktop.desktop);
			},
			scope        : this  // Scope is defaults to the Window (viewport)
		}
	]
};

// Environment search configuration
CMDB.Environment.Edit = {
	// Title shown
	title         : "Environment Editing",
	
	// Border layout
	layout        : 'accordion',
	
	// Width
	width         : 640,
	
	// Height
	height        : 480,
	
	layoutConfig  : {
		animate : false
	},
	
	defaults      : {
		padding : 10
	},
	
	items         : [
		{
			title        : 'General',
			
			autoScroll   : true,
			
			xtype        : 'form',
					
            border       : false,
					
			labelStyle   : 'font-size:10; color:#556677;',
			labelAlign   : 'top',
			
			defaults     : {
				width     : 300
			},
							
			items       : [
				{
					xtype                : 'textfield',
					fieldLabel           : 'Name',
					allowBlank           : false,
					blankText            : 'Enter a unique environment name',
					jsonMapping          : 'com.klistret.cmdb.ci.pojo.name'
				},
				{
					xtype                : 'combo',
					fieldLabel           : 'Category',
					store                : CMDB.Environment.CategoryStore,
					displayField         : 'name',
					mode                 : 'local',
					forceSelection       : true,
					jsonMapping          : 'com.klistret.cmdb.ci.pojo.configuration/@Watermark'
				},
				{
					xtype                : 'combo',
					fieldLabel           : 'Ownership',
					store                : CMDB.Environment.OwnershipStore,
					displayField         : 'name',
					mode                 : 'local',
					forceSelection       : true
				},
				{
					xtype                : 'htmleditor',
					fieldLabel           : 'Description',
					width                : 'auto'
				}
			]
		}
	],
	
	doSave : function() {
		var formPanel = this.findByType('form')[0];
		var form = formPanel.getForm();
		
		if (!this.environment || !this.environment["com.klistret.cmdb.ci.pojo.id"]) {
			this.environment = {
				"com.klistret.cmdb.ci.pojo.type"              : CMDB.EnvironmentType.Empty,
				"com.klistret.cmdb.ci.pojo.fromTimeStamp"     : new Date(),
				"com.klistret.cmdb.ci.pojo.createTimeStamp"   : new Date(),
				"com.klistret.cmdb.ci.pojo.updateTimeStamp"   : new Date(),
				"com.klistret.cmdb.ci.pojo.configuration"     : {
					"@www.w3.org.2001.XMLSchema-instance.type"       : "com.klistret.cmdb.ci.element.logical.collection:Environment"
				}
			};
		}
		
		form.items.each(function(item) {
			if (!Ext.isEmpty(item.getValue())) {
				var parts = (item.jsonMapping || '').split('/'),
					prop = this.environment,
					part;
				
				part = parts.shift();	
				while (parts.length > 0) {
					if (!prop.hasOwnProperty(part)) {
						prop[part] = {};
					}
					prop = prop[part];
					part = parts.shift(); 
          		}
          		
          		prop[part] = item.getValue();
			}
        }, this);
		
		var dummy = "";
	},
	
	buttons       : [
		{
			text         : 'Save',
			handler      : function(b, e) {
				var win = b.findParentByType('window');
				win.doSave();
			},
			scope        : this // Scope is defaults to the Window (viewport)
		}
	]
};