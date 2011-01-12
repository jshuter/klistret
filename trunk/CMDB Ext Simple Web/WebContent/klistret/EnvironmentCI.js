/**
 * Name-spaces
*/
Ext.namespace('CMDB.Environment');


/**
 *
 */
CMDB.Environment.Edit = Ext.extend(CMDB.Element.Edit, {
	element        : {
		'Element' : {
			'@xmlns' : 
				{
					'ns9'  : 'http://www.klistret.com/cmdb/ci/element',
					'ns10' : 'http://www.klistret.com/cmdb/ci/element/system',
					'ns2'  : 'http://www.klistret.com/cmdb/ci/commons',
					'$'    : 'http://www.klistret.com/cmdb/ci/pojo'
				},
			'name' : {
				'$' : ''
			},
			'type' : {
				'id' : {
					'$' : '18'
				},
				'name' : {
					'$' : '{http://www.klistret.com/cmdb/ci/element/system}Environment'
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
				'@xsi:type' : 'ns10:Environment'
			}
		}
	},

	/**
	 *
	 */
	initComponent  : function() {
		var config = {
			title       : 'Environment Editor',
			
			layout      : 'accordion',
			
			items       : [
				{
					xtype       : 'generalForm'
				},
				{
					xtype       : 'systemGeneralForm'
				},
				{
					xtype       : 'destRelationForm',
					ref         : 'DestRelForm'
				},
				{
					xtype       : 'propertyForm'
				}
			]
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Environment.Edit.superclass.initComponent.apply(this, arguments);
	},
	
	afterInsertion : function() {
		this.DestRelForm.Grid.enable();
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