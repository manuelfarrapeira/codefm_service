package org.web.codefm.infrastructure.cache.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheEvictionServiceTest {

    private CacheEvictionService cacheEvictionService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private SessionUser sessionUser;

    @Mock
    private Cache cache;

    @BeforeEach
    void beforeEach() {
        this.cacheEvictionService = new CacheEvictionService(this.cacheManager, this.sessionUser);
    }

    @Nested
    class Evict {

        @Test
        void when_cache_exists_expect_key_evicted() {
            final String cacheName = "schools";
            final Object key = 1;

            when(CacheEvictionServiceTest.this.cacheManager.getCache(cacheName)).thenReturn(CacheEvictionServiceTest.this.cache);

            CacheEvictionServiceTest.this.cacheEvictionService.evict(cacheName, key);

            verify(CacheEvictionServiceTest.this.cacheManager).getCache(cacheName);
            verify(CacheEvictionServiceTest.this.cache).evict(key);
        }

        @Test
        void when_cache_does_not_exist_expect_no_cache_interaction() {
            final String cacheName = "nonExistentCache";
            final Object key = 1;

            when(CacheEvictionServiceTest.this.cacheManager.getCache(cacheName)).thenReturn(null);

            CacheEvictionServiceTest.this.cacheEvictionService.evict(cacheName, key);

            verify(CacheEvictionServiceTest.this.cacheManager).getCache(cacheName);
            verifyNoInteractions(CacheEvictionServiceTest.this.cache);
        }
    }

    @Nested
    class EvictByTeacher {

        @Test
        void when_cache_exists_expect_teacher_key_evicted() {
            final String cacheName = "schools";
            final Integer teacherId = 42;

            when(CacheEvictionServiceTest.this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(teacherId);
            when(CacheEvictionServiceTest.this.cacheManager.getCache(cacheName)).thenReturn(CacheEvictionServiceTest.this.cache);

            CacheEvictionServiceTest.this.cacheEvictionService.evictByTeacher(cacheName);

            verify(CacheEvictionServiceTest.this.sessionUser).getParameter(SessionParameter.TEACHER_ID);
            verify(CacheEvictionServiceTest.this.cacheManager).getCache(cacheName);
            verify(CacheEvictionServiceTest.this.cache).evict(teacherId);
        }

        @Test
        void when_cache_does_not_exist_expect_no_cache_interaction() {
            final String cacheName = "nonExistentCache";
            final Integer teacherId = 42;

            when(CacheEvictionServiceTest.this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(teacherId);
            when(CacheEvictionServiceTest.this.cacheManager.getCache(cacheName)).thenReturn(null);

            CacheEvictionServiceTest.this.cacheEvictionService.evictByTeacher(cacheName);

            verify(CacheEvictionServiceTest.this.sessionUser).getParameter(SessionParameter.TEACHER_ID);
            verify(CacheEvictionServiceTest.this.cacheManager).getCache(cacheName);
            verifyNoInteractions(CacheEvictionServiceTest.this.cache);
        }
    }

    @Nested
    class EvictAll {

        @Test
        void when_cache_exists_expect_cache_cleared() {
            final String cacheName = "schools";

            when(CacheEvictionServiceTest.this.cacheManager.getCache(cacheName)).thenReturn(CacheEvictionServiceTest.this.cache);

            CacheEvictionServiceTest.this.cacheEvictionService.evictAll(cacheName);

            verify(CacheEvictionServiceTest.this.cacheManager).getCache(cacheName);
            verify(CacheEvictionServiceTest.this.cache).clear();
        }

        @Test
        void when_cache_does_not_exist_expect_no_cache_interaction() {
            final String cacheName = "nonExistentCache";

            when(CacheEvictionServiceTest.this.cacheManager.getCache(cacheName)).thenReturn(null);

            CacheEvictionServiceTest.this.cacheEvictionService.evictAll(cacheName);

            verify(CacheEvictionServiceTest.this.cacheManager).getCache(cacheName);
            verifyNoInteractions(CacheEvictionServiceTest.this.cache);
        }
    }
}
