package eu.stamp_project.utils;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import eu.stamp_project.test_framework.TestFrameworkSupport;
import eu.stamp_project.utils.program.ConstantsProperties;

public class DSpotCache {
	private static Cache<String, TestFrameworkSupport> frameworkCache;
	private static CacheManager cacheManager;

	static {
		cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("frameworkCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
						TestFrameworkSupport.class, ResourcePoolsBuilder.heap(
								Long.parseLong(ConstantsProperties.CACHE_SIZE.getDefaultValue()))).build())
				.build(true);

		frameworkCache = cacheManager.getCache("frameworkCache", String.class, TestFrameworkSupport.class);
	}

	public static Cache<String, TestFrameworkSupport> getTestFrameworkCache() {
		return frameworkCache;
	}
}
