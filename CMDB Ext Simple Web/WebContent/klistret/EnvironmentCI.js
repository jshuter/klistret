/**
 * Name-spaces
*/
Ext.namespace('CMDB.Environment');



CMDB.Environment.Edit = Ext.extend(CMDB.Element.Edit, {

	initComponent  : function() {
		var config = {
			title       : 'Environment Editor'
		};
	
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		CMDB.Environment.Edit.superclass.initComponent.apply(this, arguments);
	}
});



CMDB.Environment.Search = Ext.extend(CMDB.Element.Search, {

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
					xtype             : 'hidden',
					plugins           : [new Ext.Element.HiddenSearchPlugin()],	
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]'
				},
				{
					xtype             : 'hidden',
					plugins           : [new Ext.Element.HiddenSearchPlugin()],
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[matches(pojo:name,\"Environment\")]'
				},
				{
					xtype             : 'displayfield',
					width             : 'auto',
					'html'            : 'Search criteria for this CI (Configuration Item)'
				},
				{
					xtype             : 'textfield',
					plugins           : [new Ext.Element.TextFieldSearchPlugin()],
					required          : true,
					fieldLabel        : 'Name',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"{0}\")]'
				},
				{
					xtype             : 'datefield',
					fieldLabel        : 'Created after',
					format            : 'Y-m-d',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"{0}\" cast as xs:dateTime]'
				},
				{
					xtype             : 'datefield',
					fieldLabel        : 'Created before',
					format            : 'Y-m-d',
					expression        : 'declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp lt \"{0}\" cast as xs:dateTime]'
				}
			]
		});
	
		var config = {
			title       : 'Environment Search',

			items       : form,
		
			fields      : [
				{
					name        : 'Id', 
		 			mapping     : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.id'
		 		},
				{
					name        : 'Name', 
					mapping     : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.name'
				},
				{
					name        : 'Watermark', 
					mapping     : 'com.klistret.cmdb.ci.pojo.Element/com.klistret.cmdb.ci.pojo.configuration/@Watermark'
				},
				{
					name        : 'Element',
					mapping     : 'com.klistret.cmdb.ci.pojo.Element'
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
	}
});