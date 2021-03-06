Ext.namespace('CMDB.Change');
Ext.namespace('CMDB.SoftwareInstallation');

/**
 * System states as store
 */
CMDB.Change.StateStore = new Ext.data.ArrayStore(
		{
			fields : [ 'Name', 'Description' ],
			data : [
					[ 'Planned', 'No activity as yet, still in planning phase' ],
					[ 'In Progress', 'Change is being worked on' ],
					[ 'Waiting', 'Change is on hold' ],
					[ 'Completed',
							'All activities regarding the changed have successfully been completed' ],
					[ 'Failed', 'Failure to handle the change' ],
					[ 'Canceled', 'Changed has been discarded or canceled' ] ]
		});

/**
 * Software Installation (general form)
 * 
 */
CMDB.SoftwareInstallation.GeneralForm = Ext
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
									{
										xtype : 'displayfield',
										width : 'auto',
										'html' : 'A software installation targets an environment where software and even a publication is to be instllated.'
									},
									{
										xtype : 'combo',
										ref : 'Environment',
										elementdata : true,
										fieldLabel : 'Target environment',
										allowBlank : false,
										blankText : 'Target is required',
										store : new CMDB.EnvironmentStore(),
										displayField : 'Name',
										valueField : 'Id',
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										allowAddNewData : true,

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
													&& !this.disabled
													&& element['Element']['configuration']) {
												var change = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/process/change'), commons = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/commons'), record = this
														.getStore()
														.getById(
																this.getValue());

												element['Element']['configuration'][change
														+ ':Environment'] = {};
												element['Element']['configuration'][change
														+ ':Environment'][commons
														+ ':Id'] = {
													'$' : record.get('Id')
												};
												element['Element']['configuration'][change
														+ ':Environment'][commons
														+ ':Name'] = {
													'$' : record.get('Name')
												};
												element['Element']['configuration'][change
														+ ':Environment'][commons
														+ ':QName'] = {
													'$' : CMDB.Badgerfish
															.get(
																	record
																			.get('Element'),
																	'type/name/$')
												};
											}
											// Removal logic erased (not sure
											// why it existed?)
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var id = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Environment/Id/$'), data = {
												'Id' : id,
												'Name' : CMDB.Badgerfish
														.get(element,
																'Element/configuration/Environment/Name/$')
											};

											var record = new (this.store.reader).recordType(
													data, id);
											this.getStore().insert(0, record);

											this.setValue(id);
										}
									},
									{
										xtype : 'combo',
										ref : 'Name',
										fieldLabel : 'Name',
										allowBlank : false,
										blankText : 'Name is required',
										store : new CMDB.SoftwarePublicationContextStore(),
										displayField : 'Name',
										valueField : 'Name',
										itemSelector : 'div.name-item',
										tpl : new Ext.XTemplate(
												'<tpl for="."><div class="name-item">',
												'<h3><span>{Type}<br /></span>{Name}</h3></div></tpl>'),
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,

										allowAddNewData : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : function(e) {
												e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"'
														+ e.query + '%\")]';
											},

											'invalid' : {
												fn : function(field, msg) {
													this.Version.disable();
													this.Version.clearInvalid();
													this.Version.clearValue();
												},
												scope : this
											},

											'select' : {
												fn : function(combo, record,
														index) {
													this.Version.enable();
													this.Version.clearInvalid();
													this.Version.clearValue();
												},
												scope : this
											}
										}
									},
									{
										xtype : 'combo',
										ref : 'Version',
										elementdata : true,
										fieldLabel : 'Version',
										allowBlank : false,
										blankText : 'Version is required',
										store : new CMDB.SoftwarePublicationStore(),
										displayField : 'Version',
										valueField : 'Id',
										itemSelector : 'div.version-item',
										tpl : new Ext.XTemplate(
												'<tpl for="."><div class="version-item">',
												'<h3><span>{Created:date("M j, Y")}<br /></span>{Version}</h3>',
												'<tpl if="Label!=null "><p>Label: {Label}</p></tpl>', '</div></tpl>'),
										mode : 'remote',
										queryParam : 'expressions',
										forceSelection : true,
										disabled : true,

										allowAddNewData : true,

										// Edit the query for the combo into an
										// expression
										listeners : {
											'beforequery' : {
												fn : function(e) {
													if (this.Name.getValue()) {
														e.query = 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element[pojo:name eq \"'
																+ this.Name
																		.getValue()
																+ '\"]/pojo:configuration[matches(component:Version,\"'
																+ e.query
																+ '\")]';
													} else {
														return false;
													}
												},
												scope : this
											},

											'change' : {
												fn : function(field, newValue,
														oldValue, element) {
													if (!this.Name.getValue()
															&& element) {
														var name = CMDB.Badgerfish
																.get(element,
																		'Element/configuration/Software/Name/$');
														var data = {
															'Id' : 0,
															'Name' : name
														};

														var record = new (this.Name.store.reader).recordType(
																data, name);
														this.Name.getStore()
																.insert(0,
																		record);

														this.Name
																.setValue(name);
													}
												},
												scope : this
											}
										},

										// Marshall combo into the element
										marshall : function(element) {
											if (this.getValue()
													&& !this.disabled
													&& element['Element']['configuration']) {
												var change = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/process/change')

												var commons = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/commons')

												var record = this
														.getStore()
														.getAt(
																this.selectedIndex);

												element['Element']['configuration'][change
														+ ':Software'] = {};
												element['Element']['configuration'][change
														+ ':Software'][commons
														+ ':Id'] = {
													'$' : record.get('Id')
												};
												element['Element']['configuration'][change
														+ ':Software'][commons
														+ ':Name'] = {
													'$' : record.get('Name')
												};
												element['Element']['configuration'][change
														+ ':Software'][commons
														+ ':QName'] = {
													'$' : CMDB.Badgerfish
															.get(
																	record
																			.get('Element'),
																	'type/name/$')
												};
												if (record.get('Label')) {
													element['Element']['configuration'][change
															+ ':Label'] = {
														'$' : record
																.get('Label')
													};
												}
												element['Element']['configuration'][change
														+ ':Version'] = {
													'$' : record.get('Version')
												};
												element['Element']['name'] = {
													'$' : record.get('Name')
												};
												element['Element']['configuration']['Name'] = {
													'$' : record.get('Name')
												};
											}
											// Removal logic erased (not sure
											// why it existed?)
										},

										// Unmarshall element value into the
										// combo
										unmarshall : function(element) {
											var id = CMDB.Badgerfish
													.get(element,
															'Element/configuration/Software/Id/$');

											var data = {
												'Id' : id,
												'Name' : CMDB.Badgerfish.get(
														element,
														'Element/name/$'),
												'Version' : CMDB.Badgerfish
														.get(element,
																'Element/configuration/Version/$'),
												'Label' : CMDB.Badgerfish
														.get(element,
																'Element/configuration/Label/$'),
												'Element' : CMDB.Badgerfish
														.get(element, 'Element')
											};

											var record = new (this.store.reader).recordType(
													data, id);
											this.getStore().insert(0, record);

											this.setValue(id);

											this.fireEvent('change', this, id,
													null, element);
										}
									},
									{
										xtype : 'combo',

										ref : 'State',

										elementdata : true,
										fieldLabel : 'State',
										allowBlank : false,
										blankText : 'State is required',
										store : CMDB.Change.StateStore,
										displayField : 'Name',
										mode : 'local',
										value : 'Planned',
										forceSelection : true,
										disabled : true,

										marshall : function(element) {
											if (this.getValue()
													&& element['Element']['configuration']) {
												var prefix = CMDB.Badgerfish
														.getPrefix(element,
																'http://www.klistret.com/cmdb/ci/element/process');
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
						CMDB.SoftwareInstallation.GeneralForm.superclass.initComponent
								.apply(this, arguments);
					}
				});
Ext.reg('softwareInstallationGeneralForm',
		CMDB.SoftwareInstallation.GeneralForm);

/**
 * SoftwareInstallation (Editor Form)
 */
CMDB.SoftwareInstallation.Edit = Ext
		.extend(
				CMDB.Element.Edit,
				{
					element : {
						'Element' : {
							'@xmlns' : {
								'ns9' : 'http://www.klistret.com/cmdb/ci/element',
								'ns10' : 'http://www.klistret.com/cmdb/ci/element/component',
								'ns8' : 'http://www.klistret.com/cmdb/ci/element/process',
								'ns11' : 'http://www.klistret.com/cmdb/ci/element/process/change',
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
								'@xsi:type' : 'ns11:SoftwareInstallation'
							}
						}
					},

					initComponent : function() {
						var index = CMDB.ElementTypes
								.findBy(function(record, id) {
									if (record.get('Name') == 'SoftwareInstallation'
											&& record.get('Namespace') == 'http://www.klistret.com/cmdb/ci/element/process/change')
										return true;
									else
										return false;
								}), type = CMDB.ElementTypes.getAt(index).get(
								'ElementType');

						this.element['Element']['type']['id']['$'] = type['id']['$'];
						this.element['Element']['type']['name']['$'] = type['name']['$'];

						var config = {
							title : 'Software Installation Editor',

							layout : 'accordion',

							height : 550,

							items : [ {
								xtype : 'softwareInstallationGeneralForm',
								ref : 'SoftwareInstallationGeneralForm'
							}, {
								xtype : 'propertyForm'
							} ]
						};

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.SoftwareInstallation.Edit.superclass.initComponent
								.apply(this, arguments);
					},

					afterLoad : function(a, b, c) {
						if (this.element
								&& CMDB.Badgerfish.get(this.element,
										"Element/id/$")) {
							this.SoftwareInstallationGeneralForm.Environment
									.disable();
							this.SoftwareInstallationGeneralForm.Name.disable();
							this.SoftwareInstallationGeneralForm.State.enable();
						} else {
							this.SoftwareInstallationGeneralForm.Environment
									.enable();
							this.SoftwareInstallationGeneralForm.Name.enable();
							this.SoftwareInstallationGeneralForm.State
									.disable();
						}
					}
				});

/**
 * SoftwareInstallation (Search Form)
 */
CMDB.SoftwareInstallation.Search = Ext
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
												'html' : 'Search criteria for Software Installation changes'
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
																		fieldLabel : 'Environment',
																		expression : 'declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace change=\"http://www.klistret.com/cmdb/ci/element/process/change\"; /pojo:Element/pojo:configuration/change:Environment[commons:Name = {0}]',
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
																		fieldLabel : 'Name',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace change=\"http://www.klistret.com/cmdb/ci/element/process/change\"; /pojo:Element/pojo:configuration/change:Software[commons:Name = {0}]',
																		store : new CMDB.SoftwarePublicationContextStore,
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
																		fieldLabel : 'State',
																		expression : 'declare namespace process=\"http://www.klistret.com/cmdb/ci/element/process\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:configuration[process:State = {0}]',
																		displayField : 'Name',
																		valueField : 'Name',
																		mode : 'local',

																		store : CMDB.Change.StateStore,

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
																		xtype : 'combo',
																		plugins : [ new Ext.Element.SearchParameterPlugin() ],
																		fieldLabel : 'Specific software type',
																		expression : 'declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace change=\"http://www.klistret.com/cmdb/ci/element/process/change\"; /pojo:Element/pojo:configuration/change:Software[commons:QName eq {0}]',

																		store : new Ext.data.SimpleStore(
																				{
																					fields : [
																							'alias',
																							'name' ],
																					data : [
																							[
																									'Software',
																									'{http://www.klistret.com/cmdb/ci/element/component}Software' ],
																							[
																									'Publication',
																									'{http://www.klistret.com/cmdb/ci/element/component}Publication' ] ],
																					sortInfo : {
																						field : 'alias',
																						direction : 'ASC'
																					}
																				}),

																		queryParam : 'expressions',
																		displayField : 'alias',
																		valueField : 'name',
																		mode : 'local',
																		forceSelection : true,

																		extraItemCls : 'x-tag'
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
							title : 'Software Installation Search',
							editor : CMDB.SoftwareInstallation.Edit,

							height : 400,
							width : 800,

							autoScroll : false,

							elementType : '{http://www.klistret.com/cmdb/ci/element/process/change}SoftwareInstallation',

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
										name : 'EnvironmentName',
										mapping : 'Element/configuration/Environment/Name/$'
									},
									{
										name : 'SoftwareName',
										mapping : 'Element/configuration/Software/Name/$'
									},
									{
										name : 'SoftwareType',
										mapping : 'Element/configuration/Software/QName/$',
										formating : function(value) {
											return value.replace(/\{.*\}(.*)/,
													"$1");
										}
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
										name : 'State',
										mapping : 'Element/configuration/State/$'
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
										name : 'Description',
										mapping : 'Element/configuration/Description/$'
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
								header : 'Environment',
								width : 120,
								sortable : true,
								dataIndex : 'EnvironmentName'
							}, {
								header : 'Software',
								width : 120,
								sortable : true,
								dataIndex : 'SoftwareName'
							}, {
								header : 'Type',
								width : 120,
								sortable : true,
								dataIndex : 'SoftwareType'
							}, {
								header : 'Label',
								width : 200,
								sortable : true,
								dataIndex : 'Label'
							}, {
								header : 'State',
								width : 120,
								sortable : true,
								dataIndex : 'State'
							}, {
								header : 'Version',
								width : 120,
								sortable : true,
								dataIndex : 'Version'
							}, {
								header : "Tags",
								width : 120,
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
							} ],

							bbarButtons : [ {
								xtype : 'button',
								ref : 'State',
								text : 'State',
								iconCls : 'acceptButton',
								menu : {
									items : [ {
										text : 'Planned',
										handler : this.doState
									}, {
										text : 'Failed',
										handler : this.doState
									}, {
										text : 'Cancelled',
										handler : this.doState
									}, {
										text : 'Waiting',
										handler : this.doState
									}, {
										text : 'In Progress',
										handler : this.doState
									}, {
										text : 'Completed',
										handler : this.doState
									} ]
								}
							} ]
						}

						Ext.apply(this, Ext.apply(this.initialConfig, config));
						CMDB.SoftwareInstallation.Search.superclass.initComponent
								.apply(this, arguments);
					},

					doState : function(src) {
						var records = this.Grid.getSelectionModel()
								.getSelections();
						this.State = src.text;

						if (records.length > 0)
							this.Grid.loadMask.show();

						Ext
								.each(
										records,
										function(record) {
											var element = {
												'Element' : record
														.get('Element')
											};

											CMDB.Badgerfish
													.set(
															element,
															"Element/configuration/State/$",
															this.State);

											Ext.Ajax
													.request({
														url : (CMDB.URL || '')
																+ '/CMDB/resteasy/element',
														method : 'PUT',

														headers : {
															'Accept' : 'application/json,application/xml,text/*',
															'Content-Type' : 'application/json; charset=ISO-8859-1'
														},

														jsonData : Ext
																.encode(element),
														scope : this,

														success : function(
																result, request) {
															this.element = Ext.util.JSON
																	.decode(result.responseText);

															PageBus
																	.publish(
																			'CMDB.Element.Save',
																			{
																				state : 'success',
																				element : this.element
																			});

															var bbar = this.Grid
																	.getBottomToolbar();
															bbar.Status
																	.setText('Update successful');

															this.Grid.loadMask
																	.hide();
														},
														failure : function(
																result, request) {
															var bbar = this.Grid
																	.getBottomToolbar();
															bbar.Status
																	.setText('Failed updating.'
																			+ (result.responseText ? result.responseText
																					: ""));

															this.Grid.loadMask
																	.hide();
														}
													});
										}, this);
					}
				});