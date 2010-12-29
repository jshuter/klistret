Ext.namespace('CMDB.Element');


/**
 * Standard template for searching elements
*/
CMDB.Element.Search = {
	width          : 640,
	
	height         : 480,
	
	layout         : 'fit',
	
	start          : 0,
	
	limit          : 20,
	
	// Tool option doesn't work with desktop manager
	
	// Listeners establish domain subscriptions and Window event handlers
	listeners      : {
		render          : function(me) {					
			// Set-up ref event handlers
			me.Search.on('click', me.doSearch, me);
		},
		
		destroy         : function(me) {
			// Unsubscribe to domain events
		}
	},
		
	
	// Necessary for fbar buttons to align correctly
	buttonAlign    : 'left',
	
	// Status text and search button in the footer
	fbar           : [
		{
			xtype            : 'tbtext',
			ref              : '../Status'  // Window.Status
		},
		{
			xtype            : 'tbfill'
		},
		{
			xtype            : 'button',
			text             : 'Search',
			ref              : '../Search' // Window.Search
		}
	],
	
	// Performs a search (connected to the Search button in the footer via the render listener)
	doSearch       : function() {
		var win = this.desktop.getWindow(this.type + 'SearchResults');
		
		if (!win) {
			// Controll configuration set by the Desktop module
			if (!this.desktop) {
				throw 'Desktop not applied to configuration';
			}
			
			
			// JSON Reader
			if (!this.fields) {
				throw 'Fields not applied to configuration';
			}		
			var reader = new CMDB.JsonReader(
				{
					totalProperty       : 'total',
    				successProperty     : 'successful',
    				idProperty          : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.id',
    				root                : 'rows',
					fields              : this.fields
				}
			);
			
			
			// Store with HttpProxy
			var store = new Ext.data.Store({
				proxy         : new Ext.data.HttpProxy({
					url            : 'http://sadbmatrix2:55167/CMDB/resteasy/element',
					method         : 'GET',
					
					headers        : {
						'Accept'          : 'application/json,application/xml,text/html',
						'Content-Type'    : 'application/json'
					}
        		}),
        	
        		reader        : reader,
        		
        		listeners     : {
        			'remove'       : function(store, record, index) {
        				Ext.Ajax.request({
							url           : 'http://sadbmatrix2:55167/CMDB/resteasy/element/'+record.get('Id'),
			
							method        : 'DELETE',
							
							headers        : {
								'Accept'        : 'application/json,application/xml,text/html',
 			    				'Content-Type'  : 'application/json'
							},
			
							scope         : grid,
							
							success       : function ( result, request ) {
								this.element = Ext.util.JSON.decode(result.responseText);
								
								PageBus.publish(	
									'CMDB.Element.Delete', 
									{
										state         : 'success', 
										type          : this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.type"]["com.klistret.cmdb.ci.pojo.name"],
										id            : this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"],
										element       : this.element 
									}
								);
								
								var bbar = this.getBottomToolbar();
								bbar.Status.setText('Deleted record(s).');
							},
							failure       : function ( result, request ) {
								this.store.rejectChanges();
								
								var bbar = this.getBottomToolbar();
								bbar.Status.setText('Failed deleting.');
							}
						});
        			}
        		}
    		});
    		
    		
    		// On-failure to load data
    		store.on('loadexception', this.loadException, this);
    		
    		
    		// Grid
    		if (!this.columns) {
    			throw 'Columns not applied to configuration';
    		}
    		var grid = new Ext.grid.GridPanel({
    			border        : false,
    				
    			store         : store,
    				
    			columns       : this.columns,
			
				loadMask      : true,
				
				desktop       : this.desktop,
				
				editor        : this.editor,
				
				viewConfig    : {
					forceFit       : true
				},
				
				// Setup domain event listeners
				listeners      : {
					render          : function(me) {
						// Subscribe to domain events
					},
		
					destroy         : function(me) {
						// Unsubscribe to domain events
					},
					
					rowdblclick     : function(me, rowIndex) {
						var record = this.store.getAt(rowIndex);
						
						var id = record.get('Payload')["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"];
						var name = record.get('Payload')["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.name"];
					
						var win = desktop.getWindow(id);
						if (!win) {
							win = desktop.createWindow(
								Ext.applyIf(
									{
										id         : id,
										title      : name
									},
									this.editor)
							);
						}
						
        				win.show();
					}
				},
				
				bbar: new Ext.PagingToolbar({
            		pageSize       : 20,
					store          : store,
            		displayInfo    : true,
            		displayMsg     : 'Displaying rows {0} - {1} of {2}',
            		emptyMsg       : 'No rows to display',
            		
            		// Override private doLoad method
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
            		
            		items          : [
                		'-', 
                		{
                			xtype          : 'button',
                			text           : 'Delete',
                			cls            : 'x-btn-text-icon remove',
                			handler        : function(b, e) {
                				var records = grid.getSelectionModel().getSelections();
                				
                				if (records) {
                					store.remove(records);
                				}
                			}
                		},
                		'-',
                		{
							xtype        : 'tbtext',
							ref          : 'Status'
						}
            		]
            	})
			});
			
			win = this.desktop.createWindow({
    			id           : this.type + 'SearchResults',
    			title        : 'Search results - ' + this.type,
    		
    			width        : 740,
    			height       : 480,
    		
    			iconCls      : 'icon-grid',
    		
   		 		layout       : 'fit',
    			items        : grid 
    		});
    	}
		win.show();
		
		
		if (!this.Form) {
			throw 'Form not applied to configuration';
		}
		var expressions;
		this.Form.getForm().items.each(function(item) {
			if ((item.getXType() != 'displayfield' && item.getXType() != 'hidden') && !Ext.isEmpty(item.getValue())) {
				var expression = String.format(item.expression, item.getValue());
				
				// Dates specially handled, should a converter function to each item instead
				if (Ext.isDate(item.getValue())) expression = String.format(item.expression, item.getValue().format('Y-m-d\\TH:i:s.uP'));
				
				// Replace * with %
				if (Ext.isString(item.getValue())) expression = String.format(item.expression, item.getValue().replace(/\*/g, '%'));
								
				expressions = !expressions ? Ext.urlEncode({expressions : expression}) : expressions + '&' + Ext.urlEncode({expressions : expression});
			}
			
			if (item.getXType() == 'hidden') {
				expressions = !expressions ? Ext.urlEncode({expressions : item.expression}) : expressions + '&' + Ext.urlEncode({expressions : item.expression});
			}
		});
		
		if (expressions) {
    		var grid = win.getComponent(0);
    		
    		grid.getStore().expressions = expressions;
    		grid.getStore().load({
    			params   : 'start=' + this.start + '&limit=' + this.limit+'&'+expressions
    		});
    	}
	},
	
	loadException    : function() {
	}
};




/**
 * Standard template for editing elements
*/
CMDB.Element.Edit = {
	width          : 640,
	
	height         : 480,
	
	layout         : 'accordion',
	
	layoutConfig   : {
		animate         : false
	},
	
	// Listeners establish domain subscriptions and Window event handlers
	listeners      : {
		render          : function(me) {
			me.mask = new Ext.LoadMask(me.getEl(), {msg:'Sending. Please wait...'})
		
			// Subscribe to domain events
			me.ElementDeleteSubscribeId = PageBus.subscribe(
				'CMDB.Element.Delete', 
				this, 
				function() {
					this.close();
				}, 
				null
			);
					
			// Set-up ref event handlers
			me.Save.on('click', me.doSave, me);
			
			me.Delete.on('click', me.doDelete, me);
		},
		
		destroy         : function(me) {
			// Unsubscribe to domain events
			PageBus.unsubscribe(me.ElementDeleteSubscribeId);
		}
	},
	
	// Necessary for the fbar items to align properly
	buttonAlign    : 'left',
	
	fbar           : [
		{
			xtype        : 'tbtext',
			ref          : '../Status',
			listeners    : {
				render         : function() {
				}
			}
		},
		{
			xtype        : 'tbfill'
		},
		{
		    xtype        : 'button',
		    ref          : '../Save',
			text         : 'Save'
		},
		{
		    xtype        : 'button',
		    ref          : '../Delete',
			text         : 'Delete',
			disabled     : true
		}
	],
	
	doLoad         : function() {
		alert('do load');
		
		this.Delete.enable();
	},
	
	doSave         : function() {
		var isValid = true;
	
		// Save element mappings
		Ext.each(this.findByType('form'), function(formPanel) {
			var form = formPanel.getForm();
			
			form.items.each(function(item) {
				if (!item.validate()) isValid = false;
			
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
			}, this);
		}, this);
		
		if (!isValid) return;
		
		// Save relation mappings
		
		// Follow up
		this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.configuration"]["com.klistret.cmdb.ci.commons.Name"] = this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.name"];
		
		/**
		 * First mask the window then make a request to either create or
		 * update the element
 		*/
		this.mask.show();
		
		Ext.Ajax.request({
			url           : 'http://sadbmatrix2:55167/CMDB/resteasy/element',
			
			method        : !this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"] ? 'POST' : 'PUT',

			headers       : {
				'Accept'        : 'application/json,application/xml,text/html',
 			    'Content-Type'  : 'application/json'
			},
			
			jsonData      : Ext.encode(this.element),
			
			scope         : this,
			
			success       : function ( result, request ) {
				this.element = Ext.util.JSON.decode(result.responseText);
                
                this.mask.hide();
                this.Status.setText('Succesfully saved ' + new Date().format('g:i:s A'));
                
				PageBus.publish(	
					'CMDB.Element.Save', 
					{
						state         : 'success', 
						type          : this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.type"]["com.klistret.cmdb.ci.pojo.name"],
						id            : this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"],
						element       : this.element 
					}
				);
				
				this.Delete.enable();
			},
			failure       : function ( result, request ) {
				var jsonData = Ext.util.JSON.decode(result.responseText);
				
				this.mask.hide();
				this.Status.setText('Failed saving.');
			}
		});
	},
	
	doDelete       : function() {
		this.mask.show();
	
		Ext.Ajax.request({
			url           : 'http://sadbmatrix2:55167/CMDB/resteasy/element/'+this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"],
			
			method        : 'DELETE',
							
			headers        : {
				'Accept'        : 'application/json,application/xml,text/html',
 			    'Content-Type'  : 'application/json'
			},
			
			scope         : this,
			
			success       : function ( result, request ) {
				this.element = Ext.util.JSON.decode(result.responseText);
				
				this.mask.hide();
				PageBus.publish(	
					'CMDB.Element.Delete', 
					{
						state         : 'success', 
						type          : this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.type"]["com.klistret.cmdb.ci.pojo.name"],
						id            : this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"],
						element       : this.element 
					}
				);
			},
			failure       : function ( result, request ) {
				this.element = Ext.util.JSON.decode(result.responseText);
				
				this.mask.hide();
				this.Status.setText('Failed deleting.');
			}
		});
	}
};
