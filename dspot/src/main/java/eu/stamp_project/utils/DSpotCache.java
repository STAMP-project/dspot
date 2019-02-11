package eu.stamp_project.utils;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import eu.stamp_project.test_framework.TestFrameworkSupport;

public class DSpotCache {
	private static Cache<String, TestFrameworkSupport> frameworkCache;
	private static CacheManager cacheManager;

	static {
		cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("frameworkCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
						TestFrameworkSupport.class, ResourcePoolsBuilder.heap(10000)).build())
				.build(true);

		frameworkCache = cacheManager.getCache("frameworkCache", String.class, TestFrameworkSupport.class);

		// frameworkCache = cacheManager.createCache("methodCache",
		// CacheConfigurationBuilder
		// .newCacheConfigurationBuilder(String.class, TestFramework.class,
		// ResourcePoolsBuilder.heap(100))
		// .build());
	}

	public static Cache<String, TestFrameworkSupport> getTestFrameworkCache() {
		return frameworkCache;
	}
}
