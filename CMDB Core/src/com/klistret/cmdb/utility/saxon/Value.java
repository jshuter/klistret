/**
 ** This file is part of Klistret. Klistret is free software: you can
 ** redistribute it and/or modify it under the terms of the GNU General
 ** Public License as published by the Free Software Foundation, either
 ** version 3 of the License, or (at your option) any later version.

 ** Klistret is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 ** General Public License for more details. You should have received a
 ** copy of the GNU General Public License along with Klistret. If not,
 ** see <http://www.gnu.org/licenses/>
 */
package com.klistret.cmdb.utility.saxon;

import java.util.Random;

public class Value {

	/**
	 * Default literal mask
	 */
	private static Random generator = new Random(19580427);

	/**
	 * Java value
	 */
	private Object javaValue;

	/**
	 * Text only
	 */
	private String text;

	/**
	 * Raw value (i.e. with quotes so forth)
	 */
	private String literal;

	/**
	 * Mask (variable)
	 */
	private String mask;
	

	public Object getJavaValue() {
		return javaValue;
	}

	public void setJavaValue(Object javaValue) {
		this.javaValue = javaValue;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}

	public String getMask() {
		if (mask == null)
			mask = "v" + generator.nextInt();

		return mask;
	}
}
