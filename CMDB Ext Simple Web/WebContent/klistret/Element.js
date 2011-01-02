/**
 * Name-spaces
*/
Ext.namespace('CMDB.Element');


Ext.Element.SearchParameterPlugin = (function() {

	return {

		init       : function(item) {
		
			Ext.apply(item, {
				elementdata      : true,
				
				getParameter     : function() {
					if (Ext.isEmpty(this.getValue())) return null;
				
					if (this.getXType() == 'textfield') {
						return Ext.urlEncode({expressions : String.format(this.expression, this.getValue().replace(/\*/g, '%'))});
					}
					
					if (this.getXType() == 'datefield') {
						return Ext.urlEncode({expressions : String.format(this.expression, this.getValue().format('Y-m-d\\TH:i:s.uP'))});
					}
				
					return null;
				}
			});
		}
	};
});


Ext.Element.EditParameterPlugin = (function() {

	return {

		init       : function(item) {
		
			Ext.apply(item, {
				elementdata      : true,
				
				extract         : function(element) {
					this.setting(element, this.mapping);
				},
				
				insert           : function(element) {
					this.setValue(this.getting(element, this.mapping));
				},
				
				getting          : function(obj, expr) {
					var parts = (expr || '').split('/'),
						result = obj,
						part;
					
					while (parts.length > 0 && result) {
						part = parts.shift();
						result = result[part];
					}
          		
					return result;
				},
				
				setting          : function(obj, expr) {
					var parts = (expr || '').split('/'),
						prop = obj,
						part;
                                
					part = parts.shift();   
					while (parts.length > 0) {
						if (!prop.hasOwnProperty(part)) {
							prop[part] = {};
						}
						prop = prop[part];
						part = parts.shift(); 
					}
                        
					prop[part] = this.getValue();
				}
			});
		}
	};
});



/**
 * General Element editor
*/
CMDB.Element.Edit = Ext.extend(Ext.Window, {
	title          : 'Element Editor',
	
	dataProperty   : 'citype',
	dataValue      : true,
	
	height         : 450,
	width          : 600,
	
	buttonAlign    : 'left',
	
	layoutConfig   : {
		animate          : false
	},
	
	fbar           : [
		{
			xtype        : 'tbtext',
			ref          : '../Status'
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

	/**
	 * Initialize component prior to rendering (settings/events)
	*/
	initComponent  : function() {
		CMDB.Element.Edit.superclass.initComponent.apply(this, arguments);
		
		this.addEvents(
			'beforesave',
			
			'beforeload',
			
			'beforedelete',
			
			'aftersave',
			
			'afterload',
			
			'afterdelete',
			
			'afterinsertion',
			
			'afterextraction',
			
			'requestfailure'
		);
	},
	
	/**
	 * Adjust component after child elements are rendered 
	*/
	onRender       : function() {
		// Add Delete subscription
		this.ElementDeleteSubscribeId = PageBus.subscribe(
			'CMDB.Element.Delete', 
			this, 
			function(subj, msg, data) {
				if (msg.state == 'success' && this.element && this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"] == msg.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"]) {
					this.close();
				}
			}, 
			null
		);
		
		// Add Save subscription
		this.ElementDeleteSubscribeId = PageBus.subscribe(
			'CMDB.Element.Save', 
			this, 
			function(subj, msg, data) {
				if (msg.state == 'success' && this.element && this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"] == msg.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"]) {
					this.element = msg.element;
					this.doLoad();
				}
			}, 
			null
		);
		
		// Handle fbar events
		this.Save.on('click', this.doSave, this);
		this.Delete.on('click', this.doDelete, this);

	
		// Parent code
		CMDB.Element.Edit.superclass.onRender.apply(this, arguments);

		
		// Masks
		this.updateMask = new Ext.LoadMask(
			this.getEl(), 
			{
				msg      : 'Sending. Please wait...'
			}
		)
		
		// Load element
		this.doLoad();
	},
	
	/**
	 * Prior to destroying destroy child Ext objects
	*/
	beforeDestroy  : function(){
		if (this.rendered) {
			Ext.destroy(
				this.updateMask
			);
		}
	
		CMDB.Element.Edit.superclass.beforeDestroy.apply(this, arguments);
	},
	
	/**
	 * Prior to destroying clean up
	*/
	onDestroy      : function() {
		// Remove event subscriptions
		PageBus.unsubscribe(this.ElementDeleteSubscribeId);
	
		CMDB.Element.Edit.superclass.onDestroy.apply(this, arguments);
	},
	
	/**
	 * Load of element data by first calling the insertion method
	 * that gets data from the element and puts it into the form 
	 * fields
 	*/
	doLoad           : function() {
		if(this.fireEvent('beforeload', this) !== false){
			this.loading();
		}
	},
	
	loading          : function() {
		if (this.element && this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"]) {
			this.insertion();
		}
		
		this.fireEvent('afterload', this);
	},
	
	/**
	 * Saves element by first calling the extraction method that gets
	 * data from the form fields and updates the element
	*/
	doSave          : function() {
		if(this.fireEvent('beforesave', this) !== false){
			this.saving();
		}
	},
	
	saving          : function() {
		if (this.element) {
			this.updateMask.show();
						
			this.extraction();
			
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
				
					PageBus.publish(	
						'CMDB.Element.Save', 
						{
							state         : 'success', 
							element       : this.element 
						}
					);
					
					this.updateMask.hide();
					this.Status.setText("Successfully saved.");
					this.fireEvent('aftersave', this);
				},
				failure       : function ( result, request ) {
					this.updateMask.hide();
					this.Status.setText("Failed saving.");
					this.fireEvent('requestfailure', this, result);
				}
			});
		}
	},
	
	
	/**
	 * Delete the element by id
	*/
	doDelete         : function() {
		if(this.fireEvent('beforedelete', this) !== false){
			this.deleting();
		}
	},
	
	
	deleting         : function() {
		if (this.element && this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"]) {
			this.updateMask.show();
			
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
				
					PageBus.publish(	
						'CMDB.Element.Delete', 
						{
							state         : 'success', 
							element       : this.element 
						}
					);
					
					this.updateMask.hide();
					this.fireEvent('afterdelete', this);
				},
				failure       : function ( result, request ) {
					this.updateMask.hide();
					this.Status.setText("Failed deleting.");
					this.fireEvent('requestfailure', this, result);
				}
			});
		}
	},
	
	extraction       : function() {
		var element = this.element, fields = this.find('elementdata', true);			
		Ext.each(fields, function(field) {
			field.extract(element);
		});
		
		element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.configuration"]["com.klistret.cmdb.ci.commons.Name"] = element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.name"]; 
		
		this.fireEvent('afterextraction', this);
	},
	
	insertion        : function() {
		var element = this.element, fields = this.find('elementdata', true);			
		Ext.each(fields, function(field) {
			field.insert(element);
		});
		
		this.fireEvent('afterinsertion', this);
	}       
});



/**
 * General Element search window
*/
CMDB.Element.Search = Ext.extend(Ext.Window, {
	title          : 'Element Search',
	
	height         : 450,
	width          : 600,
	
	layout         : 'fit',
	
	buttonAlign    : 'left',
	
	fbar           : [
		{
			xtype        : 'tbtext',
			ref          : '../Status'
		},
		{
			xtype        : 'tbfill'
		},
		{
		    xtype        : 'button',
		    ref          : '../Search',
			text         : 'Search'
		}
	],
	
	start          : 0,
	limit          : 20,
	
	initComponent  : function() {
		CMDB.Element.Search.superclass.initComponent.apply(this, arguments);
		
		this.addEvents(
			'beforesearch',
			
			'aftersearch'
		);
	},
	
	onRender       : function() {
		// Handle fbar events
		this.Search.on('click', this.doSearch, this);
	
		CMDB.Element.Search.superclass.onRender.apply(this, arguments);
	},
	
	onDestroy      : function() {
		CMDB.Element.Search.superclass.onDestroy.apply(this, arguments);
	},
	
	beforeSearch   : Ext.emptyFn,
	
	afterSearch    : Ext.emptyFn,
	
	doSearch       : function() {
		var initialized, criteria = this.find('elementdata', true);			
		Ext.each(criteria, function(criterion) {
			var parameter = criterion.getParameter();
			
			if (parameter) {
				initialized = !initialized ? parameter : initialized + "&" + parameter;
			}
		});

		this.expressions = initialized;	
		if(this.expressions && this.fireEvent('beforesearch', this) !== false) {
			this.searching();
		}
	},
	
	searching      : function() {
		win = this.desktop.createWindow(
			{
				desktop      : this.desktop,
				fields       : this.fields,
				columns      : this.columns,
				editor       : this.editor,
				
				title        : 'Results - ' + this.title
			},
			CMDB.Element.Results
		);

		win.show();
		win.Grid.getStore().expressions = this.expressions;
		win.Grid.getStore().load({
			params   : 'start=' + this.start + '&limit=' + this.limit+'&'+this.expressions
		});
	}
});



/**
 * General Element results window
*/
CMDB.Element.Results = Ext.extend(Ext.Window, {
	title          : 'Search Results',
	
	height         : 450,
	width          : 600,
	
	layout         : 'fit',
	iconCls        : 'icon-grid',
	
	initComponent  : function() {
		var fields = this.fields || [];
		var columns = this.columns || [];
		
		var reader = new CMDB.JsonReader({
			totalProperty       : 'total',
    		successProperty     : 'successful',
    		idProperty          : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.id',
    		root                : 'rows',
			fields              : fields
		});
		
		var proxy = new Ext.data.HttpProxy({
			url            : 'http://sadbmatrix2:55167/CMDB/resteasy/element',
			method         : 'GET',
					
			headers        : {
				'Accept'          : 'application/json,application/xml,text/html',
				'Content-Type'    : 'application/json'
			}
        });
		
		var store = new Ext.data.Store({
			proxy         : proxy,
        	reader        : reader
        });
        
        var grid = new Ext.grid.GridPanel({
        	ref           : 'Grid',
        
        	border        : false,
    		store         : store,
    		columns       : columns,
			loadMask      : true,
			
			viewConfig    : {
				forceFit       : true
			},
			
			bbar: new Ext.PagingToolbar({
				pageSize       : 20,
				store          : store,
            	displayInfo    : true,
            	displayMsg     : 'Displaying rows {0} - {1} of {2}',
            	emptyMsg       : 'No rows to display',
            		
            	// Override private doLoad method in Ext.PagingToolbar class
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
                		ref            : 'Delete',
                		text           : 'Delete',
                		handler        : this.doDelete,
                		scope          : this
                	},
                	'-',
                	{
						xtype        : 'tbtext',
						ref          : 'Status'
					}
            	]
            })
        });
        
        grid.on('rowdblclick', this.doOpen, this);
		
		var config = {
			items      : grid
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Element.Results.superclass.initComponent.apply(this, arguments);
	},
	
	onRender       : function() {
		// Add Delete subscription
		this.ElementDeleteSubscribeId = PageBus.subscribe(
			'CMDB.Element.Delete', 
			this, 
			function(subj, msg, data) {
				if (msg.state == 'success' && this.element) {
					var record = this.Grid.store.getById(this.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"]);
					
					if (record) {
						this.Grid.store.remove(record);
					}	
				}
			}, 
			null
		);
		
		this.ElementSaveSubscribeId = PageBus.subscribe(
			'CMDB.Element.Save', 
			this, 
			function(subj, msg, data) {
				if (msg.state == 'success') {
					var record = this.Grid.store.getById(msg.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"]);
					
					if (record) {
						var other = new this.Grid.store.recordType(msg.element, msg.element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.id"]);
						var index = this.Grid.store.indexOf(record);
						
						this.Grid.store.remove(record);
						this.Grid.store.insert(index, other);
					}	
				}
			}, 
			null
		);
	
		CMDB.Element.Results.superclass.onRender.apply(this, arguments);
	},
	
	onDestroy      : function() {
		// Remove event subscriptions
		PageBus.unsubscribe(this.ElementDeleteSubscribeId);
		
		CMDB.Element.Results.superclass.onDestroy.apply(this, arguments);
	},
	
	doDelete       : function() {
		var records = this.Grid.getSelectionModel().getSelections();
		
		Ext.each(records, function(record) {
			Ext.Ajax.request({
				url           : 'http://sadbmatrix2:55167/CMDB/resteasy/element/'+record.id,
				method        : 'DELETE',
							
				headers        : {
					'Accept'        : 'application/json,application/xml,text/html',
					'Content-Type'  : 'application/json'
				},
			
				scope         : this,
			
				success       : function ( result, request ) {
					this.element = Ext.util.JSON.decode(result.responseText);
				
					PageBus.publish(	
						'CMDB.Element.Delete', 
						{
							state         : 'success', 
							element       : this.element 
						}
					);
					
					var bbar = this.Grid.getBottomToolbar();
					bbar.Status.setText('Deletion successful');
				},
				failure       : function ( result, request ) {
					var bbar = this.Grid.getBottomToolbar();
					bbar.Status.setText('Failed deleting.');
				}
			});
		});
	},
	
	doOpen         : function(grid, index) {
		var record = grid.getStore().getAt(index);	
		var element = record.get("Element");
		
		win = this.desktop.createWindow(
			{
				element       : { 'com.klistret.cmdb.ci.pojo.Element' : element }
			},
			this.editor
		);
		
		win.show();
	}
});