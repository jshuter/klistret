Ext.namespace('CMDB');


CMDB.URL = 'http://vsgpklistret.sfa.se:50003';


// Message
CMDB.Message = function(){
    var msgCt;

    function createBox(t, s){
        return ['<div class="msg">',
                '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
                '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><h3>', t, '</h3>', s, '</div></div></div>',
                '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
                '</div>'].join('');
    }
    return {
        msg : function(title, format){
            if(!msgCt){
                msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
            }
            msgCt.alignTo(document, 't-t');
            var s = String.format.apply(String, Array.prototype.slice.call(arguments, 1));
            var m = Ext.DomHelper.append(msgCt, {html:createBox(title, s)}, true);
            m.slideIn('t').pause(3).ghost("t", {remove:true});
        },

        init : function(){
        }
	};
}();



// Badgerfish reader
CMDB.Badgerfish = function() {
	var 
		getFullPropertyName = function (obj, suffix) {
			var name;
		
			for (var propName in obj) {
				if (propName.replace(/^\w+:/,'') === suffix) name = propName;
			}
			return name;
		},
		
		getNamespaces = function(obj) {
			var result = obj,
			    namespaces = {};
			
			for (var prop in result) {
				if (prop === "@xmlns") {
					Ext.applyIf(namespaces, result[prop]);
				}
				if (Ext.isObject(result[prop]) && prop !== "@xmlns") {
					Ext.applyIf(namespaces, getNamespaces(result[prop]));
				}
			}
			
			return namespaces;
		}
	;

	return {
		remove     : function(obj, expr) {
			var parts = (expr || '').split('/'),
              	result = obj,
				part;
				
			part = parts.shift();   
			while (parts.length > 0) {
				var propName = getFullPropertyName(result, part);	
				result = propName ? result[propName] : null;
				
				part = parts.shift(); 
			}
			
			var propName = getFullPropertyName(result, part);
			if (propName) delete result[propName]; 
		},
		
		get        : function(obj, expr) {
			var parts = (expr || '').split('/'),
              	result = obj,
				part;
					
			while (parts.length > 0 && result) {
				part = parts.shift();
				
				var propName = getFullPropertyName(result, part);	
				result = propName ? result[propName] : null;
          	}
          		
			return result;
		},
		
		set        : function(obj, expr, value) {
			var parts = (expr || '').split('/'),
				result = obj,
				part;
                                
			part = parts.shift();   
			while (parts.length > 0) {
				var propName = getFullPropertyName(result, part);	
				result = propName ? result[propName] : null;
				
				part = parts.shift(); 
			}
            
            var propName = getFullPropertyName(result, part);            
			if (propName) result[propName] = value;
		},
		
		getNS      : function(obj) {
			return getNamespaces(obj);
		},
		
		getPrefix  : function(obj, ns) {
			var namespaces = getNamespaces(obj),
				prefix;
			
			for (key in namespaces) {
				if (namespaces[key] === ns) prefix = key;
			}
			return prefix;
		}
	};
}();




/**
 http://erichauser.net/2007/11/07/more-wcf-json-and-extjs/
*/
CMDB.JsonReader = Ext.extend(Ext.data.JsonReader, {

	rewriteProperties : function(obj) {
		if (typeof obj !== "object") return obj;
		for (var prop in obj) {
			if (obj.hasOwnProperty(prop)) {
				obj[prop.replace(/\./g, ":")] = this.rewriteProperties(obj[prop]);
				if (prop.indexOf(".") > -1) {
					delete obj[prop];
				}
			}
		}
		return obj;
	},

	read : function(response){
		var json = response.responseText;
		var o = eval("("+json+")");

  		if(!o) {
			throw {message: "JsonReader.read: Json object not found"};
		}
		
		var data = response.status == '200' ? {total: this.meta.passedCount ? this.meta.passedCount : o.length, successful: true, rows: o} : {total: 0, successful: false, rows: []};

		return CMDB.JsonReader.superclass.readRecords.call(this, data);
	},
	
	createAccessor : function(){
        return function(expr) {
            if(Ext.isEmpty(expr)){
                return Ext.emptyFn;
            }
            
            if(Ext.isFunction(expr)){
                return expr;
            }
            
            return function(obj){
            	return CMDB.Badgerfish.get(obj, expr);
            };
        };
    }(),
    
    createRecord : function(rawdata, id) {
    	var recordDef = Ext.data.Record.create(this.meta.fields),
			record = {};
							
		Ext.each(
			this.meta.fields, 
			function(field) {
				var accessor = this.createAccessor(field.mapping);
				var value = accessor(rawdata);
							
				record[field.name] = value;
			},
			this
		);
						
		return new recordDef(record, id);
    }
});


CMDB.ElementTypes =  new Ext.data.Store({
	baseParams     : {},
                                        
	proxy          : new Ext.data.HttpProxy({
		url            : (CMDB.URL || '') + '/CMDB/resteasy/elementType',
		method         : 'GET',
                                        
		headers        : {
			'Accept'          : 'application/json,application/xml,text/*',
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
			},
			{
				name             : 'Namespace',
				mapping          : 'ElementType/name/$'
			},
			{
				name             : 'ElementType',
				mapping          : 'ElementType'
			}
		]
	}),
                                        
	listeners       : {
		'load'           : function(store, records, options) {
			Ext.each(records, function(record) {
				var name = record.get('Name'),
					namespace = record.get('Namespace');
                                                                
				record.set('Name', name.replace(/\{.*\}(.*)/,"$1"));
				record.set('Namespace', namespace.replace(/\{(.*)\}.*/,"$1"));
				record.commit();
			});
		}
	}
});

CMDB.ElementTypes.load({
	params : {
		'name'      : '%'
	}
});


CMDB.RelationTypes =  new Ext.data.Store({
	baseParams     : {},
                                        
	proxy          : new Ext.data.HttpProxy({
		url            : (CMDB.URL || '') + '/CMDB/resteasy/relationType',
		method         : 'GET',
                                        
		headers        : {
			'Accept'          : 'application/json,application/xml,text/*',
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
			},
			{
				name             : 'RelationType',
				mapping          : 'RelationType'
			}
		]
	})
});

CMDB.RelationTypes.load({
	params : {
		'name'      : '%'
	}
});
