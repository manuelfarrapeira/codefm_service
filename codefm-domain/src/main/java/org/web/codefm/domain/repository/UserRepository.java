package org.web.codefm.domain.repository;

import org.web.codefm.domain.entity.User;

public interface UserRepository {

    User findByName(String usuario);

}
