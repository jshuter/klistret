package test.com.klistret.cmdb.utility.jaxb;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class Experimenting {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void reflections() {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.getUrlsForCurrentClasspath())
				.setScanners(new TypeAnnotationsScanner()));

		Set<Class<?>> entries = reflections
				.getTypesAnnotatedWith(com.klistret.cmdb.annotations.ci.Proxy.class);

		System.out.println(String.format("Found %d entries", entries.size()));
	}

}
