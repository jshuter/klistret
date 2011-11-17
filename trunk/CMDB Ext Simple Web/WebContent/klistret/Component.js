Ext.namespace('CMDB.Software');
Ext.namespace('CMDB.Publication');

/**
 * Software - General form
 */
CMDB.Software.GeneralForm = Ext
		.extend(
				Ext.form.FormPanel,
				{
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
						CMDB.Software.GeneralForm.superclass.initComponent
								.apply(this, arguments);
					}
				});
Ext.reg('softwareGeneralForm', CMDB.Software.GeneralForm);

/**
 * Software - Software form forcing associations to organizations, software
 * names and a timeframe.
 * 
 */
CMDB.Software.IdentificationForm = Ext
		.extend(
				Ext.form.FormPanel,
				{
					initComponent : function() {
						var config = {
							title : 'Identification',
							autoScroll : true,
							labelAlign : 'top',
							bodyStyle : 'padding:10px; background-color:white;',
							defaults : {
								width : 300
							},

							items : [
									{
										xtype : 'displayfield',
										width : 'auto',
										'html' : 'Similar the Ivy framework, software is produced by an organization (group) and ships as a logical unit thereafter further decorated with version number.'
									},
									{
										xtype : 'combo',
										elementdata : true,
										fieldLabel : 'Organization',
										allowBlank : false,
										blankText : 'Organization is required',
										store : new CMDB.OrganizationStore(),
										displayField : 'Name',
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : function(e) {
												e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
														+ e.query + '%\")]';
											}
										},

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Organization'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Organization');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Organization/$');
											this.setValue(value);
										}
									},
									{
										xtype : 'combo',
										elementdata : true,
										fieldLabel : 'Name',
										allowBlank : false,
										blankText : 'Name is required',
										store : new CMDB.SoftwareContextStore(),
										displayField : 'Name',
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : function(e) {
												e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
														+ e.query + '%\")]';
											}
										},

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
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Version',
										allowBlank : false,
										blankText : 'Enter a version',

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Version'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Version');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Version/$');
											this.setValue(value);
										}
									},
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Labels are like aliases but not necessary (should mirror a combination of the software name and version)',
										allowBlank : true,

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Label'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Label');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Label/$');
											this.setValue(value);
										}
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Software.IdentificationForm.superclass.initComponent
								.apply(this, arguments);
					}
				});
Ext.reg('softwareIdentificationForm', CMDB.Software.IdentificationForm);

/**
 * 
 */
CMDB.Software.LifecycleForm = Ext
		.extend(
				Ext.form.FormPanel,
				{
					initComponent : function() {
						var config = {
							title : 'Lifecycle',
							autoScroll : true,
							labelAlign : 'top',
							bodyStyle : 'padding:10px; background-color:white;',
							defaults : {
								width : 300
							},

							items : [
									{
										xtype : 'displayfield',
										width : 'auto',
										'html' : 'All software goes through different stages in a lifecycle and this form establishes the phase, availability plus how an organization regards this software by type.'
									},
									{
										xtype : 'combo',
										elementdata : true,
										fieldLabel : 'Phase',
										allowBlank : true,
										store : new CMDB.SoftwareLifecycleStore(),
										displayField : 'Name',
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : function(e) {
												e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
														+ e.query + '%\")]';
											}
										},

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Phase'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Phase');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Phase/$');
											this.setValue(value);
										}
									},
									{
										xtype : 'combo',
										elementdata : true,
										fieldLabel : 'Availability',
										allowBlank : true,
										store : new CMDB.TimeframeStore(),
										displayField : 'Name',
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : function(e) {
												e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
														+ e.query + '%\")]';
											}
										},

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Availability'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Availability');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Availability/$');
											this.setValue(value);
										}
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Software.LifecycleForm.superclass.initComponent
								.apply(this, arguments);
					}
				});
Ext.reg('softwareLifecycleForm', CMDB.Software.LifecycleForm);

/**
 * Software (Editor Form)
 */
CMDB.Software.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
								'ns2' : 'http://www.klistret.com/cmdb/ci/commons',
								'$' : 'http://www.klistret.com/cmdb/ci/pojo'
							},

							'type' : {
								'id' : {
									'$' : null
								},
								'name' : {
									'$' : null
								}
							},
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
									'xsi' : 'http://www.w3.org/2001/XMLSchema-instance'
								},
								'@xsi:type' : 'ns10:Software'
							}
						}
					},

					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'Software'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/component')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Software Editor',

							layout : 'accordion',

							height : 500,

							items : [
									{
										xtype : 'softwareIdentificationForm'
									},
									{
										xtype : 'softwareGeneralForm',
										tags : [ [ 'Third party' ],
												[ 'Open source' ],
												[ 'Commercial' ],
												[ 'Homegrown' ],
												[ 'Freeware' ], [ 'Firmware' ] ]
									},
									{
										xtype : 'softwareLifecycleForm'
									},
									{
										xtype : 'destRelationForm',
										title : 'Software dependencies',

										desktop : this.desktop,
										editor : CMDB.Software.Edit,

										information : 'Dependencies to other software.',

										fields : [
												{
													name : 'Id',
													mapping : 'Relation/id/$'
												},
												{
													name : 'Type',
													mapping : 'Relation/type/name/$'
												},
												{
													name : 'DestName',
													mapping : 'Relation/destination/name/$'
												},
												{
													name : 'DestType',
													mapping : 'Relation/destination/type/name/$'
												},
												{
													name : 'Created',
													mapping : 'Relation/createTimeStamp/$'
												},
												{
													name : 'Updated',
													mapping : 'Relation/updateTimeStamp/$'
												},
												{
													name : 'Relation',
													mapping : 'Relation'
												},
												{
													name : 'Destination',
													mapping : 'Relation/destination'
												},
												{
													name : 'Label',
													mapping : 'Relation/destination/configuration/Label/$'
												},
												{
													name : 'Version',
													mapping : 'Relation/destination/configuration/Version/$'
												},
												{
													name : 'Organization',
													mapping : 'Relation/destination/configuration/Organization/$'
												} ],

										columns : [ {
											header : 'Organization',
											width : 150,
											sortable : true,
											dataIndex : 'Organization'
										}, {
											header : 'Name',
											width : 150,
											sortable : true,
											dataIndex : 'DestName'
										}, {
											header : 'Version',
											width : 150,
											sortable : true,
											dataIndex : 'Version'
										}, {
											header : "Label",
											width : 200,
											sortable : true,
											dataIndex : 'Label'
										} ],

										recordCreator : function(fields,
												relation) {
											var recordDef = Ext.data.Record
													.create(fields);

											var record = new recordDef(
													{
														'Id' : CMDB.Badgerfish
																.get(relation,
																		'Relation/id/$'),
														'Type' : CMDB.Badgerfish
																.get(relation,
																		'Relation/type/name/$')
																.replace(
																		/\{.*\}(.*)/,
																		"$1"),
														'DestName' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/name/$'),
														'DestType' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/type/name/$')
																.replace(
																		/\{.*\}(.*)/,
																		"$1"),
														'Relation' : CMDB.Badgerfish
																.get(relation,
																		'Relation'),
														'Destination' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination'),
														'Label' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/configuration/Label/$'),
														'Version' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/configuration/Version/$'),
														'Organization' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/configuration/Organization/$')
													}, CMDB.Badgerfish.get(
															relation,
															'Relation/id/$'));

											return record;
										},

										relations : {
											'{http://www.klistret.com/cmdb/ci/element/component}Software' : '{http://www.klistret.com/cmdb/ci/relation}Dependency'
										}
									},
									{
										xtype : 'destRelationForm',
										title : 'Publications',

										desktop : this.desktop,
										editor : CMDB.Publication.Edit,

										information : 'Publications (artefacts) that this software produces.',

										fields : [
												{
													name : 'Id',
													mapping : 'Relation/id/$'
												},
												{
													name : 'Type',
													mapping : 'Relation/type/name/$'
												},
												{
													name : 'DestName',
													mapping : 'Relation/destination/name/$'
												},
												{
													name : 'DestType',
													mapping : 'Relation/destination/type/name/$'
												},
												{
													name : 'Created',
													mapping : 'Relation/createTimeStamp/$'
												},
												{
													name : 'Updated',
													mapping : 'Relation/updateTimeStamp/$'
												},
												{
													name : 'Relation',
													mapping : 'Relation'
												},
												{
													name : 'Destination',
													mapping : 'Relation/destination'
												},
												{
													name : 'Label',
													mapping : 'Relation/destination/configuration/Label/$'
												},
												{
													name : 'Type',
													mapping : 'Relation/destination/configuration/Type/$'
												} ],

										columns : [ {
											header : 'Name',
											width : 150,
											sortable : true,
											dataIndex : 'DestName'
										}, {
											header : 'Label',
											width : 150,
											sortable : true,
											dataIndex : 'Label'
										}, {
											header : "Type",
											width : 200,
											sortable : true,
											dataIndex : 'Type'
										} ],

										recordCreator : function(fields,
												relation) {
											var recordDef = Ext.data.Record
													.create(fields);

											var record = new recordDef(
													{
														'Id' : CMDB.Badgerfish
																.get(relation,
																		'Relation/id/$'),
														'Type' : CMDB.Badgerfish
																.get(relation,
																		'Relation/type/name/$')
																.replace(
																		/\{.*\}(.*)/,
																		"$1"),
														'DestName' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/name/$'),
														'DestType' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/type/name/$')
																.replace(
																		/\{.*\}(.*)/,
																		"$1"),
														'Relation' : CMDB.Badgerfish
																.get(relation,
																		'Relation'),
														'Destination' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination'),
														'Label' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/configuration/Label/$'),
														'Type' : CMDB.Badgerfish
																.get(relation,
																		'Relation/destination/configuration/Type/$')
													}, CMDB.Badgerfish.get(
															relation,
															'Relation/id/$'));

											return record;
										},

										relations : {
											'{http://www.klistret.com/cmdb/ci/element/component}Publication' : '{http://www.klistret.com/cmdb/ci/relation}Dependency'
										}
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Software.Edit.superclass.initComponent.apply(this,
								arguments);
					}
				});

/**
 * Software (Search Form)
 */
CMDB.Software.Search = Ext
		.extend(
				CMDB.Element.Search,
				{

					initComponent : function() {
						var form = new Ext.form.FormPanel(
								{
									border : false,
									bodyStyle : 'padding:10px; background-color:white;',
									baseCls : 'x-plain',
									labelAlign : 'top',

									items : [
											{
												xtype : 'displayfield',
												width : 'auto',
												'html' : 'Search criteria for Software items.'
											},
											{
												layout : 'column',
												border : false,

												items : [
														{
															columnWidth : .5,
															layout : 'form',
															border : false,
															defaults : {
																width : 300
															},

															items : [
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Organization',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = {0}]',
																		store : new CMDB.OrganizationStore(),
																		queryParam : 'expressions',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'remote',
																		forceSelection : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			'beforequery' : function(
																					e) {
																				e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
																						+ e.query
																						+ '%\")]';
																			}
																		}
																	},
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Name',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name = {0}]',
																		store : new CMDB.SoftwareContextStore(),
																		queryParam : 'expressions',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'remote',
																		forceSelection : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			'beforequery' : function(
																					e) {
																				e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
																						+ e.query
																						+ '%\")]';
																			}
																		}
																	},
																	{
																		xtype : 'textfield',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Version',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Version eq \"{0}\"]'
																	},
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Availability',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Availability = {0}]',
																		store : new CMDB.TimeframeStore(),
																		queryParam : 'expressions',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'remote',
																		forceSelection : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			'beforequery' : function(
																					e) {
																				e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
																						+ e.query
																						+ '%\")]';
																			}
																		}
																	} ]
														},
														{
															columnWidth : .5,
															layout : 'form',
															border : false,
															defaults : {
																width : 300
															},

															items : [
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Tags',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration[commons:Tag = {0}]',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'local',

																		store : new Ext.data.SimpleStore(
																				{
																					fields : [ 'Name' ],
																					sortInfo : {
																						field : 'Name',
																						direction : 'ASC'
																					}
																				}),

																		allowAddNewData : true,
																		addNewDataOnBlur : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			newitem : function(
																					bs,
																					v,
																					f) {
																				var newObj = {
																					Name : v
																				};
																				bs
																						.addItem(newObj);
																			}
																		}
																	},
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Environment (through application assoications)',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:destinationRelations[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Composition\"]/pojo:source[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]/pojo:configuration[element:Environment = {0}]',
																		store : new CMDB.EnvironmentStore(),
																		queryParam : 'expressions',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'remote',
																		forceSelection : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			'beforequery' : function(
																					e) {
																				e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
																						+ e.query
																						+ '%\")]';
																			}
																		}
																	},
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Phase',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Phase = {0}]',
																		store : new CMDB.SoftwareLifecycleStore(),
																		queryParam : 'expressions',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'remote',
																		forceSelection : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			'beforequery' : function(
																					e) {
																				e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
																						+ e.query
																						+ '%\")]';
																			}
																		}
																	},
																	{
																		xtype : 'datefield',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Created after',
																		format : 'Y-m-d',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"{0}\" cast as xs:dateTime]'
																	},
																	{
																		xtype : 'datefield',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Created before',
																		format : 'Y-m-d',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp lt \"{0}\" cast as xs:dateTime]'
																	} ]
														} ]
											} ]
								});

						var config = {
							title : 'Software Search',
							editor : CMDB.Software.Edit,

							height : 450,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/component}Software',

							items : form,

							fields : [
									{
										name : 'Id',
										mapping : 'Element/id/$'
									},
									{
										name : 'Name',
										mapping : 'Element/name/$'
									},
									{
										name : 'Organization',
										mapping : 'Element/configuration/Organization/$'
									},
									{
										name : 'Version',
										mapping : 'Element/configuration/Version/$'
									},
									{
										name : 'Label',
										mapping : 'Element/configuration/Label/$'
									},
									{
										name : 'Availability',
										mapping : 'Element/configuration/Availability/$'
									}, {
										name : 'Created',
										mapping : 'Element/createTimeStamp/$'
									}, {
										name : 'Updated',
										mapping : 'Element/updateTimeStamp/$'
									}, {
										name : 'Element',
										mapping : 'Element'
									} ],

							columns : [ {
								header : 'Name',
								width : 120,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : 'Organization',
								width : 200,
								sortable : true,
								dataIndex : 'Organization'
							}, {
								header : 'Version',
								width : 120,
								sortable : true,
								dataIndex : 'Version'
							}, {
								header : 'Label',
								width : 200,
								sortable : true,
								dataIndex : 'Label'
							}, {
								header : 'Availability',
								width : 120,
								sortable : true,
								dataIndex : 'Availability'
							}, {
								header : "Created",
								width : 120,
								sortable : true,
								dataIndex : 'Created'
							}, {
								header : "Last Updated",
								width : 120,
								sortable : true,
								dataIndex : 'Updated'
							} ]
						}

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Software.Search.superclass.initComponent.apply(
								this, arguments);
					}
				});

/**
 * Publication - Publication form forcing associations to type and extension.
 * 
 */
CMDB.Publication.GeneralForm = Ext
		.extend(
				Ext.form.FormPanel,
				{
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
									{
										xtype : 'displayfield',
										width : 'auto',
										'html' : 'Every publication has a type representing why the publication exists and an extension informing how the publication could be processed.'
									},
									{
										xtype : 'combo',
										elementdata : true,
										fieldLabel : 'Type',
										allowBlank : false,
										blankText : 'Type is required',
										store : new CMDB.PublicationTypeStore(),
										displayField : 'Name',
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : function(e) {
												e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
														+ e.query + '%\")]';
											}
										},

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':PublicationType'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Type');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Type/$');
											this.setValue(value);
										}
									},
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Extension',
										allowBlank : false,
										blankText : 'Enter an extension',

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Extension'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Extension');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Extension/$');
											this.setValue(value);
										}
									},
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
						CMDB.Publication.GeneralForm.superclass.initComponent
								.apply(this, arguments);
					}
				});
Ext.reg('publicationGeneralForm', CMDB.Publication.GeneralForm);

CMDB.Publication.IdentificationForm = Ext
		.extend(
				Ext.form.FormPanel,
				{
					initComponent : function() {
						var config = {
							title : 'Identification',
							autoScroll : true,
							labelAlign : 'top',
							bodyStyle : 'padding:10px; background-color:white;',
							defaults : {
								width : 300
							},

							items : [
									{
										xtype : 'displayfield',
										width : 'auto',
										'html' : 'Publications like software have an organization, a version and likely a label identifying the artefact or package.'
									},
									{
										xtype : 'combo',
										elementdata : true,
										fieldLabel : 'Organization',
										allowBlank : false,
										blankText : 'Organization is required',
										store : new CMDB.OrganizationStore(),
										displayField : 'Name',
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : function(e) {
												e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
														+ e.query + '%\")]';
											}
										},

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Organization'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Organization');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Organization/$');
											this.setValue(value);
										}
									},
									{
										xtype : 'combo',
										elementdata : true,
										fieldLabel : 'Name',
										allowBlank : false,
										blankText : 'Name is required',
										store : new CMDB.PublicationContextStore(),
										displayField : 'Name',
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : function(e) {
												e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
														+ e.query + '%\")]';
											}
										},

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
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Version',
										allowBlank : false,
										blankText : 'Enter a version',

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Version'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Version');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Version/$');
											this.setValue(value);
										}
									},
									{
										xtype : 'textfield',
										elementdata : true,
										fieldLabel : 'Labels are like aliases but not necessary (should mirror a combination of the publication name and version)',
										allowBlank : true,

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/component');
												element['Element']['configuration'][prefix
														+ ':Label'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Label');
											}
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Label/$');
											this.setValue(value);
										}
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Publication.IdentificationForm.superclass.initComponent
								.apply(this, arguments);
					}
				});
Ext.reg('publicationIdentificationForm', CMDB.Publication.IdentificationForm);

/**
 * Publication (Editor Form)
 */
CMDB.Publication.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
								'ns2' : 'http://www.klistret.com/cmdb/ci/commons',
								'$' : 'http://www.klistret.com/cmdb/ci/pojo'
							},

							'type' : {
								'id' : {
									'$' : null
								},
								'name' : {
									'$' : null
								}
							},
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
									'xsi' : 'http://www.w3.org/2001/XMLSchema-instance'
								},
								'@xsi:type' : 'ns10:Publication'
							}
						}
					},

					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'Publication'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/component')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Publication Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'publicationIdentificationForm'
									},
									{
										xtype : 'publicationGeneralForm',
										helpInfo : 'Publications are artefacts or packages associated with software.',
										tags : [ [ 'Third party' ],
												[ 'Open source' ],
												[ 'Commercial' ],
												[ 'Homegrown' ],
												[ 'Freeware' ], [ 'Firmware' ] ]
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Publication.Edit.superclass.initComponent.apply(
								this, arguments);
					}
				});

/**
 * Publication (Search Form)
 */
CMDB.Publication.Search = Ext
		.extend(
				CMDB.Element.Search,
				{

					initComponent : function() {
						var form = new Ext.form.FormPanel(
								{
									border : false,
									bodyStyle : 'padding:10px; background-color:white;',
									baseCls : 'x-plain',
									labelAlign : 'top',

									items : [
											{
												xtype : 'displayfield',
												width : 'auto',
												'html' : 'Search criteria for Publication items.'
											},
											{
												layout : 'column',
												border : false,

												items : [
														{
															columnWidth : .5,
															layout : 'form',
															border : false,
															defaults : {
																width : 300
															},

															items : [
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Organization',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = {0}]',
																		store : new CMDB.OrganizationStore(),
																		queryParam : 'expressions',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'remote',
																		forceSelection : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			'beforequery' : function(
																					e) {
																				e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
																						+ e.query
																						+ '%\")]';
																			}
																		}
																	},
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Name',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name = {0}]',
																		store : new CMDB.PublicationContextStore(),
																		queryParam : 'expressions',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'remote',
																		forceSelection : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			'beforequery' : function(
																					e) {
																				e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
																						+ e.query
																						+ '%\")]';
																			}
																		}
																	},
																	{
																		xtype : 'textfield',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Version',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Version eq \"{0}\"]'
																	},
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Type',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Type = {0}]',
																		store : new CMDB.PublicationTypeStore(),
																		queryParam : 'expressions',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'remote',
																		forceSelection : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			'beforequery' : function(
																					e) {
																				e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
																						+ e.query
																						+ '%\")]';
																			}
																		}
																	},
																	{
																		xtype : 'textfield',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Extension',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Extension eq \"{0}\"]'
																	} ]
														},
														{
															columnWidth : .5,
															layout : 'form',
															border : false,
															defaults : {
																width : 300
															},

															items : [
																	{
																		xtype : 'superboxselect',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Tags',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration[commons:Tag = {0}]',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'local',

																		store : new Ext.data.SimpleStore(
																				{
																					fields : [ 'Name' ],
																					sortInfo : {
																						field : 'Name',
																						direction : 'ASC'
																					}
																				}),

																		allowAddNewData : true,
																		addNewDataOnBlur : true,

																		extraItemCls : 'x-tag',

																		listeners : {
																			newitem : function(
																					bs,
																					v,
																					f) {
																				var newObj = {
																					Name : v
																				};
																				bs
																						.addItem(newObj);
																			}
																		}
																	},
																	{
																		xtype : 'datefield',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Created after',
																		format : 'Y-m-d',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"{0}\" cast as xs:dateTime]'
																	},
																	{
																		xtype : 'datefield',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Created before',
																		format : 'Y-m-d',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp lt \"{0}\" cast as xs:dateTime]'
																	} ]
														} ]
											} ]
								});

						var config = {
							title : 'Publication Search',
							editor : CMDB.Publication.Edit,

							height : 450,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/component}Publication',

							items : form,

							fields : [
									{
										name : 'Id',
										mapping : 'Element/id/$'
									},
									{
										name : 'Name',
										mapping : 'Element/name/$'
									},
									{
										name : 'Organization',
										mapping : 'Element/configuration/Organization/$'
									},
									{
										name : 'Version',
										mapping : 'Element/configuration/Version/$'
									},
									{
										name : 'Type',
										mapping : 'Element/configuration/Type/$'
									},
									{
										name : 'Extension',
										mapping : 'Element/configuration/Extension/$'
									},
									{
										name : 'Label',
										mapping : 'Element/configuration/Label/$'
									}, {
										name : 'Created',
										mapping : 'Element/createTimeStamp/$'
									}, {
										name : 'Updated',
										mapping : 'Element/updateTimeStamp/$'
									}, {
										name : 'Element',
										mapping : 'Element'
									} ],

							columns : [ {
								header : 'Name',
								width : 120,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : 'Organization',
								width : 200,
								sortable : true,
								dataIndex : 'Organization'
							}, {
								header : 'Version',
								width : 120,
								sortable : true,
								dataIndex : 'Version'
							}, {
								header : 'Type',
								width : 200,
								sortable : true,
								dataIndex : 'Type'
							}, {
								header : 'Label',
								width : 200,
								sortable : true,
								dataIndex : 'Label'
							}, {
								header : 'Extension',
								width : 120,
								sortable : true,
								dataIndex : 'Extension'
							}, {
								header : "Created",
								width : 120,
								sortable : true,
								dataIndex : 'Created'
							}, {
								header : "Last Updated",
								width : 120,
								sortable : true,
								dataIndex : 'Updated'
							} ]
						}

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Publication.Search.superclass.initComponent.apply(
								this, arguments);
					}
				});