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
            },
            '-',
            {
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
                    				text     : 'Application system',
                    				disabled : true
                    			},
                    			{
                    				text     : 'Computer system',
                    				disabled : true
                    			},
                    			{
                    				text     : 'Application',
                    				menu     : {
                    					items      : [
                    						{
                    							text     : 'Create',
                    							handler  : this.openCreateWindow,
                    							scope    : this,
                    							config   : CMDB.Application.Edit
                    						},
                    						{
                    							text     : 'Search',
                    							handler  : this.openSearchWindow,
                    							scope    : this,
                    							config   : CMDB.Application.Search
                    						}
                    					]
                    				}
                    			}
                    		]
                    	}
                    },
                    {
                    	text     : 'Component',
                    	menu     : {
                    		items     : [
                    			{
                    				text     : 'Software',
                    				menu     : {
                    					items      : [
                    						{
                    							text     : 'Create',
                    							handler  : this.openCreateWindow,
                    							scope    : this,
                    							config   : CMDB.Software.Edit
                    						},
                    						{
                    							text     : 'Search',
                    							handler  : this.openSearchWindow,
                    							scope    : this,
                    							config   : CMDB.Software.Search
                    						}
                    					]
                    				}
                    			}
                    		]
                    	}
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

        win = desktop.createWindow({desktop:desktop},src.config);
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
                		text     : 'Change',
                		menu     : {
                    		items     : [
                    			{
                    				text     : 'Software Installation',
                    				menu     : {
                    					items      : [
                    						{
                    							text     : 'Create',
                    							handler  : this.openCreateWindow,
                    							scope    : this,
                    							config   : CMDB.SoftwareInstallation.Edit
                    						},
                    						{
                    							text     : 'Search',
                    							handler  : this.openSearchWindow,
                    							scope    : this,
                    							config   : CMDB.SoftwareInstallation.Search
                    						}
                    					]
                    				}
                    			}
                    		]
                    	}
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
	}, // end init
	
	openCreateWindow : function(src) {
		var desktop = this.app.getDesktop();
       
        win = desktop.createWindow({desktop:desktop},src.config);
        win.show();
	},
	
	openSearchWindow : function(src) {
		var desktop = this.app.getDesktop();

        win = desktop.createWindow({desktop:desktop},src.config);
        win.show();
	}
});



// Business context menu
CMDB.ContextMenuModule = Ext.extend(Ext.app.Module, {
	init : function() {
	
		this.launcher = {
            text     : 'Context',
            menu     : {
                items      : [
                	{
                		text    : 'Collections',
                		menu    : {
                			items     : [
                				{
                					text     : 'Organization',
                					menu     : {
										items      : [
											{
												text     : 'Create',
												handler  : this.openCreateWindow,
												scope    : this,
												config   : CMDB.Organization.Edit
											},
											{
												text     : 'Search',
												handler  : this.openSearchWindow,
												scope    : this,
												config   : CMDB.Organization.Search
											}
										]
									}
                				},
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
                				{
                					text     : 'Module',
                					menu     : {
										items      : [
											{
												text     : 'Create',
												handler  : this.openCreateWindow,
												scope    : this,
												config   : CMDB.Module.Edit
											},
											{
												text     : 'Search',
												handler  : this.openSearchWindow,
												scope    : this,
												config   : CMDB.Module.Search
											}
										]
									}
                				}
                				,
                				{
                					text     : 'Publication Type',
                					menu     : {
										items      : [
											{
												text     : 'Create',
												handler  : this.openCreateWindow,
												scope    : this,
												config   : CMDB.PublicationType.Edit
											},
											{
												text     : 'Search',
												handler  : this.openSearchWindow,
												scope    : this,
												config   : CMDB.PublicationType.Search
											}
										]
									}
                				}
                			]
                		} // end menu Collections
                	},
                	{
                		text     : 'Planning',
                		menu     : {
                    		items     : [
                    			{
                    				text     : 'Software Lifecycle',
                    				menu     : {
										items      : [
											{
												text     : 'Create',
												handler  : this.openCreateWindow,
												scope    : this,
												config   : CMDB.SoftwareLifecycle.Edit
											},
											{
												text     : 'Search',
												handler  : this.openSearchWindow,
												scope    : this,
												config   : CMDB.SoftwareLifecycle.Search
											}
										]
									}
                    			},
                    			{
                    				text     : 'Timeframe',
                    				menu     : {
										items      : [
											{
												text     : 'Create',
												handler  : this.openCreateWindow,
												scope    : this,
												config   : CMDB.Timeframe.Edit
											},
											{
												text     : 'Search',
												handler  : this.openSearchWindow,
												scope    : this,
												config   : CMDB.Timeframe.Search
											}
										]
									}
                    			}
                    		]
                    	} // end menu Planing
                	}
                ]
			}// end menu Process
		}; // end launcher
	}, // end init
	
	openCreateWindow : function(src) {
		var desktop = this.app.getDesktop();
       
        win = desktop.createWindow({desktop:desktop},src.config);
        win.show();
	},
	
	openSearchWindow : function(src) {
		var desktop = this.app.getDesktop();

        win = desktop.createWindow({desktop:desktop},src.config);
        win.show();
	}
});
