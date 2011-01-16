Ext.namespace('CMDB');


CMDB.URL = 'http://sadbmatrix2:55167';


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
		
		var data = response.status == '200' ? {total: o.length, successful: true, rows: o} : {total: 0, successful: false, rows: []};

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
