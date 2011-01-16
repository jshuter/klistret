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
						return Ext.urlEncode({
							expressions : String.format(this.expression, this.getValue().replace(/\*/g, '%'))
						});
					}
					
					if (this.getXType() == 'datefield') {
						return Ext.urlEncode({
							expressions : String.format(this.expression, this.getValue().format('Y-m-d\\TH:i:s.uP'))
						});
					}
				
					return null;
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

	tags           : [],

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
					elementdata       : true,
					fieldLabel        : 'Name',
					allowBlank        : false,
					blankText         : 'Enter a unique environment name',
					// Read from object into JSON
					marshall          : function(element) {
						if (this.getValue() && element['Element']) {
							element['Element']['name'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/name');
						}
					},
					// Read from JSON into object
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/name/$');					
						this.setValue(value);
					}
				},
				{
					xtype             : 'superboxselect',
					elementdata       : true,
					fieldLabel        : 'Tags',
					// Read from object into JSON
					marshall          : function(element) {
						if (this.getValueEx() && element['Element']['configuration']) {
							var commons = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/commons')
								tags = [];
								
							Ext.each(
								this.getValueEx(), 
								function(value) {
									var tag = {
										'$' : value['name']
									};
								
									tags[tags.length] = tag;
								}
							);
							
							element['Element']['configuration'][commons+":Tag"] = tags;
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Tag');
						}
					},
					// Read from JSON into object
					unmarshall        : function(element) {
						var tags = CMDB.Badgerfish.get(element, 'Element/configuration/Tag'),
							formated = [];
						
						if (Ext.isArray(tags)) {
							Ext.each(
								tags,
								function(tag) {
									formated[formated.length] = {
										'name' : tag['$']
									};
								}
							);
						}
						
						if (Ext.isObject(tags)) {
							formated[formated.length] = {
								'name' : tags['$']
							};
						}
						
						this.setValueEx(formated);
					},
					
					store             : new Ext.data.SimpleStore({
						fields           : [
							'name'
						],
						data             : this.tags,
						sortInfo         : {
							field             : 'name', 
							direction         : 'ASC'
						}
					}),
					
					displayField      : 'name',
					valueField        : 'name',
					mode              : 'local',
					
					allowAddNewData   : true,
					addNewDataOnBlur  : true,
					
					extraItemCls: 'x-tag',
					
					listeners         : {
						newitem             : function(bs, v, f) {
							v = v.slice(0,1).toUpperCase() + v.slice(1).toLowerCase();
							var newObj = {
								name: v
							};
							bs.addItem(newObj);
                    	}			
					}
				},
				{
					xtype             : 'textarea',
					elementdata       : true,
					fieldLabel        : 'Description',
					height            : 50,
					blankText         : 'Description of the Environment',
					// Read from object into JSON
					marshall          : function(element) {
						if (this.getValue() && element['Element']['configuration']) {
							var commons = commons = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/commons');
							element['Element']['configuration'][commons+':Description'] = { '$' : this.getValue() };
						}
						else {
							CMDB.Badgerfish.remove(element, 'Element/configuration/Description');
						}
					},
					// Read from JSON into object
					unmarshall        : function(element) {
						var value = CMDB.Badgerfish.get(element, 'Element/configuration/Description/$');
						this.setValue(value);
					}
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Element.GeneralForm.superclass.initComponent.apply(this, arguments);
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
			dataIndex       : 'Type'
		},
		{
			header          : 'CI Type', 
			width           : 150, 
			sortable        : true, 
			dataIndex       : 'DestType'
		},
		{
			header          : "CI Name", 
			width           : 150, 
			sortable        : true, 
			dataIndex       : 'DestName'
		}
	],  


	/**
	 *
	*/
	initComponent  : function() {
		var fields  = this.fields || [];
		var columns = this.columns || []; 
	
		var reader = new CMDB.JsonReader({
			totalProperty   : 'total',
    		successProperty : 'successful',
    		idProperty      : 'Relation/id/$',
    		root            : 'rows',
			fields          : fields
		});
		
		var store = new Ext.data.Store({
			reader          : reader,
			sortInfo        : {
				field           : 'start', 
				direction       : 'ASC'
			}
		});
		
		var grid = new Ext.grid.GridPanel({
			height          : 200,
			store           : store,
			columns         : columns,
			
			tbar            : [
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
			'afterrender',
			function() {
				var gridDropTarget = new Ext.dd.DropTarget(
					this.Grid.getView().scroller.dom, 
					{
						ddGroup    : 'relationDDGroup',
						notifyDrop : this.doAdd.createDelegate(this)
					}
				);
			},
			this
		);
		
		grid.disable();
					
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
	
	onRender       : function() {
		this.on(
			'afterinsertion', 
			function() {
				this.Grid.enable();
			}, 
			this
		);
	
		CMDB.Element.DestRelationForm.superclass.onRender.apply(this, arguments);
	},
	
	/**
	 *
	*/
	doDelete       : function() {
	},
	
	/**
	 *
	*/
	doAdd          : function(ddSource, e, data){
		var records =  ddSource.dragData.selections;
							
		Ext.each(
			records, 
			function(record) {
				var destinationType = CMDB.Badgerfish.get(record.json, 'Element/type/name/$');
				
				var relationType = this.getRelationType(destinationType);
				
				var relation = {
				};
			},
			this
		);
	},
	
	getRelationType : function(destinationType) {
		var relationType;
	
		if (this.relations) {
			Ext.each(
				this.relations,
				function(relation) {
					if (relation.hasOwnProperty(destinationType)) {
						relationType = relation[destinationType];
					}
				},
				this
			);
		}
		
		return relationType;
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
			elementdata : true,
			// Read from object into JSON
			marshall    : function(element) {
				var properties = [],
					commons = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/commons');
						
				this.store.each(
					function(record) {
						var property = {};
							
						property[commons + ":Name"] = {
							"$" : record.get("name")
						};
						property[commons + ":Value"] = {
							"$" : record.get("value")
						};
								
						properties[properties.length] = property;
					},
					this
				);

				if (!Ext.isEmpty(properties) && element['Element']['configuration']) {
					element['Element']['configuration'][commons+':Property'] = properties;
				}
				else {
					CMDB.Badgerfish.remove(element, 'Element/configuration/Property');
				}
			},
			// Read from JSON into object
			unmarshall        : function(element) {
				var properties = CMDB.Badgerfish.get(element, 'Element/configuration/Property');
			
				if (Ext.isArray(properties)) {
					Ext.each(
						properties,
						function(property) {
							this.source[CMDB.Badgerfish.get(property,"Name/$")] = CMDB.Badgerfish.get(property,"Value/$");
						},
						this
					);
				}
						
				if (Ext.isObject(properties)) {
					this.source[CMDB.Badgerfish.get(properties,"Name/$")] = CMDB.Badgerfish.get(properties,"Value/$");
				}
			},

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
		
		this.items.each( 
			function(item) {
				this.relayEvents(item, ['afterinsertion']);
			},
			this
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
		);
		
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
		if(this.fireEvent('beforesave', this) !== false) {
			var forms = this.findByType('form');
		
			var isValid = true;
			Ext.each(forms, function(form) {
				if (!form.getForm().isValid()) isValid = false;
			});
		
			if (isValid) this.saving();
		}
	},
	
	
	// private
	saving          : function() {
		if (this.element) {
			this.updateMask.show();
						
			this.doExtraction();
			
			Ext.Ajax.request({
				url           : (CMDB.URL || '') + '/CMDB/resteasy/element',
				method        : !CMDB.Badgerfish.get(this.element,"Element/id/$") ? 'POST' : 'PUT',
				
				headers       : {
					'Accept'        : 'application/json,application/xml,text/*',
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
				url           : (CMDB.URL || '') + '/CMDB/resteasy/element/'+CMDB.Badgerfish.get(this.element,"Element/id/$"),
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
		var element = this.element, 
			fields = this.find('elementdata', true);
						
		Ext.each(
			fields, 
			function(field) {
				field.marshall(element);
			}
		);
		
		var configuration = CMDB.Badgerfish.get(element, 'Element/configuration')
			commons = CMDB.Badgerfish.getPrefix(element, 'http://www.klistret.com/cmdb/ci/commons');
						
		if (!configuration.hasOwnProperty(commons+":Name")) {
			configuration[commons+":Name"] = {
				'$' : ''
			};
		}
		 
		CMDB.Badgerfish.set(
			element,
			"Element/configuration/Name/$",
			CMDB.Badgerfish.get(element, "Element/name/$")
		);
	
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
			field.unmarshall(element);
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
	afterSearch    : Ext.emptyFn
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
			url            : (CMDB.URL || '') + '/CMDB/resteasy/element',
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
        	border        : false,
    		store         : store,
    		columns       : columns,
			loadMask      : true,
			
			ddGroup       : 'relationDDGroup',
			enableDragDrop: true,
			
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
			Grid       : grid,
			
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
					var record = this.Grid.store.getById(CMDB.Badgerfish.get(msg.element,"Element/id/$"));
					
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
		PageBus.unsubscribe(this.ElementSaveSubscribeId);
		
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
					url           : (CMDB.URL || '') + '/CMDB/resteasy/element/'+record.id,
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