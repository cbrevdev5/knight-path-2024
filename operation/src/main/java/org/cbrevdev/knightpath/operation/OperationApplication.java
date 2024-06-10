package org.cbrevdev.knightpath.operation;

import lombok.extern.slf4j.Slf4j;
import org.cbrevdev.knightpath.common.repository.KnightMoveRepository;
import org.cbrevdev.knightpath.operation.exception.BatchFailedException;
import org.cbrevdev.knightpath.operation.exception.NoSuchOperationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Import({ KnightMoveService.class, KnightMoveRepository.class, AppConfig.class })
@Slf4j
public class OperationApplication implements CommandLineRunner {
    private final KnightMoveService knightMoveService;

    public OperationApplication(KnightMoveService service) {
        knightMoveService = service;
    }

    public static void main(String[] args) {
        SpringApplication.run(OperationApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("OperationApplication runs: {}", String.join(", ", args));
        List<String> failedOperationIds = new ArrayList<>();
        for (String operationId : args) {
            try {
                knightMoveService.solve(operationId);
            } catch (NoSuchOperationException e) {
                failedOperationIds.add(e.getMessage());
            }
        }
        if (!failedOperationIds.isEmpty()) {
            throw new BatchFailedException(failedOperationIds);
        }
    }

}
