/*!
 * Ext JS Library 3.3.0
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.namespace('CMDB');


// Sample desktop configuration
CMDB.Desktop = new Ext.app.App({
	init :function(){
		Ext.QuickTips.init();
		
		PageBus.publish('CMDB.Desktop.rendered');
	},

	getModules : function(){
		return [
			new CMDB.CoreMenuModule(),
			new CMDB.ProcessMenuModule(),
			new CMDB.ContextMenuModule()
		];
	},

    // config for the start menu
    getStartConfig : function(){
        return {
            title: 'Unknown user',
            iconCls: 'user',
            toolItems: [{
                text: 'Settings',
                iconCls: 'settings',
                scope: this
            },'-',{
                text: 'Logout',
                iconCls: 'logout',
                scope: this
            }]
        };
    }
});



// Core CI menu (construction, building, searching)
CMDB.CoreMenuModule = Ext.extend(Ext.app.Module, {
	init : function() {
	
		this.launcher = {
            text     : 'Core',
            menu     : {
                items      : [
                	{
                    	text     : 'Service',
                    	menu     : {
                    		items     : [
                    			{
                    				text     : 'Business',
                    				disabled : true
                    			},
                    			{
                    				text     : 'Technical',
                    				disabled : true
                    			}
                    		]
                    	} // end menu Service
                    },
                    {
                    	text     : 'System',
                    	menu     : {
                    		items     : [
                    			{
                    				text     : 'Environment',
                    				menu     : {
                    					items      : [
                    						{
                    							text     : 'Create',
                    							handler  : this.openCreateWindow,
                    							scope    : this,
                    							config   : CMDB.Environment.Edit
                    						},
                    						{
                    							text     : 'Search',
                    							handler  : this.openSearchWindow,
                    							scope    : this,
                    							config   : CMDB.Environment.Search
                    						}
                    					]
                    				}
                    			},
                    			'-',
                    			{
                    				text     : 'Application system'
                    			},
                    			{
                    				text     : 'Computer system'
                    			}
                    		]
                    	}
                    },
                    {
                    	text     : 'Component'
                    }
                ]
			}// end menu Core
		}; // end launcher
	}, // end init
	
	
	openCreateWindow : function(src) {
		var desktop = this.app.getDesktop();
       
        win = desktop.createWindow({desktop:desktop},src.config);
        win.show();
	},
	
	openSearchWindow : function(src) {
		var desktop = this.app.getDesktop();
        var win = desktop.getWindow(src.config.id);
        
        if(!win){
            win = desktop.createWindow({desktop:desktop},src.config);
        }
        win.show();
	}
});



// Process menu
CMDB.ProcessMenuModule = Ext.extend(Ext.app.Module, {
	init : function() {
	
		this.launcher = {
            text     : 'Process',
            menu     : {
                items      : [
                	{
                		text     : 'Change'
                	},
                	{
                		text     : 'Problem',
                		disabled : true
                	},
                	{
                		text     : 'Incident',
                		disabled : true
                	}
                ]
			}// end menu Process
		}; // end launcher
	} // end init
});



// Business context menu
CMDB.ContextMenuModule = Ext.extend(Ext.app.Module, {
	init : function() {
	
		this.launcher = {
            text     : 'Business',
            menu     : {
                items      : [
                	{
                		text     : 'Organization'
                	},
                	{
                		text     : 'Planning',
                		menu     : {
                    		items     : [
                    			{
                    				text     : 'Software Lifecycle',
                    				disabled : true
                    			},
                    			{
                    				text     : 'Timeframes',
                    				disabled : true
                    			}
                    		]
                    	} // end menu Planing
                	},
                	{
                		text     : 'Module',
                		disabled : true
                	}
                ]
			}// end menu Process
		}; // end launcher
	} // end init
});



/**
 http://erichauser.net/2007/11/07/more-wcf-json-and-extjs/
*/
CMDB.JsonReader = Ext.extend(Ext.data.JsonReader, {

	wrapped: true,
	
	rewriteProperties : function(obj) {
		if (typeof obj !== "object") return obj;
		for (var prop in obj) {
			if (obj.hasOwnProperty(prop)) {
				obj[prop.replace(/\./g, ":")] = this.rewriteProperties(obj[prop]);
				if (prop.indexOf(".") > -1) {
					delete obj[prop];
				}
			}
		}
		return obj;
	},

	read : function(response){
		var json = response.responseText;
		var o = eval("("+json+")");

  		if(!o) {
			throw {message: "JsonReader.read: Json object not found"};
		}
		
		var data = response.status == '200' ? {total: o.length, successful: true, rows: o} : {total: 0, successful: false, rows: []};

		return CMDB.JsonReader.superclass.readRecords.call(this, data);
	},
	
	createAccessor : function(){
        return function(expr) {
            if(Ext.isEmpty(expr)){
                return Ext.emptyFn;
            }
            
            if(Ext.isFunction(expr)){
                return expr;
            }
            
            return function(obj){
            	var parts = (expr || '').split('/'),
              		result = obj,
					part;
					
				while (parts.length > 0 && result) {
					part = parts.shift();
					result = result[part];
          		}
          		
				return result;
            };
        };
    }()
});


/**
 * Test-only
*/
CMDB.CategoryStore = new Ext.data.ArrayStore({
	fields       : ['shortName', 'name', 'description'],
    data         : [
        ['QA', 'Quality Assurance', 'Quality assurance is ....'],
        ['Prod', 'Production', 'Production ownership is the ....']
    ]
});


/**
 * Test-only
*/
CMDB.OwnershipStore = new Ext.data.ArrayStore({
	fields       : ['shortName', 'name', 'description'],
    data         : [
        ['ITA', 'ITA', 'Development ....'],
        ['ITP', 'ITP', 'Production ownership is the ....'],
        ['ITT', 'ITT', 'Service ....']
    ]
});