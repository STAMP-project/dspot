package eu.stamp_project.utils;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import eu.stamp_project.test_framework.TestFrameworkSupport;

public class DSpotCache {

	private DSpotCache(long cacheSize) {
		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("frameworkCache",
						CacheConfigurationBuilder.newCacheConfigurationBuilder(
								String.class,
								TestFrameworkSupport.class,
								ResourcePoolsBuilder.heap(cacheSize).build()
						)
				)
				.build(true);
		frameworkCache = cacheManager.getCache("frameworkCache", String.class, TestFrameworkSupport.class);
	}

	public static void init(long cacheSize) {
		_instance = new DSpotCache(cacheSize);
	}
	private Cache<String, TestFrameworkSupport> frameworkCache;

	private static DSpotCache _instance;

	public static Cache<String, TestFrameworkSupport> getTestFrameworkCache() {
		if (_instance == null) {
			System.err.println("Must use at least one time init(long) method");
			return null;
		}
		return _instance.frameworkCache;
	}

	public static void reset() {
		if (_instance == null) {
			System.err.println("Must use at least one time init(long) method");
		} else {
			_instance.frameworkCache.clear();
		}
	}
}
