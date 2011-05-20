/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * 
 * This code is included in the Klistret CI module since
 * the JAXB2 collection-setter is no longer maintained.
 */
package com.sun.tools.xjc.addon.collection_setter_injector;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.generator.bean.field.UntypedListField;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.api.impl.NameConverter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.xml.sax.ErrorHandler;

public class PluginImpl extends Plugin {
	private ClassOutlineImpl outline;
	private CPropertyInfo prop;
	private JType implType;
	private JType exposedType;
	private JCodeModel codeModel;
	private JFieldVar field;

	public String getOptionName() {
		return "Xcollection-setter-injector";
	}

	public List<String> getCustomizationURIs() {
		return Collections
				.singletonList("http://jaxb.dev.java.net/plugin/collection-setter-injector");
	}

	public boolean isCustomizationTagName(String nsUri, String localName) {
		return (nsUri
				.equals("http://jaxb.dev.java.net/plugin/collection-setter-injector"))
				&& (localName.equals("collection"));
	}

	public String getUsage() {
		return "  -Xcollection-setter-injector\t:  add setter method for collection-based properties";
	}

	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
		FieldOutline[] fo = null;

		for (ClassOutline co : model.getClasses()) {
			if ((null != (fo = co.getDeclaredFields())) && (0 < fo.length)) {
				for (int i = 0; i < fo.length; i++) {
					if ((fo[i] instanceof UntypedListField)) {
						declareMethod((ClassOutlineImpl) co,
								(UntypedListField) fo[i]);
					}
				}
			}
		}

		return true;
	}

	private void declareMethod(ClassOutlineImpl context, UntypedListField field) {
		MethodWriter writer = context.createMethodWriter();
		Map<String, JFieldVar> fields = null;
		NameConverter nc = context.parent().getModel().getNameConverter();

		this.outline = context;
		this.codeModel = context.parent().getCodeModel();
		this.prop = field.getPropertyInfo();

		fields = this.outline.implClass.fields();

		this.field = ((JFieldVar) fields.get(field.getPropertyInfo().getName(
				false)));

		JMethod $set = writer.declareMethod(this.codeModel.VOID, "set"
				+ this.prop.getName(true));

		JVar $value = writer.addParameter(this.field.type(), this.prop
				.getName(false));

		JBlock body = $set.body();

		body.assign(JExpr._this().ref(ref()), castToImplType($value));

		String javadoc = this.prop.javadoc;
		List<Object> possibleTypes = listPossibleTypes(this.prop);
		javadoc = this.prop.javadoc;
		if (javadoc.length() == 0)
			javadoc = Messages.DEFAULT_SETTER_JAVADOC.format(new Object[] { nc
					.toVariableName(this.prop.getName(true)) });
		writer.javadoc().append(javadoc);
		writer.javadoc().addParam($value).append("allowed object is\n").append(
				possibleTypes);
	}

	protected final List<Object> listPossibleTypes(CPropertyInfo prop) {
		List<Object> r = new ArrayList<Object>();
		for (CTypeInfo tt : prop.ref()) {
			JType t = ((NType) tt.getType()).toType(this.outline.parent(),
					Aspect.EXPOSED);
			if ((t.isPrimitive()) || (t.isArray())) {
				r.add(t.fullName());
			} else {
				r.add(t);
				r.add("\n");
			}
		}

		return r;
	}

	protected JFieldVar ref() {
		return this.field;
	}

	protected final JExpression castToImplType(JExpression exp) {
		if (this.implType == this.exposedType) {
			return exp;
		}
		return JExpr.cast(this.implType, exp);
	}
}