package test.com.klistret.cmdb.utility.jaxb;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

import com.klistret.cmdb.utility.jaxb.CIContext;

public class Experimenting {

	@Before
	public void setUp() throws Exception {

	}

	//@Test
	public void scanning() {
		URL[] urls = ClasspathUrlFinder.findClassPaths();
		AnnotationDB db = new AnnotationDB();

		try {
			db.scanArchives(urls);
			Set<String> entries = db.getAnnotationIndex().get(
					com.klistret.cmdb.annotations.ci.Proxy.class.getName());

			System.out.println(String
					.format("Found %d entries", entries.size()));
			for (String name : entries)
				System.out.println(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void anotherContext() {
		CIContext ciContext = CIContext.getCIContext();

		ciContext.getJAXBContext();
	}
}
