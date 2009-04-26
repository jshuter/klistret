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

package com.klistret.cmdb.utility.hibernate;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class CMDBDatabaseHelper {

	private static Configuration sConfiguration;

	protected static CMDBDatabaseHelper sInstance;

	private String delimiter = ";";

	protected CMDBDatabaseHelper(String mConfiguration) {
		sConfiguration = new Configuration();
		sConfiguration.configure(mConfiguration);
	}

	public synchronized static CMDBDatabaseHelper getInstance(
			String mConfiguration) {
		if (sInstance == null) {
			sInstance = new CMDBDatabaseHelper(mConfiguration);
		}
		return sInstance;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void generateDatabaseSchema(String path, boolean display,
			boolean execute) {
		SchemaExport sSchemaExport = new SchemaExport(
				CMDBDatabaseHelper.sConfiguration);
		sSchemaExport.setOutputFile(path);
		sSchemaExport.setDelimiter(delimiter);
		sSchemaExport.create(display, execute);
	}
}
