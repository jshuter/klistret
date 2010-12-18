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
			new CMDB.CreateMenuModule(),
			new CMDB.SearchMenuModule()
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


// Create menu
CMDB.CreateMenuModule = Ext.extend(Ext.app.Module, {

	// Initialize module
	init : function() {
	
		this.launcher = {
            text: 'Create',
            iconCls: 'tabs',
            handler: function() {
				return false;
			},
			menu: {
                items: [
					{
						text        : 'Environment',
                    	handler     : this.createWindow,
                    	scope       : this,
                    	config      : CMDB.Environment.Edit,
                    	windowTitle : 'Environment Create'
					},
					'-',
					{
                    	text: 'Application',
                    	handler: function(src) {},
                    	scope: this
                    },
					{
                    	text: 'Software Package',
                    	handler: function(src) {},
                    	scope: this
                    },
					'-',
					{
                    	text: 'Release',
                    	handler: function(src) {},
                    	scope: this
                	}
				] // end items
            } // end menu
		}; // end launcher
	} , // end init
	
	createWindow : function(src) {
		var desktop = this.app.getDesktop();
        
        win = desktop.createWindow(
           	Ext.applyIf(
           		{
           			title   : src.windowTitle
           		},
           		src.config)
          );  // end create window

        win.show();
	}
});


// Search menu
CMDB.SearchMenuModule = Ext.extend(Ext.app.Module, {

	// Initialize module
	init : function() {
	
		this.launcher = {
            text: 'Search',
            iconCls: 'tabs',
            handler: function() {
				return false;
			},
			menu: {
                items: [
					{
						text         : 'Environment',
                    	handler      : this.createWindow,
                    	scope        : this,
                    	config       : CMDB.Environment.Search,
                    	windowId     : 'EnvironmentSearch' 
					}
					,
					'-',
					{
                    	text: 'Application',
                    	handler: function(src) {},
                    	scope: this
                    },
					{
                    	text: 'Software Package',
                    	handler: function(src) {},
                    	scope: this
                    },
					'-',
					{
                    	text: 'Release',
                    	handler: function(src) {},
                    	scope: this
                	}
				] // end items
            } // end menu
		}; // end launcher
	}, // end init
	
	createWindow : function(src) {
		var desktop = this.app.getDesktop();
        var win = desktop.getWindow(src.windowId);
        
        if(!win){
            win = desktop.createWindow(
            	Ext.applyIf(
            		{
            			id : src.windowId
            		},
            		src.config)
            );  // end create window
        }
        win.show();
	}
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