package org.web.codefm.infrastructure.cache.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheEvictionServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private SessionUser sessionUser;

    @Mock
    private Cache cache;

    @InjectMocks
    private CacheEvictionService cacheEvictionService;

    @Test
    void evict_shouldEvictKey_whenCacheExists() {
        String cacheName = "schools";
        Object key = 1;

        when(this.cacheManager.getCache(cacheName)).thenReturn(this.cache);

        this.cacheEvictionService.evict(cacheName, key);

        verify(this.cacheManager).getCache(cacheName);
        verify(this.cache).evict(key);
    }

    @Test
    void evict_shouldDoNothing_whenCacheDoesNotExist() {
        String cacheName = "nonExistentCache";
        Object key = 1;

        when(this.cacheManager.getCache(cacheName)).thenReturn(null);

        this.cacheEvictionService.evict(cacheName, key);

        verify(this.cacheManager).getCache(cacheName);
        verifyNoInteractions(this.cache);
    }

    @Test
    void evictByTeacher_shouldEvictUsingTeacherIdFromSession() {
        String cacheName = "schools";
        Integer teacherId = 42;

        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(teacherId);
        when(this.cacheManager.getCache(cacheName)).thenReturn(this.cache);

        this.cacheEvictionService.evictByTeacher(cacheName);

        verify(this.sessionUser).getParameter(SessionParameter.TEACHER_ID);
        verify(this.cacheManager).getCache(cacheName);
        verify(this.cache).evict(teacherId);
    }

    @Test
    void evictByTeacher_shouldDoNothing_whenCacheDoesNotExist() {
        String cacheName = "nonExistentCache";
        Integer teacherId = 42;

        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(teacherId);
        when(this.cacheManager.getCache(cacheName)).thenReturn(null);

        this.cacheEvictionService.evictByTeacher(cacheName);

        verify(this.sessionUser).getParameter(SessionParameter.TEACHER_ID);
        verify(this.cacheManager).getCache(cacheName);
        verifyNoInteractions(this.cache);
    }

    @Test
    void evictAll_shouldClearCache_whenCacheExists() {
        String cacheName = "schools";

        when(this.cacheManager.getCache(cacheName)).thenReturn(this.cache);

        this.cacheEvictionService.evictAll(cacheName);

        verify(this.cacheManager).getCache(cacheName);
        verify(this.cache).clear();
    }

    @Test
    void evictAll_shouldDoNothing_whenCacheDoesNotExist() {
        String cacheName = "nonExistentCache";

        when(this.cacheManager.getCache(cacheName)).thenReturn(null);

        this.cacheEvictionService.evictAll(cacheName);

        verify(this.cacheManager).getCache(cacheName);
        verifyNoInteractions(this.cache);
    }

}

