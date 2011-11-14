Ext.namespace('CMDB.Element');

/**
 * Testing the usage of plugins through a simple plugin that jacks into search
 * parameters to identify them and marshall the field's parameter.
 * 
 * Note: Wildcard "*" is replaced by the "%" character
 */
Ext.Element.SearchParameterPlugin = (function() {

	return {

		// init method is required by the plugin framework
		init : function(item) {
			Ext.apply(item, {
				// property marks the field as a searchable parameter
				elementdata : true,

				getParameter : function() {
					if (Ext.isEmpty(this.getValue()))
						return null;

					if (this.getXType() == 'textfield') {
						return Ext.urlEncode({
							expressions : this.wildcard ? String.format(
									this.expression, this.getValue().replace(
											/\*/g, this.wildcard)) : String
									.format(this.expression, this.getValue())
						});
					}

					if (this.getXType() == 'datefield') {
						return Ext.urlEncode({
							expressions : String.format(this.expression, this
									.getValue().format('Y-m-d\\TH:i:s.uP'))
						});
					}

					if (this.getXType() == 'superboxselect') {
						var values;

						Ext.each(this.getValueEx(), function(value) {
							var formated = String.format("\"{0}\"",
									value[this.valueField]);
							values = values == null ? formated : values + ","
									+ formated;
						}, this);

						return Ext.urlEncode({
							expressions : String.format(this.expression, "("
									+ values + ")")
						});
					}

					return null;
				}
			});
		}
	};
});

/**
 * General form panel common for all elements (stuff like name, description,
 * tags, and so forth)
 */
CMDB.Element.GeneralForm = Ext
		.extend(
				Ext.form.FormPanel,
				{

					// Initializes the tags field
					tags : [],

					initComponent : function() {
						var config = {
							title : 'General',
							autoScroll : true,
							labelAlign : 'top',
							bodyStyle : 'padding:10px; background-color:white;',
							defaults : {
								width : 300
							},

							items : [
									// Header text for the form
									{
										xtype : 'displayfield',
										width : 'auto',
										'html' : this.helpInfo
												|| 'Element editor window'
									},

									// Name field
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Name',
										allowBlank : false,
										blankText : 'Enter a unique name for the CI element',

										// Read from object into JSON
										marshall : function(element) {
											if (this.getValue()) {
												element['Element']['name'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish.remove(element,
														'Element/name');
											}
										},

										// Read from JSON into object
										unmarshall : function(element) {
											var value = CMDB.Badgerfish.get(
													element, 'Element/name/$');
											this.setValue(value);
										}
									},

									// Tags field
									{
										xtype : 'superboxselect',
										elementdata : true,
										fieldLabel : 'Tags',

										// Read from object into JSON
										marshall : function(element) {
											if (!Ext.isEmpty(this.getValueEx())) {
												var commons = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/commons')
												tags = [];

												Ext
														.each(
																this
																		.getValueEx(),
																function(value) {
																	var tag = {
																		'$' : value['name']
																	};

																	tags[tags.length] = tag;
																});

												element['Element']['configuration'][commons
														+ ":Tag"] = tags;
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Tag');
											}
										},

										// Read from JSON into object
										unmarshall : function(element) {
											var tags = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Tag'), formated = [];

											if (Ext.isArray(tags)) {
												Ext
														.each(
																tags,
																function(tag) {
																	formated[formated.length] = {
																		'name' : tag['$']
																	};
																});
											}

											if (Ext.isObject(tags)) {
												formated[formated.length] = {
													'name' : tags['$']
												};
											}

											this.setValueEx(formated);
										},

										// Combo box store
										store : new Ext.data.SimpleStore({
											fields : [ 'name' ],
											data : this.tags,
											sortInfo : {
												field : 'name',
												direction : 'ASC'
											}
										}),

										displayField : 'name',
										valueField : 'name',
										mode : 'local',

										allowAddNewData : true,
										addNewDataOnBlur : true,

										extraItemCls : 'x-tag',

										listeners : {
											newitem : function(bs, v, f) {
												var newObj = {
													name : v
												};
												bs.addItem(newObj);
											}
										}
									},
									// Description field
									{
										xtype : 'textarea',
										elementdata : true,
										fieldLabel : 'Description',
										height : 50,
										blankText : 'Description of the Environment',

										// Read from object into JSON
										marshall : function(element) {
											if (this.getValue()) {
												var commons = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/commons');
												element['Element']['configuration'][commons
														+ ':Description'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Description');
											}
										},

										// Read from JSON into object
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Description/$');
											this.setValue(value);
										}
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Element.GeneralForm.superclass.initComponent
								.apply(this, arguments);
					}
				});
Ext.reg('generalForm', CMDB.Element.GeneralForm);

/**
 * Contact information form
 */
CMDB.Element.ContactForm = Ext
		.extend(
				Ext.form.FormPanel,
				{

					initComponent : function() {
						var config = {
							title : 'Contact',
							autoScroll : true,
							labelAlign : 'top',
							bodyStyle : 'padding:10px; background-color:white;',
							defaults : {
								width : 300
							},

							items : [
									// Header information to this form
									{
										xtype : 'displayfield',
										width : 'auto',
										'html' : this.helpInfo
												|| 'Contact information regarding the CI'
									},

									// Contact name
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Contact name',
										allowBlank : true,

										// Read from object into JSON
										marshall : function(element) {
											if (this.getValue()) {
												var commons = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/commons');

												if (!element['Element']['configuration'][commons
														+ ':Ownership'])
													element['Element']['configuration'][commons
															+ ':Ownership'] = {};

												if (!element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'])
													element['Element']['configuration'][commons
															+ ':Ownership'][commons
															+ ':Contact'] = {};

												if (!element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'][commons
														+ ':Name'])
													element['Element']['configuration'][commons
															+ ':Ownership'][commons
															+ ':Contact'][commons
															+ ':Name'] = {};

												element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'][commons
														+ ':Name'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Ownership/Contact/Name');
											}
										},

										// Read from JSON into object
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Ownership/Contact/Name/$');
											this.setValue(value);
										}
									},

									// Contact telephone
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Telephone',
										allowBlank : true,

										// Read from object into JSON
										marshall : function(element) {
											if (this.getValue()) {
												var commons = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/commons');

												if (!element['Element']['configuration'][commons
														+ ':Ownership'])
													element['Element']['configuration'][commons
															+ ':Ownership'] = {};

												if (!element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'])
													element['Element']['configuration'][commons
															+ ':Ownership'][commons
															+ ':Contact'] = {};

												if (!element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'][commons
														+ ':Name'])
													element['Element']['configuration'][commons
															+ ':Ownership'][commons
															+ ':Contact'][commons
															+ ':Telephone'] = {};

												element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'][commons
														+ ':Telephone'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Ownership/Contact/Telephone');
											}
										},

										// Read from JSON into object
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Ownership/Contact/Telephone/$');
											this.setValue(value);
										}
									},

									// Contact email
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'EMail',
										allowBlank : true,

										// Read from object into JSON
										marshall : function(element) {
											if (this.getValue()) {
												var commons = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/commons');

												if (!element['Element']['configuration'][commons
														+ ':Ownership'])
													element['Element']['configuration'][commons
															+ ':Ownership'] = {};

												if (!element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'])
													element['Element']['configuration'][commons
															+ ':Ownership'][commons
															+ ':Contact'] = {};

												if (!element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'][commons
														+ ':Name'])
													element['Element']['configuration'][commons
															+ ':Ownership'][commons
															+ ':Contact'][commons
															+ ':EMail'] = {};

												element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'][commons
														+ ':EMail'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Ownership/Contact/EMail');
											}
										},

										// Read from JSON into object
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Ownership/Contact/EMail/$');
											this.setValue(value);
										}
									},

									// Contact location
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Location',
										allowBlank : true,

										// Read from object into JSON
										marshall : function(element) {
											if (this.getValue()) {
												var commons = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/commons');

												if (!element['Element']['configuration'][commons
														+ ':Ownership'])
													element['Element']['configuration'][commons
															+ ':Ownership'] = {};

												if (!element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'])
													element['Element']['configuration'][commons
															+ ':Ownership'][commons
															+ ':Contact'] = {};

												if (!element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'][commons
														+ ':Name'])
													element['Element']['configuration'][commons
															+ ':Ownership'][commons
															+ ':Contact'][commons
															+ ':Location'] = {};

												element['Element']['configuration'][commons
														+ ':Ownership'][commons
														+ ':Contact'][commons
														+ ':Location'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Ownership/Contact/Location');
											}
										},

										// Read from JSON into object
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Ownership/Contact/Location/$');
											this.setValue(value);
										}
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Element.ContactForm.superclass.initComponent
								.apply(this, arguments);
					}
				});
Ext.reg('contactForm', CMDB.Element.ContactForm);

/**
 * Relation form panel common to all elements
 */
CMDB.Element.DestRelationForm = Ext
		.extend(
				Ext.form.FormPanel,
				{
					/**
					 * Initialize component
					 */
					initComponent : function() {
						var fields = this.fields || [];
						var columns = this.columns || [];

						var proxy = new Ext.data.HttpProxy(
								{
									url : (CMDB.URL || '')
											+ '/CMDB/resteasy/relation',
									method : 'GET',

									headers : {
										'Accept' : 'application/json,application/xml,text/*',
										'Content-Type' : 'application/json'
									}
								});

						var reader = new CMDB.JsonReader({
							totalProperty : 'total',
							successProperty : 'successful',
							idProperty : 'Relation/id/$',
							root : 'rows',
							fields : fields
						});

						var store = new Ext.data.Store(
								{
									reader : reader,
									proxy : proxy,
									sortInfo : {
										field : 'DestName',
										direction : 'ASC'
									},
									listeners : {
										'load' : function(store, records,
												options) {
											Ext
													.each(
															records,
															function(record) {
																var type = record
																		.get('Type'), destType = record
																		.get('DestType');

																record
																		.set(
																				'Type',
																				type
																						.replace(
																								/\{.*\}(.*)/,
																								"$1"));
																record
																		.set(
																				'DestType',
																				destType
																						.replace(
																								/\{.*\}(.*)/,
																								"$1"));
																record.commit();
															});
										}
									}
								});

						store.expressions = Ext
								.urlEncode({
									expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[empty(pojo:toTimeStamp)]/pojo:source[pojo:id eq '
											+ CMDB.Badgerfish.get(
													this.ownerCt.element,
													"Element/id/$") + ']'
								});

						if (this.ownerCt.element
								&& CMDB.Badgerfish.get(this.ownerCt.element,
										"Element/id/$")) {
							store.load({
								params : 'start=0&limit=100&'
										+ store.expressions
							});
						}

						var grid = new Ext.grid.GridPanel(
								{
									height : 200,
									store : store,
									columns : columns,

									loadMask : true,

									viewConfig : {
										forceFit : true
									},

									bbar : new Ext.PagingToolbar(
											{
												pageSize : 20,
												store : store,
												displayInfo : true,
												displayMsg : 'Displaying rows {0} - {1} of {2}',
												emptyMsg : 'No rows to display',

												// Override private doLoad
												// method in Ext.PagingToolbar
												// class
												doLoad : function(start) {
													var o = {}, pn = this
															.getParams();
													o[pn.start] = start;
													o[pn.limit] = this.pageSize;

													if (this.fireEvent(
															'beforechange',
															this, o) !== false) {
														this.store
																.load({
																	params : 'start='
																			+ start
																			+ '&limit='
																			+ this.pageSize
																			+ '&'
																			+ this.store.expressions
																});
													}
												},

												items : [ '-', {
													xtype : 'button',
													ref : 'Delete',
													text : 'Delete',
													iconCls : 'deleteButton',
													handler : this.doDelete,
													scope : this
												}, '-', {
													xtype : 'tbtext',
													ref : 'Status'
												} ]
											})
								});

						grid.on('afterrender', function() {
							var gridDropTarget = new Ext.dd.DropTarget(
									this.Grid.getView().scroller.dom, {
										ddGroup : 'relationDDGroup',
										notifyDrop : this.doAdd
												.createDelegate(this)
									});
						}, this);

						/**
						 * Necessary after layout is done to deal with a bug in
						 * IE
						 * (http://www.coolite.com/forums/Topic24335-16-1.aspx#bm24337)
						 */
						grid
								.on(
										'afterlayout',
										function() {
											(this.ownerCt.element && CMDB.Badgerfish
													.get(this.ownerCt.element,
															"Element/id/$")) ? this.Grid
													.enable()
													: this.Grid.disable();
										}, this);

						grid.on('rowdblclick', this.doOpen, this);

						var config = {
							title : this.title || 'Owned relations',
							autoScroll : true,
							labelAlign : 'top',
							bodyStyle : 'padding:10px; background-color:white;',

							Grid : grid,

							items : [
									{
										xtype : 'displayfield',
										width : 'auto',
										html : this.information
												|| 'Relationships owned by the CI'
									}, grid ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Element.DestRelationForm.superclass.initComponent
								.apply(this, arguments);
					},

					onRender : function() {
						// Add Save subscription
						this.RelationSaveSubscribeId = PageBus.subscribe(
								'CMDB.Relation.Save', this, function(subj, msg,
										data) {
									if (msg.state == 'success') {
										var record = this.Grid.store
												.getById(CMDB.Badgerfish.get(
														msg.relation,
														'Relation/id/$'));
										if (record) {
											this.Grid.store.remove(record);
										}

										record = this.recordCreator(
												this.fields, msg.relation);

										this.Grid.store.add(record);
									}
								}, null);

						// Add Delete subscription
						this.RelationDeleteSubscribeId = PageBus
								.subscribe(
										'CMDB.Relation.Delete',
										this,
										function(subj, msg, data) {
											if (msg.state == 'success'
													&& msg.relation) {
												var record = this.Grid.store
														.getById(CMDB.Badgerfish
																.get(
																		msg.relation,
																		"Relation/id/$"));

												if (record) {
													this.Grid.store
															.remove(record);
												}
											}
										}, null);

						CMDB.Element.DestRelationForm.superclass.onRender
								.apply(this, arguments);

						this
								.on(
										'afterinsertion',
										function() {
											(this.ownerCt.element && CMDB.Badgerfish
													.get(this.ownerCt.element,
															"Element/id/$")) ? this.Grid
													.enable()
													: this.Grid.disable();
										}, this);
					},

					onDestroy : function() {
						// Remove event subscriptions
						PageBus.unsubscribe(this.RelationSaveSubscribeId);
						PageBus.unsubscribe(this.RelationDeleteSubscribeId);

						CMDB.Element.DestRelationForm.superclass.onDestroy
								.apply(this, arguments);
					},

					/**
					 * 
					 */
					doDelete : function() {
						var records = this.Grid.getSelectionModel()
								.getSelections();

						Ext
								.each(
										records,
										function(record) {
											Ext.Ajax
													.request({
														url : (CMDB.URL || '')
																+ '/CMDB/resteasy/relation/'
																+ record
																		.get('Id'),
														method : 'DELETE',

														headers : {
															'Accept' : 'application/json,application/xml,text/*',
															'Content-Type' : 'application/json'
														},

														scope : this,

														success : function(
																result, request) {
															var data = Ext.util.JSON
																	.decode(result.responseText);

															PageBus
																	.publish(
																			'CMDB.Relation.Delete',
																			{
																				state : 'success',
																				relation : data
																			});

															var bbar = this.Grid
																	.getBottomToolbar();
															bbar.Status
																	.setText('Deletion successful');
														},
														failure : function(
																result, request) {
															var bbar = this.Grid
																	.getBottomToolbar();
															bbar.Status
																	.setText('Failed deleting.');

															CMDB.Message
																	.msg(
																			'Failure',
																			(result.responseText ? result.responseText
																					: "Test unable to show message"));
														}
													});
										}, this);
					},

					/**
					 * 
					 */
					doAdd : function(ddSource, e, data) {
						var records = ddSource.dragData.selections;

						Ext
								.each(
										records,
										function(record) {
											var destinationType = CMDB.Badgerfish
													.get(record.get('Element'),
															'type/name/$'), name = this
													.getRelationType(destinationType), index = CMDB.RelationTypes
													.find('Name', name);

											if (index >= 0) {
												var relationType = CMDB.RelationTypes
														.getAt(index).get(
																'RelationType');

												var relation = {
													'Relation' : {
														'@xmlns' : {
															'ns9' : 'http://www.klistret.com/cmdb/ci/relation',
															'ns2' : 'http://www.klistret.com/cmdb/ci/commons',
															'$' : 'http://www.klistret.com/cmdb/ci/pojo'
														},
														'type' : {
															'id' : {
																'$' : relationType['id']['$']
															},
															'name' : {
																'$' : relationType['name']['$']
															}
														},
														'source' : this.ownerCt.element['Element'],
														'destination' : record.json['Element'],
														'fromTimeStamp' : {
															'$' : new Date()
														},
														'createTimeStamp' : {
															'$' : new Date()
														},
														'updateTimeStamp' : {
															'$' : new Date()
														},
														'configuration' : {
															'@xmlns' : {
																'rel' : relationType['name']['$']
																		.replace(
																				/\{(.*)\}.*/,
																				"$1"),
																'xsi' : 'http://www.w3.org/2001/XMLSchema-instance'
															},
															'@xsi:type' : 'rel:'
																	+ relationType['name']['$']
																			.replace(
																					/\{.*\}(.*)/,
																					"$1"),
															'ns2:Name' : {
																'$' : CMDB.Badgerfish
																		.get(
																				record
																						.get('Element'),
																				'name/$')
															}
														}
													}
												};

												Ext.Ajax
														.request({
															url : (CMDB.URL || '')
																	+ '/CMDB/resteasy/relation',
															method : 'POST',

															headers : {
																'Accept' : 'application/json,application/xml,text/*',
																'Content-Type' : 'application/json'
															},

															jsonData : Ext
																	.encode(relation),
															scope : this,

															success : function(
																	result,
																	request) {
																var data = Ext.util.JSON
																		.decode(result.responseText);

																PageBus
																		.publish(
																				'CMDB.Relation.Save',
																				{
																					state : 'success',
																					relation : data
																				});
															},
															failure : function(
																	result,
																	request) {
																CMDB.Message
																		.msg(
																				'Failure',
																				(result.responseText ? result.responseText
																						: "Test unable to show message"));
															}
														});
											} else {
												CMDB.Message
														.msg(
																"Invalid relationship",
																"Relationship is not valid for this CI.");
											}
										}, this);
					},

					getRelationType : function(destinationType) {
						var relationType;

						if (this.relations) {
							Ext.each(this.relations, function(relation) {
								if (relation.hasOwnProperty(destinationType)) {
									relationType = relation[destinationType];
								}
							}, this);
						}

						return relationType;
					},

					doOpen : function(grid, index) {
						var record = grid.getStore().getAt(index);
						var destination = record.get("Destination");

						destination['@xmlns'] = CMDB.Badgerfish.get(record
								.get("Relation"), '@xmlns');

						win = this.desktop.createWindow({
							desktop : this.desktop,
							editor : this.editor,
							element : {
								'Element' : destination
							}
						}, this.editor);

						win.show();
					}
				});
Ext.reg('destRelationForm', CMDB.Element.DestRelationForm);

/**
 * Property form panel common to all elements
 */
CMDB.Element.PropertyForm = Ext
		.extend(
				Ext.form.FormPanel,
				{

					/**
					 * 
					 */
					initComponent : function() {
						var grid = new Ext.grid.PropertyGrid(
								{
									elementdata : true,
									// Read from object into JSON
									marshall : function(element) {
										var properties = [], commons = CMDB.Badgerfish
												.getPrefix(element,
														'http://www.klistret.com/cmdb/ci/commons');

										this.store
												.each(
														function(record) {
															var property = {};

															property[commons
																	+ ":Name"] = {
																"$" : record
																		.get("name")
															};
															property[commons
																	+ ":Value"] = {
																"$" : record
																		.get("value")
															};

															properties[properties.length] = property;
														}, this);

										if (!Ext.isEmpty(properties)
												&& element['Element']['configuration']) {
											element['Element']['configuration'][commons
													+ ':Property'] = properties;
										} else {
											CMDB.Badgerfish
													.remove(element,
															'Element/configuration/Property');
										}
									},
									// Read from JSON into object
									unmarshall : function(element) {
										var properties = CMDB.Badgerfish
												.get(element,
														'Element/configuration/Property');

										if (Ext.isArray(properties)) {
											Ext
													.each(
															properties,
															function(property) {
																this.source[CMDB.Badgerfish
																		.get(
																				property,
																				"Name/$")] = CMDB.Badgerfish
																		.get(
																				property,
																				"Value/$");
															}, this);
										}

										if (Ext.isObject(properties)) {
											this.source[CMDB.Badgerfish.get(
													properties, "Name/$")] = CMDB.Badgerfish
													.get(properties, "Value/$");
										}
									},

									height : 200,

									viewConfig : {
										forceFit : true,
										scrollOffset : 2
									// the grid will never have scrollbars
									},

									source : {},

									tbar : [ {
										xtype : 'textfield',
										ref : '../Name'
									}, {
										xtype : 'button',
										ref : '../Add',
										iconCls : 'addButton',
										text : 'Add',
										handler : Ext.emptyFn,
										scope : this
									}, {
										xtype : 'button',
										ref : '../Delete',
										iconCls : 'deleteButton',
										text : 'Delete',
										handler : Ext.emptyFn,
										scope : this
									} ]
								});

						grid.Add.on('click', this.doAdd, grid);
						grid.Delete.on('click', this.doDelete, grid);

						var config = {
							title : 'Properties',
							autoScroll : true,
							labelAlign : 'top',
							bodyStyle : 'padding:10px; background-color:white;',

							items : [
									{
										xtype : 'displayfield',
										width : 'auto',
										html : 'User defined properties that ease up the ability to charaterise this CI without extending it further into another subclassing CI.'
									}, grid ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Element.PropertyForm.superclass.initComponent
								.apply(this, arguments);
					},

					/**
					 * 
					 */
					onRender : function() {
						CMDB.Element.PropertyForm.superclass.onRender.apply(
								this, arguments);
					},

					/**
					 * 
					 */
					doDelete : function() {
						var selection = this.getSelectionModel()
								.getSelectedCell();

						if (selection) {
							var record = this.store.getAt(selection[0]);
							this.removeProperty(record.get('name'));
						}
					},

					/**
					 * 
					 */
					doAdd : function() {
						if (!Ext.isEmpty(this.Name.getValue())) {
							this.setProperty(this.Name.getValue(), "", true);
						}
					}
				});
Ext.reg('propertyForm', CMDB.Element.PropertyForm);

/**
 * General Element editor
 */
CMDB.Element.Edit = Ext
		.extend(
				Ext.Window,
				{
					title : 'Element Editor',

					autoScroll : false,

					dataProperty : 'citype',
					dataValue : true,

					height : 450,
					width : 600,

					buttonAlign : 'left',

					layoutConfig : {
						animate : false
					},

					fbar : [ {
						xtype : 'tbtext',
						ref : '../Status'
					}, {
						xtype : 'tbfill'
					}, {
						xtype : 'button',
						ref : '../Save',
						text : 'Save'
					}, {
						xtype : 'button',
						ref : '../Delete',
						text : 'Delete',
						disabled : true
					} ],

					/**
					 * Initialize component prior to rendering (settings/events)
					 */
					initComponent : function() {
						CMDB.Element.Edit.superclass.initComponent.apply(this,
								arguments);

						this.addEvents('beforesave',

						'beforeload',

						'beforedelete',

						'aftersave',

						'afterload',

						'afterdelete',

						'afterinsertion',

						'afterextraction',

						'requestfailure');
					},

					/**
					 * Adjust component after child elements are rendered
					 */
					onRender : function() {
						// Add Delete subscription
						this.ElementDeleteSubscribeId = PageBus
								.subscribe(
										'CMDB.Element.Delete',
										this,
										function(subj, msg, data) {
											if (msg.state == 'success'
													&& this.element
													&& CMDB.Badgerfish.get(
															this.element,
															"Element/id/$") == CMDB.Badgerfish
															.get(msg.element,
																	"Element/id/$")) {
												this.close();
											}
										}, null);

						// Add Save subscription
						this.ElementDeleteSubscribeId = PageBus
								.subscribe(
										'CMDB.Element.Save',
										this,
										function(subj, msg, data) {
											if (msg.state == 'success'
													&& this.element
													&& CMDB.Badgerfish.get(
															this.element,
															"Element/id/$") == CMDB.Badgerfish
															.get(msg.element,
																	"Element/id/$")) {
												this.element = msg.element;
												this.doLoad();
											}
										}, null);

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
						CMDB.Element.Edit.superclass.onRender.apply(this,
								arguments);

						// Masks
						this.updateMask = new Ext.LoadMask(this.getEl(), {
							msg : 'Sending. Please wait...'
						});

						this.identificationMask = new Ext.LoadMask(
								this.getEl(),
								{
									msg : 'Identification check. Please wait...'
								});

						this.items.each(function(item) {
							item.relayEvents(this, [ 'afterinsertion' ]);
						}, this);
					},

					/**
					 * 
					 */
					afterRender : function() {
						// Load element
						this.doLoad();

						CMDB.Element.Edit.superclass.afterRender.apply(this,
								arguments);
					},

					/**
					 * Prior to destroying destroy child Ext objects
					 */
					beforeDestroy : function() {
						if (this.rendered) {
							Ext.destroy(this.updateMask);
							Ext.destroy(this.identificationMask);
						}

						CMDB.Element.Edit.superclass.beforeDestroy.apply(this,
								arguments);
					},

					/**
					 * Prior to destroying clean up
					 */
					onDestroy : function() {
						// Remove event subscriptions
						PageBus.unsubscribe(this.ElementDeleteSubscribeId);

						CMDB.Element.Edit.superclass.onDestroy.apply(this,
								arguments);
					},

					/**
					 * Load of element data by first calling the insertion
					 * method that gets data from the element and puts it into
					 * the form fields
					 */
					doLoad : function() {
						if (this.fireEvent('beforeload', this) !== false) {
							this.loading();
						}
					},

					// private
					loading : function() {
						if (this.element
								&& CMDB.Badgerfish.get(this.element,
										"Element/id/$")) {
							this.doInsertion();
							this.Delete.enable();
						}

						this.fireEvent('afterload', this);
					},

					/**
					 * Abstract method automatically called by event beforeload
					 */
					beforeLoad : Ext.emptyFn,

					/**
					 * Abstract method automatically called by event afterload
					 */
					afterLoad : Ext.emptyFn,

					/**
					 * Saves element by first calling the extraction method that
					 * gets data from the form fields and updates the element
					 */
					doSave : function() {
						if (this.fireEvent('beforesave', this) !== false) {
							var forms = this.findByType('form');

							var isValid = true;
							Ext.each(forms, function(form, index) {
								if (!form.getForm().isValid()) {
									isValid = false;
								}
							});

							if (isValid) {
								this.identification();
							} else {
								this.Status.setText("Validation failed.");
							}
						}
					},

					identification : function() {
						if (this.element) {
							this.identificationMask.show();

							this.doExtraction();

							Ext.Ajax
									.request({
										url : (CMDB.URL || '')
												+ '/CMDB/resteasy/identification',
										method : 'POST',

										headers : {
											'Accept' : 'application/json,application/xml,text/*',
											'Content-Type' : 'application/json; charset=ISO-8859-1'
										},

										jsonData : Ext.encode(this.element),
										scope : this,

										success : function(result, request) {
											var count = Ext.util.JSON
													.decode(result.responseText);

											this.identificationMask.hide();

											if (count == 0) {
												this.Status
														.setText("Identification passed");
												this.saving();
											} else {
												this.Status
														.setText("Identification failed. Found "
																+ count
																+ " similar element(s).");
												CMDB.Message
														.msg(
																"Identification failed",
																"Found "
																		+ count
																		+ " similar element(s).");
											}
										},
										failure : function(result, request) {
											this.identificationMask.hide();
											this.Status
													.setText("Failed identification. ");
											this.fireEvent('requestfailure',
													this, result);

											CMDB.Message
													.msg(
															'Failure',
															(result.responseText ? result.responseText
																	: "Test unable to show message"));
										}
									});
						}
					},

					// private
					saving : function() {
						if (this.element) {
							this.updateMask.show();

							this.doExtraction();

							Ext.Ajax
									.request({
										url : (CMDB.URL || '')
												+ '/CMDB/resteasy/element',
										method : !CMDB.Badgerfish.get(
												this.element, "Element/id/$") ? 'POST'
												: 'PUT',

										headers : {
											'Accept' : 'application/json,application/xml,text/*',
											'Content-Type' : 'application/json; charset=ISO-8859-1'
										},

										jsonData : Ext.encode(this.element),
										scope : this,

										success : function(result, request) {
											this.element = Ext.util.JSON
													.decode(result.responseText);

											PageBus.publish(
													'CMDB.Element.Save', {
														state : 'success',
														element : this.element
													});

											this.updateMask.hide();
											this.Status
													.setText("Successfully saved.");
											this.fireEvent('aftersave', this);
										},
										failure : function(result, request) {
											this.updateMask.hide();
											this.Status
													.setText("Failed saving. ");
											this.fireEvent('requestfailure',
													this, result);

											CMDB.Message
													.msg(
															'Failure',
															(result.responseText ? result.responseText
																	: "Test unable to show message"));
										}
									});
						}
					},

					/**
					 * Abstract method automatically called by event beforesave
					 */
					beforeSave : Ext.emptyFn,

					/**
					 * Abstract method automatically called by event aftersave
					 */
					afterSave : Ext.emptyFn,

					/**
					 * Delete the element by id
					 */
					doDelete : function() {
						if (this.fireEvent('beforedelete', this) !== false) {
							this.deleting();
						}
					},

					// private
					deleting : function() {
						if (this.element
								&& CMDB.Badgerfish.get(this.element,
										"Element/id/$")) {
							this.updateMask.show();

							Ext.Ajax
									.request({
										url : (CMDB.URL || '')
												+ '/CMDB/resteasy/element/'
												+ CMDB.Badgerfish.get(
														this.element,
														"Element/id/$"),
										method : 'DELETE',

										headers : {
											'Accept' : 'application/json,application/xml,text/*',
											'Content-Type' : 'application/json'
										},

										scope : this,

										success : function(result, request) {
											this.element = Ext.util.JSON
													.decode(result.responseText);

											PageBus.publish(
													'CMDB.Element.Delete', {
														state : 'success',
														element : this.element
													});

											this.updateMask.hide();
											this.fireEvent('afterdelete', this);
										},
										failure : function(result, request) {
											this.updateMask.hide();
											this.Status
													.setText("Failed deleting."
															+ (result.responseText ? result.responseText
																	: ""));
											this.fireEvent('requestfailure',
													this, result);
										}
									});
						}
					},

					/**
					 * Abstract method automatically called by event
					 * beforedelete
					 */
					beforeDelete : Ext.emptyFn,

					/**
					 * Abstract method automatically called by event afterdelete
					 */
					afterDelete : Ext.emptyFn,

					/**
					 * 
					 */
					doExtraction : function() {
						var element = this.element, fields = this.find(
								'elementdata', true);

						Ext.each(fields, function(field) {
							field.marshall(element);
						});

						var configuration = CMDB.Badgerfish.get(element,
								'Element/configuration')
						commons = CMDB.Badgerfish.getPrefix(element,
								'http://www.klistret.com/cmdb/ci/commons');

						if (!configuration.hasOwnProperty(commons + ":Name")) {
							configuration[commons + ":Name"] = {
								'$' : ''
							};
						}

						CMDB.Badgerfish.set(element,
								"Element/configuration/Name/$", CMDB.Badgerfish
										.get(element, "Element/name/$"));

						this.fireEvent('afterextraction', this);
					},

					/**
					 * Abstract method automatically called by event
					 * afterextraction
					 */
					afterExtraction : Ext.emptyFn,

					/**
					 * 
					 */
					doInsertion : function() {
						var element = this.element, fields = this.find(
								'elementdata', true);
						Ext.each(fields, function(field) {
							field.unmarshall(element);
						});

						this.fireEvent('afterinsertion', this);
					},

					/**
					 * Abstract method automatically called by event
					 * afterinsertion
					 */
					afterInsertion : Ext.emptyFn
				});

/**
 * General Element search window
 */
CMDB.Element.Search = Ext
		.extend(
				Ext.Window,
				{
					title : 'Element Search',

					autoScroll : false,

					height : 450,
					width : 600,

					layout : 'fit',

					buttonAlign : 'left',

					fbar : [ {
						xtype : 'tbtext',
						ref : '../Status'
					}, {
						xtype : 'tbfill'
					}, {
						xtype : 'button',
						ref : '../Search',
						text : 'Search'
					} ],

					start : 0,
					limit : 20,

					/**
					 * 
					 */
					initComponent : function() {
						CMDB.Element.Search.superclass.initComponent.apply(
								this, arguments);

						this.addEvents('beforesearch',

						'aftersearch');
					},

					/**
					 * 
					 */
					onRender : function() {
						// Handle fbar events
						this.Search.on('click', this.doSearch, this);

						// Handle component events
						this.on('beforesearch', this.beforeSearch, this);
						this.on('aftersearch', this.afterExtraction, this);

						CMDB.Element.Search.superclass.onRender.apply(this,
								arguments);

						// Counting mask
						this.countMask = new Ext.LoadMask(this.getEl(), {
							msg : 'Determine result size. Please wait...'
						});
					},

					/**
					 * Prior to destroying destroy child Ext objects
					 */
					beforeDestroy : function() {
						if (this.rendered) {
							Ext.destroy(this.countMask);
						}

						CMDB.Element.Edit.superclass.beforeDestroy.apply(this,
								arguments);
					},

					/**
					 * 
					 */
					onDestroy : function() {
						CMDB.Element.Search.superclass.onDestroy.apply(this,
								arguments);
					},

					/**
					 * Loops through all of the components with 'elementdata'
					 * property and uses the getParameter method to get each
					 * criterion.
					 */
					doSearch : function() {
						var initialized, criteria = this.find('elementdata',
								true);
						Ext.each(criteria, function(criterion) {
							var parameter = criterion.getParameter();

							if (parameter) {
								initialized = !initialized ? parameter
										: initialized + "&" + parameter;
							}
						});

						this.expressions = initialized;
						if (this.expressions
								&& this.fireEvent('beforesearch', this) !== false) {
							this.counting();
						}
					},

					// private
					counting : function() {
						this.countMask.show();

						Ext.Ajax
								.request({
									url : (CMDB.URL || '')
											+ '/CMDB/resteasy/element/count',
									method : 'GET',

									headers : {
										'Accept' : 'application/json,application/xml,text/*',
										'Content-Type' : 'application/json'
									},
									scope : this,
									success : function(result, request) {
										this.countMask.hide();
										this.searching(result.responseText);
									},
									failure : function(result, request) {
										this.countMask.hide();
										this.Status
												.setText("Failed searching."
														+ (result.responseText ? result.responseText
																: ""));
									},
									params : this.expressions
								});
					},

					// private
					searching : function(count) {
						win = this.desktop.createWindow({
							desktop : this.desktop,
							fields : this.fields,
							columns : this.columns,
							editor : this.editor,
							bbarButtons : this.bbarButtons,
							count : count,

							title : 'Results - ' + this.title
						}, CMDB.Element.Results);

						win.show();
						win.Grid.getStore().expressions = this.expressions;
						win.Grid.getStore().load(
								{
									params : 'start=' + this.start + '&limit='
											+ this.limit + '&'
											+ this.expressions
								});
					},

					/**
					 * Automatically called by event beforesearch (careful when
					 * overriding)
					 */
					beforeSearch : function() {
						this.expressions = Ext
								.urlEncode({
									expressions : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]'
								})
								+ "&" + this.expressions;
						this.expressions = Ext
								.urlEncode({
									expressions : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[pojo:name eq \"'
											+ this.elementType + '\"]'
								})
								+ "&" + this.expressions;
					},

					/**
					 * Abstract method automatically called by event aftersearch
					 */
					afterSearch : Ext.emptyFn
				});

/**
 * General Element results window
 */
CMDB.Element.Results = Ext
		.extend(
				Ext.Window,
				{
					title : 'Search Results',

					height : 450,
					width : 800,

					layout : 'fit',
					iconCls : 'icon-grid',

					/**
					 * Added listeners to the store to enable custom formatting
					 * on the record items (like tags)
					 */
					initComponent : function() {
						var fields = this.fields || [];
						var columns = this.columns || [];

						var bbarItems = new Array();
						bbarItems.push('-');

						bbarItems.push({
							xtype : 'button',
							ref : 'Delete',
							text : 'Delete',
							iconCls : 'deleteButton',
							handler : this.doDelete,
							scope : this
						});

						if (this.bbarButtons) {
							for ( var buttonIndex = 0; buttonIndex < this.bbarButtons.length; buttonIndex++) {
								bbarItems.push('-');

								var buttonConfig = this.bbarButtons[buttonIndex];
								Ext.apply(buttonConfig, {
									scope : this
								});
								if (buttonConfig.menu) {
									for ( var menuIndex = 0; menuIndex < buttonConfig.menu.items.length; menuIndex++) {
										Ext
												.apply(
														buttonConfig.menu.items[menuIndex],
														{
															scope : this
														});
									}
								}
								bbarItems.push(buttonConfig);
							}
						}

						bbarItems.push('-');
						bbarItems.push({
							xtype : 'tbtext',
							ref : 'Status'
						});

						var reader = new CMDB.JsonReader({
							totalProperty : 'total',
							successProperty : 'successful',
							idProperty : 'Element/id/$',
							root : 'rows',
							fields : fields,
							passedCount : this.count
						});

						var proxy = new Ext.data.HttpProxy(
								{
									url : (CMDB.URL || '')
											+ '/CMDB/resteasy/element',
									method : 'GET',

									headers : {
										'Accept' : 'application/json,application/xml,text/*',
										'Content-Type' : 'application/json'
									}
								});

						proxy.on('exception', function(dp, type, action,
								options, response, arguments) {
							CMDB.Message.msg("Query failure",
									(response.statusText ? response.statusText
											: "Test unable to show message"));
						});

						var store = new Ext.data.Store(
								{
									proxy : proxy,
									reader : reader,
									count : this.count,

									listeners : {
										/**
										 * Not using the set method in the
										 * Record bypasses the dirty flag and
										 * does not change the store's modify
										 * records (ie. disables the update
										 * event).
										 */
										'load' : function(store, records,
												options) {
											Ext
													.each(
															records,
															function(record) {
																record.fields
																		.each(
																				function(
																						item,
																						index,
																						length) {
																					if (Ext
																							.isFunction(item.formating)) {
																						this.data[item.name] = item
																								.formating(this
																										.get(item.name));
																					}
																				},
																				record);

																record.commit();
															});
										},

										/**
										 * No commit is issued (apparently not
										 * necessary)
										 */
										'add' : function(store, records, index) {
											Ext
													.each(
															records,
															function(record) {
																record.fields
																		.each(
																				function(
																						item,
																						index,
																						length) {
																					if (Ext
																							.isFunction(item.formating)) {
																						this.data[item.name] = item
																								.formating(this
																										.get(item.name));
																					}
																				},
																				record);
															});
										}
									}
								});

						var grid = new Ext.grid.GridPanel(
								{
									border : false,
									store : store,
									columns : columns,
									loadMask : true,

									ddGroup : 'relationDDGroup',
									enableDragDrop : true,

									viewConfig : {
									// forceFit : true
									},

									bbar : new Ext.PagingToolbar(
											{
												pageSize : 20,
												store : store,
												displayInfo : true,
												displayMsg : 'Displaying rows {0} - {1} of {2}',
												emptyMsg : 'No rows to display',

												/**
												 * Override private doLoad
												 * method in Ext.PagingToolbar
												 * class
												 */
												doLoad : function(start) {
													var o = {}, pn = this
															.getParams();

													o[pn.start] = start;
													o[pn.limit] = this.pageSize;

													if (this.fireEvent(
															'beforechange',
															this, o) !== false) {
														this.store
																.load({
																	params : 'start='
																			+ start
																			+ '&limit='
																			+ this.pageSize
																			+ '&'
																			+ this.store.expressions,
																	start : start,
																	limit : this.pageSize
																});
													}
												},

												/**
												 * Override private onLoad
												 * method in Ext.PagingToolbar
												 * class (to handle cursor
												 * setting)
												 */
												onLoad : function(store, r, o) {
													if (!this.rendered) {
														this.dsLoaded = [
																store, r, o ];
														return;
													}
													var p = this.getParams();
													this.cursor = o.start ? o.start
															: 0;
													var d = this.getPageData(), ap = d.activePage, ps = d.pages;

													this.afterTextItem
															.setText(String
																	.format(
																			this.afterPageText,
																			d.pages));
													this.inputItem.setValue(ap);
													this.first
															.setDisabled(ap == 1);
													this.prev
															.setDisabled(ap == 1);
													this.next
															.setDisabled(ap == ps);
													this.last
															.setDisabled(ap == ps);
													this.refresh.enable();
													this.updateInfo();
													this.fireEvent('change',
															this, d);
												},

												items : bbarItems
											})
								});

						grid.on('rowdblclick', this.doOpen, this);

						var config = {
							Grid : grid,

							items : grid
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Element.Results.superclass.initComponent.apply(
								this, arguments);
					},

					/**
					 * 
					 */
					onRender : function() {
						// Add Delete subscription
						this.ElementDeleteSubscribeId = PageBus.subscribe(
								'CMDB.Element.Delete', this, function(subj,
										msg, data) {
									if (msg.state == 'success' && msg.element) {
										var record = this.Grid.store
												.getById(CMDB.Badgerfish.get(
														msg.element,
														"Element/id/$"));

										if (record) {
											this.Grid.store.remove(record);
										}
									}
								}, null);

						this.ElementSaveSubscribeId = PageBus
								.subscribe(
										'CMDB.Element.Save',
										this,
										function(subj, msg, data) {
											if (msg.state == 'success') {
												var record = this.Grid.store
														.getById(CMDB.Badgerfish
																.get(
																		msg.element,
																		"Element/id/$"));

												if (record) {
													var other = this.Grid.store.reader
															.createRecord(
																	msg.element,
																	CMDB.Badgerfish
																			.get(
																					msg.element,
																					"Element/id/$"));

													var index = this.Grid.store
															.indexOf(record);
													this.Grid.store
															.remove(record);
													this.Grid.store.insert(
															index, other);
												}
											}
										}, null);

						CMDB.Element.Results.superclass.onRender.apply(this,
								arguments);
					},

					/**
					 * 
					 */
					onDestroy : function() {
						// Remove event subscriptions
						PageBus.unsubscribe(this.ElementDeleteSubscribeId);
						PageBus.unsubscribe(this.ElementSaveSubscribeId);

						CMDB.Element.Results.superclass.onDestroy.apply(this,
								arguments);
					},

					/**
					 * 
					 */
					doDelete : function() {
						var records = this.Grid.getSelectionModel()
								.getSelections();

						if (records.length > 0)
							this.Grid.loadMask.show();

						Ext
								.each(
										records,
										function(record) {
											Ext.Ajax
													.request({
														url : (CMDB.URL || '')
																+ '/CMDB/resteasy/element/'
																+ record.id,
														method : 'DELETE',

														headers : {
															'Accept' : 'application/json,application/xml,text/*',
															'Content-Type' : 'application/json'
														},

														scope : this,

														success : function(
																result, request) {
															this.element = Ext.util.JSON
																	.decode(result.responseText);

															PageBus
																	.publish(
																			'CMDB.Element.Delete',
																			{
																				state : 'success',
																				element : this.element
																			});

															var bbar = this.Grid
																	.getBottomToolbar();
															bbar.Status
																	.setText('Deletion successful');

															this.Grid.loadMask
																	.hide();
														},
														failure : function(
																result, request) {
															var bbar = this.Grid
																	.getBottomToolbar();
															bbar.Status
																	.setText('Failed deleting.'
																			+ (result.responseText ? result.responseText
																					: ""));

															this.Grid.loadMask
																	.hide();
														}
													});
										}, this);
					},

					/**
					 * 
					 */
					doOpen : function(grid, index) {
						var record = grid.getStore().getAt(index);
						var element = record.get("Element");

						win = this.desktop.createWindow({
							desktop : this.desktop,
							editor : this.editor,
							element : {
								'Element' : element
							}
						}, this.editor);

						win.show();
					}
				});

/**
 * Organizations (context) as store
 */
CMDB.OrganizationStore = Ext
		.extend(
				Ext.data.Store,
				{
					proxy : new Ext.data.HttpProxy(
							{
								url : (CMDB.URL || '')
										+ '/CMDB/resteasy/element',
								method : 'GET',

								headers : {
									'Accept' : 'application/json,application/xml,text/html',
									'Content-Type' : 'application/json'
								}
							}),

					reader : new CMDB.JsonReader({
						totalProperty : 'total',
						successProperty : 'successful',
						idProperty : 'Element/id/$',
						root : 'rows',
						fields : [ {
							name : 'Id',
							mapping : 'Element/id/$'
						}, {
							name : 'Name',
							mapping : 'Element/name/$'
						}, {
							name : 'Element',
							mapping : 'Element'
						} ]
					}),

					listeners : {
						'beforeload' : function(store, options) {
							var expressions;

							expressions = expressions
									+ "&"
									+ Ext
											.urlEncode({
												expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}Organization\"]'
											});
							expressions = expressions + "&" + Ext.urlEncode({
								expressions : store.baseParams.expressions
							});

							options.params = "start=0&limit=10&" + expressions;
						}
					}
				});

/**
 * Modules (context) as store
 */
CMDB.ModuleStore = Ext
		.extend(
				Ext.data.Store,
				{
					proxy : new Ext.data.HttpProxy(
							{
								url : (CMDB.URL || '')
										+ '/CMDB/resteasy/element',
								method : 'GET',

								headers : {
									'Accept' : 'application/json,application/xml,text/html',
									'Content-Type' : 'application/json'
								}
							}),

					reader : new CMDB.JsonReader({
						totalProperty : 'total',
						successProperty : 'successful',
						idProperty : 'Element/id/$',
						root : 'rows',
						fields : [ {
							name : 'Id',
							mapping : 'Element/id/$'
						}, {
							name : 'Name',
							mapping : 'Element/name/$'
						}, {
							name : 'Element',
							mapping : 'Element'
						} ]
					}),

					listeners : {
						'beforeload' : function(store, options) {
							var expressions;

							expressions = expressions
									+ "&"
									+ Ext
											.urlEncode({
												expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}Module\"]'
											});
							expressions = expressions + "&" + Ext.urlEncode({
								expressions : store.baseParams.expressions
							});

							options.params = "start=0&limit=10&" + expressions;
						}
					}
				});

/**
 * PublicationType (context) as store
 */
CMDB.PublicationTypeStore = Ext
		.extend(
				Ext.data.Store,
				{
					proxy : new Ext.data.HttpProxy(
							{
								url : (CMDB.URL || '')
										+ '/CMDB/resteasy/element',
								method : 'GET',

								headers : {
									'Accept' : 'application/json,application/xml,text/html',
									'Content-Type' : 'application/json'
								}
							}),

					reader : new CMDB.JsonReader({
						totalProperty : 'total',
						successProperty : 'successful',
						idProperty : 'Element/id/$',
						root : 'rows',
						fields : [ {
							name : 'Id',
							mapping : 'Element/id/$'
						}, {
							name : 'Name',
							mapping : 'Element/name/$'
						}, {
							name : 'Element',
							mapping : 'Element'
						} ]
					}),

					listeners : {
						'beforeload' : function(store, options) {
							var expressions;

							expressions = expressions
									+ "&"
									+ Ext
											.urlEncode({
												expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}PublicationType\"]'
											});
							expressions = expressions + "&" + Ext.urlEncode({
								expressions : store.baseParams.expressions
							});

							options.params = "start=0&limit=10&" + expressions;
						}
					}
				});

CMDB.EnvironmentStore = Ext
		.extend(
				Ext.data.Store,
				{
					proxy : new Ext.data.HttpProxy(
							{
								url : (CMDB.URL || '')
										+ '/CMDB/resteasy/element',
								method : 'GET',

								headers : {
									'Accept' : 'application/json,application/xml,text/html',
									'Content-Type' : 'application/json'
								}
							}),

					reader : new CMDB.JsonReader({
						totalProperty : 'total',
						successProperty : 'successful',
						idProperty : 'Element/id/$',
						root : 'rows',
						fields : [ {
							name : 'Id',
							mapping : 'Element/id/$'
						}, {
							name : 'Name',
							mapping : 'Element/name/$'
						}, {
							name : 'Element',
							mapping : 'Element'
						} ]
					}),

					listeners : {
						'beforeload' : function(store, options) {
							var expressions;

							expressions = expressions
									+ "&"
									+ Ext
											.urlEncode({
												expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}Environment\"]'
											});
							expressions = expressions + "&" + Ext.urlEncode({
								expressions : store.baseParams.expressions
							});

							options.params = "start=0&limit=10&" + expressions;
						}
					}
				});

CMDB.SoftwareLifecycleStore = Ext
		.extend(
				Ext.data.Store,
				{
					proxy : new Ext.data.HttpProxy(
							{
								url : (CMDB.URL || '')
										+ '/CMDB/resteasy/element',
								method : 'GET',

								headers : {
									'Accept' : 'application/json,application/xml,text/html',
									'Content-Type' : 'application/json'
								}
							}),

					reader : new CMDB.JsonReader({
						totalProperty : 'total',
						successProperty : 'successful',
						idProperty : 'Element/id/$',
						root : 'rows',
						fields : [ {
							name : 'Id',
							mapping : 'Element/id/$'
						}, {
							name : 'Name',
							mapping : 'Element/name/$'
						}, {
							name : 'Element',
							mapping : 'Element'
						} ]
					}),

					listeners : {
						'beforeload' : function(store, options) {
							var expressions;

							expressions = expressions
									+ "&"
									+ Ext
											.urlEncode({
												expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context/lifecycle}SoftwareLifecycle\"]'
											});
							expressions = expressions + "&" + Ext.urlEncode({
								expressions : store.baseParams.expressions
							});

							options.params = "start=0&limit=10&" + expressions;
						}
					}
				});

CMDB.TimeframeStore = Ext
		.extend(
				Ext.data.Store,
				{
					proxy : new Ext.data.HttpProxy(
							{
								url : (CMDB.URL || '')
										+ '/CMDB/resteasy/element',
								method : 'GET',

								headers : {
									'Accept' : 'application/json,application/xml,text/html',
									'Content-Type' : 'application/json'
								}
							}),

					reader : new CMDB.JsonReader({
						totalProperty : 'total',
						successProperty : 'successful',
						idProperty : 'Element/id/$',
						root : 'rows',
						fields : [ {
							name : 'Id',
							mapping : 'Element/id/$'
						}, {
							name : 'Name',
							mapping : 'Element/name/$'
						}, {
							name : 'Element',
							mapping : 'Element'
						} ]
					}),

					listeners : {
						'beforeload' : function(store, options) {
							var expressions;

							expressions = expressions
									+ "&"
									+ Ext
											.urlEncode({
												expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}Timeframe\"]'
											});
							expressions = expressions + "&" + Ext.urlEncode({
								expressions : store.baseParams.expressions
							});

							options.params = "start=0&limit=10&" + expressions;
						}
					}
				});

CMDB.ApplicationStore = new Ext.data.Store(
		{
			proxy : new Ext.data.HttpProxy({
				url : (CMDB.URL || '') + '/CMDB/resteasy/element',
				method : 'GET',

				headers : {
					'Accept' : 'application/json,application/xml,text/html',
					'Content-Type' : 'application/json'
				}
			}),

			reader : new CMDB.JsonReader({
				totalProperty : 'total',
				successProperty : 'successful',
				idProperty : 'Element/id/$',
				root : 'rows',
				fields : [ {
					name : 'Id',
					mapping : 'Element/id/$'
				}, {
					name : 'Name',
					mapping : 'Element/name/$'
				}, {
					name : 'Element',
					mapping : 'Element'
				} ]
			}),

			listeners : {
				'beforeload' : function(store, options) {
					var expressions;

					expressions = expressions
							+ "&"
							+ Ext
									.urlEncode({
										expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]'
									});
					expressions = expressions + "&" + Ext.urlEncode({
						expressions : store.baseParams.expressions
					});

					options.params = "start=0&limit=10&" + expressions;
				}
			}
		});

CMDB.SoftwareStore = Ext
		.extend(
				Ext.data.Store,
				{
					proxy : new Ext.data.HttpProxy(
							{
								url : (CMDB.URL || '')
										+ '/CMDB/resteasy/element',
								method : 'GET',

								headers : {
									'Accept' : 'application/json,application/xml,text/html',
									'Content-Type' : 'application/json'
								}
							}),

					reader : new CMDB.JsonReader({
						totalProperty : 'total',
						successProperty : 'successful',
						idProperty : 'Element/id/$',
						root : 'rows',
						fields : [ {
							name : 'Id',
							mapping : 'Element/id/$'
						}, {
							name : 'Name',
							mapping : 'Element/name/$'
						}, {
							name : 'Created',
							mapping : 'Element/createTimeStamp/$',
							type : 'date'
						}, {
							name : 'Version',
							mapping : 'Element/configuration/Version/$'
						}, {
							name : 'Type',
							mapping : 'Element/configuration/Type/$'
						}, {
							name : 'Label',
							mapping : 'Element/configuration/Label/$'
						}, {
							name : 'Element',
							mapping : 'Element'
						} ]
					}),

					listeners : {
						'beforeload' : function(store, options) {
							var expressions;

							expressions = expressions
									+ "&"
									+ Ext
											.urlEncode({
												expressions : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]'
											});
							expressions = expressions + "&" + Ext.urlEncode({
								expressions : store.baseParams.expressions
							});

							options.params = "start=0&limit=10&" + expressions;
						}
					}
				});