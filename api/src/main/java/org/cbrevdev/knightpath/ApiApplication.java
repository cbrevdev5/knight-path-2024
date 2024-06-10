package org.cbrevdev.knightpath;

import org.cbrevdev.knightpath.common.repository.KnightMoveRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ KnightController.class, KnightService.class, KnightMoveRepository.class, AppConfig.class })
public class ApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
