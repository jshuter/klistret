/**
 * Name-spaces
*/
Ext.namespace('CMDB.Element');


/**
 *
 */
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


/**
 *
 */
Ext.Element.EditParameterPlugin = (function() {

	return {

		init       : function(item) {
		
			Ext.apply(item, {
				elementdata      : true,
				
				extract         : function(element) {
					if (this.getXType() == 'textfield' || this.getXType() == 'textarea') {
						CMDB.Badgerfish.set(element, this.mapping, this.getValue());
					}
					
					if (this.getXType() == 'propertygrid') {
						var properties = [];
						
						this.store.each(
							function(record) {
								var property = {
									"com.klistret.cmdb.ci.commons.Name"  : record.get("name"),
									"com.klistret.cmdb.ci.commons.Value" : record.get("value")
								};
								
								properties[properties.length] = property;
							},
							this
						);
						
						CMDB.Badgerfish.set(element, this.mapping, properties);
					}
				},
				
				insert           : function(element) {
					if (this.getXType() == 'textfield' || this.getXType() == 'textarea') {
						var value = CMDB.Badgerfish.get(element, this.mapping);
						this.setValue(value);
					}
					
					if (this.getXType() == 'propertygrid') {
						var properties = CMDB.Badgerfish.get(element, this.mapping);
						/**
						Ext.each(
							properties, 
							function(property) {
								this.source[property["com.klistret.cmdb.ci.commons.Name"]] = property["com.klistret.cmdb.ci.commons.Value"];
							},
							this);
						*/
					}
				}
			});
		}
	};
});


/**
 * General form panel common for all elements (stuff like
 * name, description, tags, and so forth)
*/
CMDB.Element.GeneralForm = Ext.extend(Ext.form.FormPanel, {

	initComponent  : function() {
		var config = {
			title       : 'General',
			autoScroll  : true,
			labelAlign  : 'top',
			bodyStyle   : 'padding:10px; background-color:white;',
			defaults    : {
				width             : 300
			},
			
			items       : [
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.EditParameterPlugin()],
					fieldLabel        : 'Name',
					allowBlank        : false,
					blankText         : 'Enter a unique environment name',
					mapping           : 'Element/name/$'
				},
				{
					xtype             : 'textarea',
					plugins           : [new Ext.Element.EditParameterPlugin()],
					fieldLabel        : 'Description',
					height            : 50,
					blankText         : 'Description of the Environment',
					mapping           : 'Element/configuration/Description/$'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Element.GeneralForm.superclass.initComponent.apply(this, arguments);
	},
	
	onRender       : function() {
		CMDB.Element.GeneralForm.superclass.onRender.apply(this, arguments);
	}
});
Ext.reg('generalForm', CMDB.Element.GeneralForm);



/**
 * Relation form panel common to all elements
*/
CMDB.Element.DestRelationForm = Ext.extend(Ext.form.FormPanel, {
	fields      : [
		{
			name            : 'Id',
			mapping         : 'Relation/id/$'
		},
		{
			name            : 'Type', 
			mapping         : 'Relation/type/name/$'
		},
		{
			name            : 'DestName', 
			mapping         : 'Relation/destination/name/$'
		},
		{
			name            : 'DestType',
			mapping         : 'Relation/destination/type/name/$'
		},
		{
			name            : 'Relation',
			mapping         : 'Relation'
		}
	],
	
	columns     : [
		{
			header          : 'Relationship', 
			width           : 150, 
			sortable        : true, 
			dataIndex       : 'Type',
			editor          : {
				xtype           : 'combo',
				allowBlank      : false,
				blankText       : 'Relationship field necessary',
				typeAhead       : true,
				forceSelection  : true,
				mode            : 'remote',
				
				/**
				 * Query parameter is 'name' rather than default 'query'
				*/
				queryParam      : 'name',

				/**
				 * Modify the query parameter if exists with wildcards
				*/
				listeners       : {
         			'beforequery'       : function(e) {
         				e.query = Ext.isEmpty(e.query) ? '' : '%' + e.query + '%';
         			}
   				},
   				
   				/**
   				 * Nullify the start/limit parameters
   				*/
				store           : new Ext.data.Store({
					baseParams     : {
					},
					
					proxy          : new Ext.data.HttpProxy({
						url            : 'http://sadbmatrix2:55167/CMDB/resteasy/relationType',
						method         : 'GET',
					
						headers        : {
							'Accept'          : 'application/json,application/xml,text/html',
							'Content-Type'    : 'application/json'
						}
        			}),
        			
        			reader         : new CMDB.JsonReader({
						totalProperty       : 'total',
    					successProperty     : 'successful',
    					idProperty          : 'RelationType/id/$',
    					root                : 'rows',
						fields              : [
							{
								name             : 'Id',
								mapping          : 'RelationType/id/$'
							},
							{
								name             : 'Name',
								mapping          : 'RelationType/name/$'
							}
						]
					}),
					
					listeners       : {
						'load'           : function(store, records, options) {
							Ext.each(records, function(record) {
								var name = record.get('Name');
								
								record.set('Name', name.replace(/\{.*\}(.*)/,"$1"));
								record.commit();
							});
						}
					}
    			}),
    			
    			valueField      : 'Name',
    			displayField    : 'Name'
			}
		},
		{
			header          : 'CI Type', 
			width           : 150, 
			sortable        : true, 
			dataIndex       : 'DestType',
			editor          : {
				xtype           : 'combo',
				allowBlank      : false,
				blankText       : 'CI Type is necessary to search by name',
				typeAhead       : true,
				forceSelection  : true,
				mode            : 'remote',
				
				bubbleEvents    : [
					'select'
				],
				
				/**
				 * Query parameter is 'name' rather than default 'query'
				*/
				queryParam      : 'name',

				/**
				 * Modify the query parameter if exists with wildcards
				*/
				listeners       : {
         			'beforequery'       : function(e) {
         				e.query = Ext.isEmpty(e.query) ? '' : '%' + e.query + '%';
         			}
   				},
   				
   				/**
   				 * Nullify the start/limit parameters
   				*/
				store           : new Ext.data.Store({
					baseParams     : {
					},
					
					proxy          : new Ext.data.HttpProxy({
						url            : 'http://sadbmatrix2:55167/CMDB/resteasy/elementType',
						method         : 'GET',
					
						headers        : {
							'Accept'          : 'application/json,application/xml,text/html',
							'Content-Type'    : 'application/json'
						}
        			}),
        			
        			reader         : new CMDB.JsonReader({
						totalProperty       : 'total',
    					successProperty     : 'successful',
    					idProperty          : 'ElementType/id/$',
    					root                : 'rows',
						fields              : [
							{
								name             : 'Id',
								mapping          : 'ElementType/id/$'
							},
							{
								name             : 'Name',
								mapping          : 'ElementType/name/$'
							}
						]
					}),
					
					listeners       : {
						'load'           : function(store, records, options) {
							Ext.each(records, function(record) {
								var name = record.get('Name');
								
								record.set('Name', name.replace(/\{.*\}(.*)/,"$1"));
								record.commit();
							});
						}
					}
    			}),
    			
    			valueField      : 'Name',
    			displayField    : 'Name'
			}
		},
		{
			header          : "CI Name", 
			width           : 150, 
			sortable        : true, 
			dataIndex       : 'DestName',
			editor          : {
				xtype           : 'textfield',
				allowBlank      : false,
				disabled        : true
			}
		}
	],  


	/**
	 *
	*/
	initComponent  : function() {
		var fields  = this.fields || [];
		var columns = this.columns || []; 
	
		var editor = new Ext.ux.grid.RowEditor({ saveText: 'Update' });
		
		var reader = new CMDB.JsonReader({
			totalProperty   : 'total',
    		successProperty : 'successful',
    		idProperty      : 'Relation/id/$',
    		root            : 'rows',
			fields          : fields
		});
		
		var store = new Ext.data.GroupingStore({
			reader          : reader,
			sortInfo        : {
				field           : 'start', 
				direction       : 'ASC'
			}
		});
		
		var grid = new Ext.grid.GridPanel({
			height          : 200,
			view            : new Ext.grid.GroupingView({
				markDirty       : false
			}),
			plugins         : editor,
			editor          : editor,
			store           : store,
			columns         : columns,
			
			tbar            : [
				{
					xtype        : 'button',
					ref          : '../Add',
					iconCls      : 'addButton',
					text         : 'Add',
					handler      : this.doAdd,
					scope        : this
				},
				{
					xtype        : 'button',
					ref          : '../Delete',
					iconCls      : 'deleteButton',
					text         : 'Delete',
					handler      : this.doDelete,
					scope        : this
				}
			]
		});
		
		grid.on(
			'select',
			function(component, selected) {
				if (component.getXType() === 'combo') {
					var ed = this.getColumnModel().getColumnAt(2).getEditor();
					ed.enable();
					ed.ciType = selected.get('Name');
				}
			},
			grid
		); 
	
		var config = {
			title       : 'Owned relations',
			autoScroll  : true,
			labelAlign  : 'top',
			bodyStyle   : 'padding:10px; background-color:white;',
			
			Grid        : grid,
			
			items       : [
				{
					xtype       : 'displayfield',
					width       : 'auto',
					html        : 'Relationships owned by the Environment CI'
				},
				grid
			]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Element.DestRelationForm.superclass.initComponent.apply(this, arguments);
	},
	
	
	/**
	 *
	*/
	doDelete       : function() {
	},
	
	
	/**
	 *
	*/
	doAdd          : function() {
		var recordDef = Ext.data.Record.create(this.fields);
		var dummy = new recordDef({
			'Id'         : null,
			'Type'       : null,
			'DestName'   : null,
			'DestType'   : null,
			'Relation'   : null
		});
		
		this.Grid.editor.stopEditing();
		this.Grid.store.insert(0, dummy);
		this.Grid.getView().refresh();
		this.Grid.getSelectionModel().selectRow(0);
		this.Grid.editor.startEditing(0);
	}
});
Ext.reg('destRelationForm', CMDB.Element.DestRelationForm);



/**
 * Property form panel common to all elements
*/
CMDB.Element.PropertyForm = Ext.extend(Ext.form.FormPanel, {

	/**
	 *
	*/
	initComponent  : function() {
		var grid = new Ext.grid.PropertyGrid({
			plugins     : [new Ext.Element.EditParameterPlugin()],
			mapping     : 'Element/configuration/Property',
			height      : 200,
			
			viewConfig  : {
				forceFit       : true,
				scrollOffset   : 2 // the grid will never have scrollbars
			},
			
			source      : {},
			
			tbar        : [
				{
					xtype        : 'textfield',
					ref          : '../Name'
				},
				{
					xtype        : 'button',
					ref          : '../Add',
					iconCls      : 'addButton',
					text         : 'Add',
					handler      : Ext.emptyFn,
					scope        : this
				},
				{
					xtype        : 'button',
					ref          : '../Delete',
					iconCls      : 'deleteButton',
					text         : 'Delete',
					handler      : Ext.emptyFn,
					scope        : this
				}	
			]
		});
		
		grid.Add.on('click', this.doAdd, grid);
		grid.Delete.on('click', this.doDelete, grid);
	
		var config = {
			title       : 'Properties',
			autoScroll  : true,
			labelAlign  : 'top',
			bodyStyle   : 'padding:10px; background-color:white;',
			
			items       : [
				{
					xtype       : 'displayfield',
					width       : 'auto',
					html        : 'User defined properties specific to the Environment CI.'
				},
				grid
			]
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Element.PropertyForm.superclass.initComponent.apply(this, arguments);
	},
	
	
	/**
	 *
	*/
	onRender       : function() {		
		CMDB.Element.PropertyForm.superclass.onRender.apply(this, arguments);
	},
	
	
	/**
	 *
	*/
	doDelete       : function() {
		var selection = this.getSelectionModel().getSelectedCell();
		
		if (selection) {
			var record = this.store.getAt(selection[0]);
			this.removeProperty(record.get('name'));
		}
	},
	
	
	/**
	 *
	*/
	doAdd          : function() {
		if (!Ext.isEmpty(this.Name.getValue())) {
			this.setProperty(this.Name.getValue(),"",true);
		}
	}
});
Ext.reg('propertyForm', CMDB.Element.PropertyForm);



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
				if (msg.state == 'success' && this.element && CMDB.Badgerfish.get(this.element,"Element/id/$") == CMDB.Badgerfish.get(msg.element,"Element/id/$")) {
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
				if (msg.state == 'success' && this.element && CMDB.Badgerfish.get(this.element,"Element/id/$") == CMDB.Badgerfish.get(msg.element,"Element/id/$")) {
					this.element = msg.element;
					this.doLoad();
				}
			}, 
			null
		);
		
		// Handle fbar events
		this.Save.on('click', this.doSave, this);
		this.Delete.on('click', this.doDelete, this);
		
		
		// Handle component events
		this.on('beforeload', this.beforeLoad, this);
		this.on('afterload', this.afterLoad, this);
		this.on('beforesave', this.beforeSave, this);
		this.on('aftersave', this.afterSave, this);
		this.on('beforedelete', this.beforeDelete, this);
		this.on('afterdelete', this.afterDelete, this);
		this.on('afterinsertion', this.afterInsertion, this);
		this.on('afterextraction', this.afterExtraction, this);
	
	
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
	
	
	// private
	loading          : function() {
		if (this.element && CMDB.Badgerfish.get(this.element,"Element/id/$")) {
			this.doInsertion();
		}
		
		this.fireEvent('afterload', this);
	},
	
	
	/**
     * Abstract method automatically called by event beforeload
     */
	beforeLoad       : Ext.emptyFn,
	
	
	/**
     * Abstract method automatically called by event afterload
     */
	afterLoad        : Ext.emptyFn,
	
	
	/**
	 * Saves element by first calling the extraction method that gets
	 * data from the form fields and updates the element
	*/
	doSave          : function() {
		if(this.fireEvent('beforesave', this) !== false){
			this.saving();
		}
	},
	
	
	// private
	saving          : function() {
		if (this.element) {
			this.updateMask.show();
						
			this.doExtraction();
			
			Ext.Ajax.request({
				url           : 'http://sadbmatrix2:55167/CMDB/resteasy/element',
				method        : !CMDB.Badgerfish.get(this.element,"Element/id/$") ? 'POST' : 'PUT',
				
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
     * Abstract method automatically called by event beforesave
     */
	beforeSave       : Ext.emptyFn,
	
	
	/**
     * Abstract method automatically called by event aftersave
     */
	afterSave        : Ext.emptyFn,
	
	
	/**
	 * Delete the element by id
	*/
	doDelete         : function() {
		if(this.fireEvent('beforedelete', this) !== false){
			this.deleting();
		}
	},
	
	
	// private
	deleting         : function() {
		if (this.element && CMDB.Badgerfish.get(this.element,"Element/id/$")) {
			this.updateMask.show();
			
			Ext.Ajax.request({
				url           : 'http://sadbmatrix2:55167/CMDB/resteasy/element/'+CMDB.Badgerfish.get(this.element,"Element/id/$"),
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
	
	
	/**
     * Abstract method automatically called by event beforedelete
     */
	beforeDelete     : Ext.emptyFn,
	
	
	/**
     * Abstract method automatically called by event afterdelete
     */
	afterDelete      : Ext.emptyFn,
	
	
	/**
	 *
	 */
	doExtraction     : function() {
		var element = this.element, fields = this.find('elementdata', true);			
		Ext.each(fields, function(field) {
			field.extract(element);
		});
		
		element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.configuration"]["com.klistret.cmdb.ci.commons.Name"] = element["com.klistret.cmdb.ci.pojo.Element"]["com.klistret.cmdb.ci.pojo.name"]; 
		
		this.fireEvent('afterextraction', this);
	},
	
	
	/**
     * Abstract method automatically called by event afterextraction
     */
	afterExtraction  : Ext.emptyFn,
	
	
	/**
	 *
	 */
	doInsertion      : function() {
		var element = this.element, fields = this.find('elementdata', true);			
		Ext.each(fields, function(field) {
			field.insert(element);
		});
		
		this.fireEvent('afterinsertion', this);
	},
	
	
	/**
     * Abstract method automatically called by event afterinsertion
     */
     afterInsertion  : Ext.emptyFn       
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
	
	
	/**
	 *
	 */
	initComponent  : function() {
		CMDB.Element.Search.superclass.initComponent.apply(this, arguments);
		
		this.addEvents(
			'beforesearch',
			
			'aftersearch'
		);
	},
	
	
	/**
	 *
	 */
	onRender       : function() {
		// Handle fbar events
		this.Search.on('click', this.doSearch, this);
		
		// Handle component events
		this.on('beforesearch', this.beforeSearch ,this);
		this.on('afterextraction', this.afterExtraction, this);
	
		CMDB.Element.Search.superclass.onRender.apply(this, arguments);
	},
	
	
	/**
	 *
	 */
	onDestroy      : function() {
		CMDB.Element.Search.superclass.onDestroy.apply(this, arguments);
	},
	
	
	/**
	 * Loops through all of the components with 'elementdata' property
	 * and uses the getParameter method to get each criterion.
	 */
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
	
	// private
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
	},
	
	
	/**
     * Abstract method automatically called by event beforesearch
     */
	beforeSearch   : Ext.emptyFn,
	
	
	/**
     * Abstract method automatically called by event aftersearch
     */
	afterSearch    : Ext.emptyFn,
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
	
	/**
	 *
	 */
	initComponent  : function() {
		var fields = this.fields || [];
		var columns = this.columns || [];
		
		var reader = new CMDB.JsonReader({
			totalProperty       : 'total',
    		successProperty     : 'successful',
    		idProperty          : 'Element/id/$',
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
                		iconCls        : 'deleteButton',
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
	
	
	/**
	 *
	 */
	onRender       : function() {
		// Add Delete subscription
		this.ElementDeleteSubscribeId = PageBus.subscribe(
			'CMDB.Element.Delete', 
			this, 
			function(subj, msg, data) {
				if (msg.state == 'success' && this.element) {
					var record = this.Grid.store.getById(CMDB.Badgerfish.get(this.element,"Element/id/$"));
					
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
						var other = this.Grid.store.reader.createRecord(
							msg.element, 
							CMDB.Badgerfish.get(msg.element,"Element/id/$")
						);
						
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
	
	
	/**
	 *
	 */
	onDestroy      : function() {
		// Remove event subscriptions
		PageBus.unsubscribe(this.ElementDeleteSubscribeId);
		
		CMDB.Element.Results.superclass.onDestroy.apply(this, arguments);
	},
	
	
	/**
	 *
	 */
	doDelete       : function() {
		var records = this.Grid.getSelectionModel().getSelections();
		
		Ext.each(
			records, 
			function(record) {
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
				},
				this
			);
		});
	},
	
	
	/**
	 *
	 */
	doOpen         : function(grid, index) {
		var record = grid.getStore().getAt(index);	
		var element = record.get("Element");
		
		win = this.desktop.createWindow(
			{
				element       : { 'Element' : element }
			},
			this.editor
		);
		
		win.show();
	}
});