package org.web.codefm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.User;
import org.web.codefm.domain.service.HelloWorldService;

import java.util.Objects;

@Slf4j
@Service
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public String helloWorld(User user) {

        log.info("User: " + user);

        return Objects.nonNull(user) ? "Hi! " + user.getName() : "User not found";
    }
}
