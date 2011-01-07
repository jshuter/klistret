/**
 * Name-spaces
*/
Ext.namespace('CMDB.Environment');


/**
 *
 */
CMDB.Environment.Edit = Ext.extend(CMDB.Element.Edit, {

	/**
	 *
	 */
	initComponent  : function() {
		var config = {
			title       : 'Environment Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm',
				},
				{
					xtype       : 'form',
					
					title       : 'Relations',
					autoScroll  : true,
					labelAlign  : 'top',
					bodyStyle   : 'padding:10px; background-color:white;',
					
					items       : [
						{
							xtype       : 'displayfield',
							width       : 'auto',
                            html        : 'Relationships to the Environment CI'
						},
						{
							xtype       : 'grid',
							ref         : '../RelationGrid',
							height      : 200,
							view        : new Ext.grid.GroupingView({
								markDirty       : false
							}),
							
							plugins     : [new Ext.ux.grid.RowEditor({ saveText: 'Update' })],
							
							store       : new Ext.data.GroupingStore({
								reader          : new Ext.data.JsonReader({
									fields         : [
										{
											name        : 'Id', 
		 									mapping     : 'com.klistret.cmdb.ci.pojo.Relation/com.klistret.cmdb.ci.pojo.id'
		 								},
		 								{
											name        : 'Type', 
		 									mapping     : 'com.klistret.cmdb.ci.pojo.Relation/com.klistret.cmdb.ci.pojo.type/com.klistret.cmdb.ci.pojo.name'
		 								},
		 								{
											name        : 'SourceName', 
		 									mapping     : 'com.klistret.cmdb.ci.pojo.Relation/com.klistret.cmdb.ci.pojo.configuration'
		 								},
		 								{
											name        : 'SourceType', 
		 									mapping     : 'com.klistret.cmdb.ci.pojo.Relation/com.klistret.cmdb.ci.pojo.configuration'
		 								},
		 								{
											name        : 'DestinationName', 
		 									mapping     : 'com.klistret.cmdb.ci.pojo.Relation/com.klistret.cmdb.ci.pojo.configuration'
		 								},
		 								{
											name        : 'DestinationType', 
		 									mapping     : 'com.klistret.cmdb.ci.pojo.Relation/com.klistret.cmdb.ci.pojo.configuration'
		 								},
		 								{
		 									name        : 'Relation',
		 									mapping     : 'com.klistret.cmdb.ci.pojo.Relation'
		 								}
									]
								}),
								sortInfo        : {
									field: 'start', 
									direction: 'ASC'
								}
							}), 
							
							columns     : [
								{
									header      : "Source Name", 
									width       : 120, 
									sortable    : true, 
									dataIndex   : 'SourceName',
									editor      : {
										xtype        : 'textfield',
										allowBlank   : false
									}
								},
								{
									header      : "Source Type", 
									width       : 100, 
									sortable    : true, 
									dataIndex   : 'SourceType',
									editor      : {
										xtype        : 'textfield',
										allowBlank   : false
									}
								},
								{
									header      : "Destination Name", 
									width       : 120, 
									sortable    : true, 
									dataIndex   : 'DestinationName',
									editor      : {
										xtype        : 'textfield',
										allowBlank   : false
									}
								},
								{
									header      : "Destination Type", 
									width       : 100, 
									sortable    : true, 
									dataIndex   : 'DestinationType',
									editor      : {
										xtype        : 'textfield',
										allowBlank   : false
									}
								},
								{
									header      : "Relation", 
									width       : 100, 
									sortable    : true, 
									dataIndex   : 'Type',
									editor      : {
										xtype        : 'textfield',
										allowBlank   : false
									}
								}
							],
							
							tbar        : [
								{
									xtype        : 'button',
									ref          : '../Add',
									iconCls      : 'addButton',
									text         : 'Add',
									handler      : this.addRelation,
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
						}
					]
				},
				{
					xtype       : 'form',
					
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
						{
							xtype       : 'propertygrid',
							ref         : '../PropertyGrid',
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
									handler      : this.addProperty,
									scope        : this
								},
								{
									xtype        : 'button',
									ref          : '../Delete',
									iconCls      : 'deleteButton',
									text         : 'Delete',
									handler      : this.deleteProperty,
									scope        : this
								}	
							]
						}
					]
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Environment.Edit.superclass.initComponent.apply(this, arguments);
	},
	
	
	/**
	 *
	 */
	onRender       : function() {
		CMDB.Environment.Edit.superclass.onRender.apply(this, arguments);
	},
	
	
	/**
	 *
	 */
	deleteProperty : function(b,e) {
		var selection = this.PropertyGrid.getSelectionModel().getSelectedCell();
		
		if (selection) {
			var record = this.PropertyGrid.store.getAt(selection[0]);
			this.PropertyGrid.removeProperty(record.get('name'));
		}
	},
	
	
	/**
	 *
	 */
	addProperty    : function(b,e) {
		if (!Ext.isEmpty(this.PropertyGrid.Name.getValue())) {
			this.PropertyGrid.setProperty(this.PropertyGrid.Name.getValue(),"",true);
		}
	},
	
	addRelation    : function(b,e) {
		if (!this.RelationGrid.editor) {
			this.RelationGrid.editor = this.RelationGrid.plugins[0];
		}
		
		var recDef = Ext.data.Record.create([
			{
				name: 'Id',
				type: 'string'
			},
			{
				name: 'Type',
				type: 'string'
			}, 
			{
        		name: 'SourceName',
        		type: 'string'
    		}, 
    		{
				name: 'SourceType',
        		type: 'string'
        	},
        	{
				name: 'DestinationName',
				type: 'string'
    		},
    		{
        		name: 'DestinationType',
        		type: 'string'
    		}
    	]);

		var rec = new recDef({
			'Id' : '12312',
			'Type' : 'Dependency',
			'SourceName' : 'Whatever',
			'SourceType' : 'Environment',
			'DestinationName' : 'Simple',
			'DestinationType' : 'System'
		});
		
		this.RelationGrid.editor.stopEditing();
		this.RelationGrid.store.insert(0, rec);
		this.RelationGrid.getView().refresh();
		this.RelationGrid.getSelectionModel().selectRow(0);
		this.RelationGrid.editor.startEditing(0);
	}
});



/**
 *
 */
CMDB.Environment.Search = Ext.extend(CMDB.Element.Search, {

	/**
	 *
	 */
	initComponent  : function() {
		var form = new Ext.form.FormPanel({
			border          : false,
			bodyStyle       : 'padding:10px; background-color:white;',
			baseCls         : 'x-plain',
			labelAlign      : 'top',        	
			defaults        : {
				width            : 300
			},
			
			items           : [
				{
					xtype             : 'displayfield',
					width             : 'auto',
					'html'            : 'Search criteria for this CI (Configuration Item)'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Name',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]'
				},
				{
					xtype             : 'datefield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Created after',
					format            : 'Y-m-d',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"{0}\" cast as xs:dateTime]'
				},
				{
					xtype             : 'datefield',
					plugins           : [new Ext.Element.SearchParameterPlugin()],
					fieldLabel        : 'Created before',
					format            : 'Y-m-d',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp lt \"{0}\" cast as xs:dateTime]'
				}
			]
		});
	
		var config = {
			title       : 'Environment Search',
			editor      : CMDB.Environment.Edit,

			items       : form,
		
			fields      : [
				{
					name        : 'Id', 
		 			mapping     : 'Element/id/$'
		 		},
				{
					name        : 'Name', 
					mapping     : 'Element/name/$'
				},
				{
					name        : 'Watermark', 
					mapping     : 'Element/configuration/@Watermark'
				},
				{
					name        : 'Element',
					mapping     : 'Element'
				}
			],
			
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
		}
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Environment.Search.superclass.initComponent.apply(this, arguments);
	},
	
	
	/**
	 *
	 */
	onRender       : function() {
		CMDB.Environment.Search.superclass.onRender.apply(this, arguments);
	},
	
	
	/**
	 * Apply extra filters
	 */
	beforeSearch   : function() {
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]'});
		this.expressions = this.expressions + "&" + Ext.urlEncode({expressions : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[matches(pojo:name,\"Environment\")]'});
	}
});