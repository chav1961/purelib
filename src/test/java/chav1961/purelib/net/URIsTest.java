package chav1961.purelib.net;

import java.net.spi.URLStreamHandlerProvider;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.net.fsys.FSysHandlerProvider;
import chav1961.purelib.net.root.RootHandlerProvider;
import chav1961.purelib.net.self.SelfHandlerProvider;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class URIsTest {
	@Test
	public void spiTest() {
		final Set<Class<URLStreamHandlerProvider>>	providers = new HashSet<>();
		
		for (URLStreamHandlerProvider item : ServiceLoader.load(URLStreamHandlerProvider.class)) {
			providers.add((Class<URLStreamHandlerProvider>)item.getClass());
		}
		Assert.assertEquals(3,providers.size());
		Assert.assertTrue(providers.contains(FSysHandlerProvider.class));		
		Assert.assertTrue(providers.contains(RootHandlerProvider.class));		
		Assert.assertTrue(providers.contains(SelfHandlerProvider.class));		
	}
}
