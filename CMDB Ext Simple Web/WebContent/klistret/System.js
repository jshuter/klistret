Ext.namespace('CMDB.System');
Ext.namespace('CMDB.Application');

/**
 * System states as store
 */
CMDB.System.StateStore = new Ext.data.ArrayStore(
		{
			fields : [ 'Name', 'Description' ],
			data : [
					[ 'Online', 'System is online or active' ],
					[ 'Offline', 'System is offline or inactive' ],
					[ 'Transition',
							'System is in transation either to an online or offline state' ] ]
		});

/**
 * System (general form)
 */
CMDB.System.GeneralForm = Ext
		.extend(
				Ext.form.FormPanel,
				{

					initComponent : function() {
						var config = {
							title : 'System',
							autoScroll : true,
							labelAlign : 'top',
							bodyStyle : 'padding:10px; background-color:white;',
							defaults : {
								width : 300
							},

							items : [
									{
										xtype : 'superboxselect',
										elementdata : true,
										fieldLabel : 'Environments',
										allowBlank : true,
										store : new CMDB.EnvironmentStore(),
										queryParam : 'expressions',
										displayField : 'Name',
										valueField : 'Name',
										mode : 'remote',
										forceSelection : true,

										allowAddNewData : true,

										extraItemCls : 'x-tag',

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
											if (!Ext.isEmpty(this.getValueEx())
													&& element['Element']['configuration']) {
												var ele = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element')
												environments = [];

												Ext
														.each(
																this
																		.getValueEx(),
																function(value) {
																	var environment = {
																		'$' : value['Name']
																	};

																	environments[environments.length] = environment;
																});

												element['Element']['configuration'][ele
														+ ":Environment"] = environments;
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/Environment');
											}
										},

										// Read from JSON into object
										unmarshall : function(element) {
											var environments = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Environment'), formated = [];

											if (Ext.isArray(environments)) {
												Ext
														.each(
																environments,
																function(
																		environment) {
																	formated[formated.length] = {
																		'Name' : environment['$']
																	};
																});
											}

											if (Ext.isObject(environments)) {
												formated[formated.length] = {
													'Name' : environments['$']
												};
											}

											this.setValueEx(formated);
										}
									},
									{
										xtype : 'combo',
										elementdata : true,
										fieldLabel : 'State',
										allowBlank : false,
										blankText : 'State is required',
										store : CMDB.System.StateStore,
										displayField : 'Name',
										mode : 'local',
										forceSelection : true,

										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element');
												element['Element']['configuration'][prefix
														+ ':State'] = {
													'$' : this.getValue()
												};
											} else {
												CMDB.Badgerfish
														.remove(element,
																'Element/configuration/State');
											}
										},
										unmarshall : function(element) {
											var value = CMDB.Badgerfish
													.get(element,
															'Element/configuration/State/$');
											this.setValue(value);
										}
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.System.GeneralForm.superclass.initComponent.apply(
								this, arguments);
					}
				});
Ext.reg('systemGeneralForm', CMDB.System.GeneralForm);

/**
 * Application (Editor Form)
 */
CMDB.Application.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
								'ns11' : 'http://www.klistret.com/cmdb/ci/element/system',
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
								'@xsi:type' : 'ns11:Application'
							}
						}
					},

					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'Application'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/system')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Application Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'generalForm',
										helpInfo : 'An application is a runtime conglomeration of software within an application system and is a managed object.',
										tags : [ [ 'Third party' ],
												[ 'Open source' ],
												[ 'Commercial' ],
												[ 'Homegrown' ],
												[ 'Freeware' ], [ 'Firmware' ] ]
									},
									{
										xtype : 'systemGeneralForm'
									},
									{
										xtype : 'destRelationForm',
										relations : [ {
											'{http://www.klistret.com/cmdb/ci/element/component/software}ApplicationSoftware' : '{http://www.klistret.com/cmdb/ci/relation}Composition'
										} ]
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Application.Edit.superclass.initComponent.apply(
								this, arguments);
					}
				});

/**
 * Application (Search Form)
 */
CMDB.Application.Search = Ext
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
												'html' : 'Search criteria for Application items.  Regular expressions may used used with all text fields other than the name field.'
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
																		xtype : 'textfield',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Name',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]',
																		wildcard : '%'
																	},
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
																		fieldLabel : 'Composed of an application software named',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:sourceRelations[empty(pojo:toTimeStamp)]/pojo:destination[pojo:name = {0}]',
																		store : CMDB.ApplicationSoftwareStore,
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
																		fieldLabel : 'Availability of related appliation software',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:sourceRelations[empty(pojo:toTimeStamp)]/pojo:destination/pojo:configuration[sw:Availability = {0}]',
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
																		fieldLabel : 'Environment',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:configuration[element:Environment = {0}]',
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
							title : 'Application Search',
							editor : CMDB.Application.Edit,

							height : 400,
							width : 800,
							
							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/system}Application',

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
										name : 'Description',
										mapping : 'Element/configuration/Description/$'
									},
									{
										name : 'Tag',
										mapping : 'Element/configuration/Tag',
										formating : function(values) {
											var formated = '';

											Ext
													.each(
															values,
															function(value) {
																formated = Ext
																		.isEmpty(formated) ? value['$']
																		: formated
																				+ ', '
																				+ value['$'];
															});
											return formated;
										}
									},
									{
										name : 'Environment',
										mapping : 'Element/configuration/Environment',
										formating : function(values) {
											var formated = '';

											Ext
													.each(
															values,
															function(value) {
																formated = Ext
																		.isEmpty(formated) ? value['$']
																		: formated
																				+ ', '
																				+ value['$'];
															});
											return formated;
										}
									},
									{
										name : 'State',
										mapping : 'Element/configuration/State/$'
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
								width : 200,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : "Environments",
								width : 200,
								sortable : true,
								dataIndex : 'Environment'
							}, {
								header : "State",
								width : 120,
								sortable : true,
								dataIndex : 'State'
							}, {
								header : "Tags",
								width : 120,
								sortable : true,
								dataIndex : 'Tag'
							}, {
								header : "Description",
								width : 120,
								sortable : true,
								dataIndex : 'Description'
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
						CMDB.Application.Search.superclass.initComponent.apply(
								this, arguments);
					}
				});