package org.web.codefm.infrastructure.cache.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

@Component
@RequiredArgsConstructor
public class CacheEvictionService {

    private final CacheManager cacheManager;

    private final SessionUser sessionUser;

    public void evict(String cacheName, Object key) {
        final Cache cache = this.cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    public void evictAll(String cacheName) {
        final Cache cache = this.cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    public void evictByTeacher(String cacheName) {
        this.evict(cacheName, this.sessionUser.getParameter(SessionParameter.TEACHER_ID));
    }

}

