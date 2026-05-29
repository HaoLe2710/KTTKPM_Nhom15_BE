package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

  private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
  private static final Duration PRODUCT_SEARCH_TTL = Duration.ofMinutes(1);
  private static final Duration PRODUCT_LIST_TTL = Duration.ofMinutes(1);
  private static final Duration PRODUCT_DETAIL_TTL = Duration.ofMinutes(10);
  private static final Duration SEARCH_SUGGESTIONS_TTL = Duration.ofMinutes(3);
  private static final Duration PRODUCT_MASTER_DATA_TTL = Duration.ofMinutes(45);
  private static final Duration CACHE_ERROR_LOG_INTERVAL = Duration.ofSeconds(30);

  private final ConcurrentMap<String, Long> cacheErrorLogTimes = new ConcurrentHashMap<>();

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
    RedisCacheConfiguration defaultConfiguration = baseConfiguration(objectMapper).entryTtl(DEFAULT_TTL);

    Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
      CacheNames.PRODUCT_SEARCH, defaultConfiguration.entryTtl(PRODUCT_SEARCH_TTL),
      CacheNames.PRODUCT_LIST, defaultConfiguration.entryTtl(PRODUCT_LIST_TTL),
      CacheNames.PRODUCT_DETAIL, defaultConfiguration.entryTtl(PRODUCT_DETAIL_TTL),
      CacheNames.SEARCH_SUGGESTIONS, defaultConfiguration.entryTtl(SEARCH_SUGGESTIONS_TTL),
      CacheNames.PRODUCT_MASTER_DATA, defaultConfiguration.entryTtl(PRODUCT_MASTER_DATA_TTL)
    );

    return RedisCacheManager.builder(redisConnectionFactory)
      .cacheDefaults(defaultConfiguration)
      .withInitialCacheConfigurations(cacheConfigurations)
      .transactionAware()
      .build();
  }

  @Override
  public CacheErrorHandler errorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logCacheError("get", cache, key, exception);
      }

      @Override
      public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        logCacheError("put", cache, key, exception);
      }

      @Override
      public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logCacheError("evict", cache, key, exception);
      }

      @Override
      public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logCacheError("clear", cache, null, exception);
      }
    };
  }

  private void logCacheError(String operation, Cache cache, Object key, RuntimeException exception) {
    String cacheName = cache != null ? cache.getName() : "unknown";
    String logKey = operation + ":" + cacheName;
    long now = System.currentTimeMillis();
    long intervalMillis = CACHE_ERROR_LOG_INTERVAL.toMillis();
    Long previous = cacheErrorLogTimes.get(logKey);
    if (previous != null && now - previous < intervalMillis) {
      return;
    }
    cacheErrorLogTimes.put(logKey, now);

    if (key == null) {
      log.warn("Redis cache {} failed cache={}: {}. Similar messages are throttled for {}s.",
        operation, cacheName, exception.getMessage(), CACHE_ERROR_LOG_INTERVAL.toSeconds());
      return;
    }
    log.warn("Redis cache {} failed cache={}, key={}: {}. Similar messages are throttled for {}s.",
      operation, cacheName, key, exception.getMessage(), CACHE_ERROR_LOG_INTERVAL.toSeconds());
  }

  private RedisCacheConfiguration baseConfiguration(ObjectMapper objectMapper) {
    BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
      .allowIfSubType("fit.iuh.kttkpm_nhom15_be")
      .allowIfSubType("java.lang")
      .allowIfSubType("java.util")
      .allowIfSubType("java.math")
      .allowIfSubType("java.time")
      .build();
    ObjectMapper cacheObjectMapper = objectMapper.copy()
      .activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);

    GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

    return RedisCacheConfiguration.defaultCacheConfig()
      .disableCachingNullValues()
      .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
      .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
  }
}
