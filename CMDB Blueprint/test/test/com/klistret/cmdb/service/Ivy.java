package test.com.klistret.cmdb.service;

import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

public class Ivy {

	private com.klistret.cmdb.service.ElementService elementService;

	/**
	 * Date formatter (friendly format for date-time as XML)
	 */
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ");

	private class UpdateTask implements Callable {
		private com.klistret.cmdb.service.ElementService elementService;

		private com.klistret.cmdb.ci.pojo.Element element;

		public void setElementService(
				com.klistret.cmdb.service.ElementService elementService) {
			this.elementService = elementService;
		}

		public void setElement(com.klistret.cmdb.ci.pojo.Element element) {
			this.element = element;
		}

		public com.klistret.cmdb.ci.pojo.Element call() {
			return elementService.update(element);
		}
	}

	@Before
	public void setUp() throws Exception {
		GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
		ctx.getEnvironment().setActiveProfiles("development", "ivy");
		ctx.load("classpath:Spring.cfg.xml");
		ctx.refresh();

		elementService = ctx
				.getBean(com.klistret.cmdb.service.ElementService.class);
	}

	public void update() throws InterruptedException, ExecutionException {
		com.klistret.cmdb.ci.pojo.Element element = elementService
				.get(new Long(571734));

		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		CompletionService<com.klistret.cmdb.ci.pojo.Element> pool = new ExecutorCompletionService<com.klistret.cmdb.ci.pojo.Element>(
				threadPool);

		for (int i = 0; i < 2; i++) {
			((com.klistret.cmdb.ci.element.component.Software) element
					.getConfiguration()).setPhase("S" + i);

			UpdateTask task = new UpdateTask();
			task.setElementService(elementService);
			task.setElement(element);

			pool.submit(task);
		}

		for (int i = 0; i < 2; i++) {
			com.klistret.cmdb.ci.pojo.Element future = (com.klistret.cmdb.ci.pojo.Element) pool
					.take().get();

			System.out.println(String.format("Element [id: %s, version: %s]",
					future.getId(), sdf.format(future.getUpdateTimeStamp())));
		}

		threadPool.shutdown();
	}

	@Test
	public void update2() {
		com.klistret.cmdb.ci.pojo.Element element = elementService
				.get(new Long(577405));
		
		((com.klistret.cmdb.ci.element.component.Software) element
				.getConfiguration()).setPhase("maintanace");

		elementService.update(element);
	}
}
