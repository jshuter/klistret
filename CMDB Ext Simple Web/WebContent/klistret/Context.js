Ext.namespace('CMDB.Environment');
Ext.namespace('CMDB.Organization');
Ext.namespace('CMDB.SoftwareContext');
Ext.namespace('CMDB.PublicationContext');
Ext.namespace('CMDB.PublicationType');
Ext.namespace('CMDB.SoftwareLifecycle');
Ext.namespace('CMDB.Timeframe');

/**
 * Environment Editor
 */
CMDB.Environment.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns8' : 'http://www.klistret.com/cmdb/ci/commons',
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/context',
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
								'@xsi:type' : 'ns10:Environment',
								'ns8:Ownership' : {
									'ns8:Contact' : {}
								}
							}
						}
					},

					/**
					 * 
					 */
					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'Environment'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/context')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Environment Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'generalForm',
										helpInfo : 'An environment is a collection of logical systems that represent an entire production, test or development IT landspace.',
										tags : [ [ 'Production' ], [ 'Test' ],
												[ 'Development' ],
												[ 'Verification' ],
												[ 'Sandbox' ], [ 'POC' ] ]
									},
									{
										xtype : 'contactForm',
										helpInfo : 'Contact or ownership information is not required but helpful in locating responsibility for the Environment CI.  The contact name may also be an organization.'
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Environment.Edit.superclass.initComponent.apply(
								this, arguments);
					}
				});

/**
 * Environment Search
 */
CMDB.Environment.Search = Ext
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
												'html' : 'Search for Environments'
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
							title : 'Environment Search',
							editor : CMDB.Environment.Edit,

							height : 300,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/context}Environment',

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
										name : 'Contact Name',
										mapping : 'Element/configuration/Ownership/Contact/Name/$'
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
								header : "Name",
								width : 200,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : "Tags",
								width : 200,
								sortable : true,
								dataIndex : 'Tag'
							}, {
								header : "Description",
								width : 200,
								sortable : true,
								dataIndex : 'Description'
							}, {
								header : "Contact Name",
								width : 120,
								sortable : true,
								dataIndex : 'Contact Name'
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
						CMDB.Environment.Search.superclass.initComponent.apply(
								this, arguments);
					}
				});

/**
 * Organization Editor
 */
CMDB.Organization.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/context',
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
								'@xsi:type' : 'ns10:Organization'
							}
						}
					},

					/**
					 * 
					 */
					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'Organization'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/context')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Organization Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'generalForm',
										helpInfo : 'An organisation is either a company, an individual, or simply any group of people.',
										tags : [ [ 'Project' ], [ 'Section' ],
												[ 'Department' ],
												[ 'Activity' ], [ 'Team' ] ]
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Organization.Edit.superclass.initComponent.apply(
								this, arguments);
					}
				});

/**
 * Organization Search
 */
CMDB.Organization.Search = Ext
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
												'html' : 'Search for Organizations.'
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
							title : 'Organization Search',
							editor : CMDB.Organization.Edit,

							height : 300,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/context}Organization',

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
								header : "Name",
								width : 200,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : "Tags",
								width : 200,
								sortable : true,
								dataIndex : 'Tag'
							}, {
								header : "Description",
								width : 200,
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
						CMDB.Organization.Search.superclass.initComponent
								.apply(this, arguments);
					}
				});


/**
 * Software context Editor
 */
CMDB.SoftwareContext.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/context',
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

					/**
					 * 
					 */
					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'Software'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/context')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Software Context Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'generalForm',
										helpInfo : 'Software contexts are logical reprensent of software mainly used to store metadata across software versions.',
										tags : [ [ 'Middleware' ],
												[ 'Educational' ],
												[ 'Simulation' ],
												[ 'Content access' ],
												[ 'Media devleopment' ],
												[ 'Enterprise' ] ]
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.SoftwareContext.Edit.superclass.initComponent.apply(this,
								arguments);
					}
				});


/**
 * Software Context Search
 */
CMDB.SoftwareContext.Search = Ext
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
												'html' : 'Search for Software contexts.'
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
							title : 'Software Context Search',
							editor : CMDB.SoftwareContext.Edit,

							height : 300,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/context}Software',

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
								header : "Name",
								width : 200,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : "Tags",
								width : 200,
								sortable : true,
								dataIndex : 'Tag'
							}, {
								header : "Description",
								width : 200,
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
						CMDB.SoftwareContext.Search.superclass.initComponent.apply(this,
								arguments);
					}
				});


/**
 * Publication context Editor
 */
CMDB.PublicationContext.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/context',
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

					/**
					 * 
					 */
					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'Publication'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/context')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Software Context Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'generalForm',
										helpInfo : 'Publication contexts are logical reprensent of publications mainly used to store metadata across publication versions.',
										tags : [ [ 'Middleware' ],
												[ 'Educational' ],
												[ 'Simulation' ],
												[ 'Content access' ],
												[ 'Media devleopment' ],
												[ 'Enterprise' ] ]
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.PublicationContext.Edit.superclass.initComponent.apply(this,
								arguments);
					}
				});


/**
 * Publication Context Search
 */
CMDB.PublicationContext.Search = Ext
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
												'html' : 'Search for Publication contexts.'
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
							title : 'Publication Context Search',
							editor : CMDB.PublicationContext.Edit,

							height : 300,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/context}Publication',

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
								header : "Name",
								width : 200,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : "Tags",
								width : 200,
								sortable : true,
								dataIndex : 'Tag'
							}, {
								header : "Description",
								width : 200,
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
						CMDB.PublicationContext.Search.superclass.initComponent.apply(this,
								arguments);
					}
				});


/**
 * Software Lifecyle Editor
 */
CMDB.SoftwareLifecycle.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
								'ns11' : 'http://www.klistret.com/cmdb/ci/element/context/lifecycle',
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
								'@xsi:type' : 'ns11:SoftwareLifecycle'
							}
						}
					},

					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'SoftwareLifecycle'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/context/lifecycle')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Software Lifecycle Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'generalForm',
										helpInfo : 'The software life cycle is composed of discrete phases that describe the software\'s maturity as it advances from planning and development to release and support phases.',
										tags : []
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.SoftwareLifecycle.Edit.superclass.initComponent
								.apply(this, arguments);
					}
				});

/**
 * Software Lifecycle (Search)
 */
CMDB.SoftwareLifecycle.Search = Ext
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
												'html' : 'Search for Software lifecycles.'
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
							title : 'Software Lifecycle Search',
							editor : CMDB.SoftwareLifecycle.Edit,

							height : 300,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/context/lifecycle}SoftwareLifecycle',

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
								header : "Name",
								width : 200,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : "Tags",
								width : 200,
								sortable : true,
								dataIndex : 'Tag'
							}, {
								header : "Description",
								width : 200,
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
						CMDB.SoftwareLifecycle.Search.superclass.initComponent
								.apply(this, arguments);
					}
				});

/**
 * Timeframe (Editor)
 */
CMDB.Timeframe.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
								'ns11' : 'http://www.klistret.com/cmdb/ci/element/context',
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
								'@xsi:type' : 'ns11:Timeframe'
							}
						}
					},

					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'Timeframe'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/context')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Timeframe Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'generalForm',
										helpInfo : 'Time frames can be both short or long term representing organizational milestones.',
										tags : []
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.Timeframe.Edit.superclass.initComponent.apply(
								this, arguments);
					}
				});

/**
 * Timeframe (Search)
 */
CMDB.Timeframe.Search = Ext
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
												'html' : 'Search for timeframes.'
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
							title : 'Timeframe Search',
							editor : CMDB.Timeframe.Edit,

							height : 300,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/context}Timeframe',

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
								header : "Name",
								width : 120,
								sortable : true,
								dataIndex : 'Name'
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
						CMDB.Timeframe.Search.superclass.initComponent.apply(
								this, arguments);
					}
				});

/**
 * PublicationType Editor
 */
CMDB.PublicationType.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/context',
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
								'@xsi:type' : 'ns10:PublicationType'
							}
						}
					},

					/**
					 * 
					 */
					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'PublicationType'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/context')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Publication Type Editor',

							layout : 'accordion',

							items : [
									{
										xtype : 'generalForm',
										helpInfo : 'Organizations may privately type publications to denote functional characteristics internal to the organization.  For example, a database update script might be typed as DBSCRIPT.',
										tags : [ [ 'Packaging' ],
												[ 'Delivery' ] ]
									}, {
										xtype : 'propertyForm'
									} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.PublicationType.Edit.superclass.initComponent
								.apply(this, arguments);
					}
				});

/**
 * PublicationType Search
 */
CMDB.PublicationType.Search = Ext
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
												'html' : 'Search for Publication types.'
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
							title : 'Publication Type Search',
							editor : CMDB.PublicationType.Edit,

							height : 300,
							width : 800,

							autoScroll : false,
							
							elementType : '{http://www.klistret.com/cmdb/ci/element/context}PublicationType',

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
								header : "Name",
								width : 200,
								sortable : true,
								dataIndex : 'Name'
							}, {
								header : "Tags",
								width : 200,
								sortable : true,
								dataIndex : 'Tag'
							}, {
								header : "Description",
								width : 200,
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
						CMDB.PublicationType.Search.superclass.initComponent
								.apply(this, arguments);
					}
				});