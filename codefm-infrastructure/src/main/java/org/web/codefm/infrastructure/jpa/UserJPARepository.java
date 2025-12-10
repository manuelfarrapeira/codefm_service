package org.web.codefm.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.users.UserEntity;

import java.util.Optional;

@Repository
public interface UserJPARepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByLogin(String login);

}

