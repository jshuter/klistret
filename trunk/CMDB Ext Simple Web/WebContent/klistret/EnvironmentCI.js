/**
 * Necessary namespaces
*/
Ext.namespace('CMDB.Environment');
Ext.namespace('CMDB.EnvironmentType');


/**
 * Test-only
*/
CMDB.Environment.CategoryStore = new Ext.data.ArrayStore({
	fields       : ['shortName', 'name', 'description'],
    data         : [
        ['QA', 'Quality Assurance', 'Quality assurance is ....'],
        ['Prod', 'Production', 'Production ownership is the ....']
    ]
});


/**
 * Test-only
*/
CMDB.Environment.OwnershipStore = new Ext.data.ArrayStore({
	fields       : ['shortName', 'name', 'description'],
    data         : [
        ['ITA', 'ITA', 'Development ....'],
        ['ITP', 'ITP', 'Production ownership is the ....'],
        ['ITT', 'ITT', 'Service ....']
    ]
});


/**
 * Test-only
*/
CMDB.EnvironmentType.Empty = {
	"com.klistret.cmdb.ci.pojo.id" : 1,
	"com.klistret.cmdb.ci.pojo.name" : "{http://www.klistret.com/cmdb/ci/element/logical/collection}Environment",
	"com.klistret.cmdb.ci.pojo.fromTimeStamp" : "2009-08-05T11:20:12.471+02:00",
	"com.klistret.cmdb.ci.pojo.createTimeStamp" : "2009-08-05T11:20:12.471+02:00",
	"com.klistret.cmdb.ci.pojo.updateTimeStamp" : "2009-08-05T11:20:12.471+02:00"
};


/**
 * Configuration defining the search window containing a border
 * layout with a center (critria) and eastern (help) panel.
*/
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
	
	// Child configuration items
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
					
					/**
					 * Each item has an "exprssion" property that is a XPath criteria
					*/
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
							
							expression           : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"{0}\" cast as xs:dateTime]'
						},
						{
							xtype                : 'datefield',
							fieldLabel           : 'Created before',
							format               : 'Y-m-d',
							
							expression           : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp lt \"{0}\" cast as xs:dateTime]'
						},
						{
							xtype                : 'combo',
							fieldLabel           : 'Category',
							store                : CMDB.Environment.CategoryStore,
							displayField         : 'name',
							mode                 : 'local',
                            forceSelection       : true,
                            
                            expression           : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:configuration[@Watermark=\"{0}\"]'
						}
					]
				}
			]
		}
	],
	
	/**
	 * Search method that creates a results window if not present
	 * and applies the criteria (expression) properties to the 
	 * Store via the HttpProxy.
	*/
	doSearch : function(desktop) {
		/**
		 * Find the results window, create if not present
		*/
        var win = desktop.getWindow('EnvironmentSearchResults');
        if (!win) {
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
    		
    		ds.on(
				'loadexception',
				PageBus.publish.createDelegate(
					PageBus,
					[
						'CMDB.Search.Exception',
						{
							state        : 'Error', 
							elementType  : 'Environment'
						}
					],
					0
				)
			);
    	
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
				},
				
				bbar: new Ext.PagingToolbar({
            		pageSize      : 20,
					store         : ds,
            		displayInfo   : true,
            		displayMsg    : 'Displaying rows {0} - {1} of {2}',
            		emptyMsg      : 'No rows to display',
            		
            		doLoad        : function(start){
            			var o = {}, pn = this.getParams();
        				o[pn.start] = start;
        				o[pn.limit] = this.pageSize;
            			
            			if(this.fireEvent('beforechange', this, o) !== false){
            				this.store.load({
            					params   : 'start='+start+'&limit='+this.pageSize+'&'+this.store.expressions
            				});
        				}
            		},
            		
            		items:[
                		'-', 
                		{
                			xtype          : 'button',
                			text           : 'Delete',
                			cls            : 'x-btn-text-icon remove',
                			handler        : function(b, e) {
                				var grid = b.findParentByType('grid');
                				
                				var selected = grid.getSelectionModel().getSelected();
                				
                				if (selected) {
                					Ext.Ajax.defaultHeaders = {
 										'Accept'        : 'application/json,application/xml,text/html',
 										'Content-Type'  : 'application/json'
									};
		
									Ext.Ajax.request({
										url           : 'http://sadbmatrix2:55167/CMDB/resteasy/element/'+selected.get('Id'),
			
										method        : 'DELETE',
			
										scope         : grid,
										success       : function ( result, request ) {},
										failure       : function ( result, request ) {}
									});
                				}
                			}
                		}
            		]
        		})
   		 	});
    	
  	  		win = desktop.createWindow({
    			id           : 'EnvironmentSearchResults',
    			title        : 'Search results - Environment',
    		
    			width        : 740,
    			height       : 480,
    		
    			iconCls      : 'icon-grid',
    		
   		 		layout       : 'fit',
    			items        : grid 
    		});
		}
    	win.show();
    	
    	/**
    	 * Get underlying form and loop through item expressions
    	*/
    	var formPanel = this.findByType('form')[0];
		var form = formPanel.getForm();
		
		var expressions;	
		form.items.each(function(item) {
			if (!Ext.isEmpty(item.getValue())) {
				// Dates specially handled, should a converter function to each item instead
				var expression = Ext.isDate(item.getValue()) ? String.format(item.expression, item.getValue().format('Y-m-d\\TH:i:s.uP')) : String.format(item.expression, item.getValue());
				expressions = !expressions ? Ext.urlEncode({expressions : expression}) : expressions + '&' + Ext.urlEncode({expressions : expression});
			}
        });
        
        // Only active elements
        var activeOnly = 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]';
        expressions = !expressions ? Ext.urlEncode({expressions : activeOnly}) : expressions + '&' + Ext.urlEncode({expressions : activeOnly});
        
        /**
         * Service must return an empty array to denote no results (null does nothing)
        */
    	if (expressions) {
    		var winGrid = win.findByType('grid')[0];
    		
    		winGrid.getStore().expressions = expressions;
    		winGrid.getStore().load({
    			params   : 'start=0&limit=20&'+expressions
    		});
    	}
	},
	
	/**
	 Necessary for the fbar items to align properly
	*/
	buttonAlign : 'left',
	
	// Buttons
	fbar        : [
		{
			xtype        : 'tbtext',
			ref          : '../StatusText'
		},
		{
			xtype        : 'tbfill'
		},
		{
			xtype        : 'button',
			text         : 'Search',
			handler      : function(b, e) {
				var win = b.findParentByType('window');
				win.doSearch(this.CMDB.Desktop.desktop);
			},
			scope        : this  // Scope is defaults to the Window (viewport)
		}
	]
};

/**
 * Configuration for editor window
*/
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
	
	/**
	 * Accordian forms (general, ...)
	*/
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
			
			/**
			 * Each item has a elementMapping if the form field is an 
			 * element or attribute to the CIs configuration XML
			*/				
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
					store                : CMDB.Environment.CategoryStore,
					displayField         : 'name',
					mode                 : 'local',
					forceSelection       : true,
					elementMapping       : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.configuration/@Watermark'
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
	
	/**
	 * Load forms with data
	*/
	doLoad : function() {
	},
	
	/**
	 * Save form data to element plus relations 
	*/
	doSave : function() {
		/**
		 * Create a CI element if not present
		*/		
		if (!this.element || !this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"]) {
			this.element = {
				"com.klistret.cmdb.ci.pojo.Element" : {
					"com.klistret.cmdb.ci.pojo.fromTimeStamp"     : new Date(),
					"com.klistret.cmdb.ci.pojo.createTimeStamp"   : new Date(),
					"com.klistret.cmdb.ci.pojo.updateTimeStamp"   : new Date(),
					"com.klistret.cmdb.ci.pojo.type"              : CMDB.EnvironmentType.Empty,
					"com.klistret.cmdb.ci.pojo.configuration"     : {
						"@www.w3.org.2001.XMLSchema-instance.type"       : "com.klistret.cmdb.ci.element.logical.collection:Environment",
						"@Watermark"                                     : "whatever"
					}
				}
			};
		}
		
		/**
		 * Loop through the forms and pick out data to the CI element,relations
		*/
		Ext.each(this.findByType('form'), function(formPanel) {
			var form = formPanel.getForm();
			
			// CI element data only based of elementMapping expressions
			form.items.each(function(item) {
				if (!Ext.isEmpty(item.getValue()) && item.elementMapping) {
					var parts = (item.elementMapping || '').split('/'),
						prop = this.element,
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
        	}, this); // set scope to this configuration
        	
        	// TO-DO: relation mappings
        }, this);  // set scope to this configuration
        
        /**
         * Follow-up code
        */
        this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.configuration"]["com.klistret.cmdb.ci.commons.Name"] = this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.name"];
		
		
		/**
		 * First mask the window then make a request to either create or
		 * update the element
 		*/
		this.mask.show();
		
		Ext.Ajax.defaultHeaders = {
 			'Accept'        : 'application/json,application/xml,text/html',
 			'Content-Type'  : 'application/json'
		};
		
		Ext.Ajax.request({
			url           : 'http://sadbmatrix2:55167/CMDB/resteasy/element',
			
			method        : !this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"] ? 'POST' : 'PUT',

			jsonData      : Ext.encode(this.element),
			
			scope         : this,
			success       : function ( result, request ) {
				this.element = Ext.util.JSON.decode(result.responseText);
                
                this.mask.hide();
                this.StatusText.setText('Succesfully saved ' + new Date().format('g:i:s A'));
			},
			failure       : function ( result, request ) {
				var jsonData = Ext.util.JSON.decode(result.responseText);
				
				this.mask.hide();
				this.StatusText.setText('Failed saving data');
			}
		});
		
	},
	
	/**
	 Necessary for the fbar items to align properly
	*/
	buttonAlign: 'left',
	
	/**
	 * Gives status text to the left and buttons to the right
	*/
	fbar          : [
		{
			xtype        : 'tbtext',
			text         : !this.element ? 'CI is unsaved' : 'Last updated ' + this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.updateTimeStamp"],
			
			ref          : '../StatusText'
		},
		{
			xtype        : 'tbfill'
		},
		{
		    xtype        : 'button',
			text         : 'Save',
			handler      : function(b, e) {
				var win = b.findParentByType('window');
				
				if (!win.mask) {
					win.mask = new Ext.LoadMask(win.getEl(), {msg:'Saving. Please wait...'});
				}
				
				win.doSave();
			},
			scope        : this // Scope is defaults to the Window (viewport)
		}
	]
};