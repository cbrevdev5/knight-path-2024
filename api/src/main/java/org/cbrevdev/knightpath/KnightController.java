package org.cbrevdev.knightpath;

import org.cbrevdev.knightpath.common.entity.KnightMove;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseBody
@RequiredArgsConstructor
@Slf4j
public class KnightController {
    private final KnightService knightService;

    @PostMapping("/knightpath")
    public ResponseEntity<String> create(@RequestParam String source, @RequestParam String target) {
        log.info("Create Knight Path from {} to {}", source, target);
        String standardSource = source.toUpperCase();
        String standardTarget = target.toUpperCase();
        if (isInvalidLocation(source) || isInvalidLocation(target)) {
            return new ResponseEntity<>("Source and target must be in valid range.  Examples: A1, H8 with letter in the range A-H and digit 1-8.",
                    HttpStatus.BAD_REQUEST);
        }
        String operationId = knightService.createOperation(standardSource, standardTarget);
        String message = String.format("Operation Id %s was created. Please query it to find your results.", operationId);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping(value = "/knightpath", produces = "application/json")
    public ResponseEntity<Object> getByOperationId(@RequestParam String operationId) {
        KnightMove knightMove = knightService.findByOperationId(operationId);
        if (knightMove == null) {
            String message = "No operation found with id: " + operationId;
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        if (knightMove.getNumberOfMoves() == 0) {
            String message = "Operation " + operationId + " is not completed.";
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        KnightPathResponse knightPathResponse = KnightPathResponse.builder()
                .shortestPath(knightMove.getShortestPath())
                .numberOfMoves(knightMove.getNumberOfMoves())
                .starting(knightMove.getSource())
                .ending(knightMove.getTarget())
                .operationId(knightMove.getOperationId())
                .build();
        return new ResponseEntity<>(knightPathResponse, HttpStatus.OK);
    }

    private boolean isInvalidLocation(String location) {
        if (location.length() != 2) {
            return true;
        }
        char letter = location.charAt(0);
        char digit = location.charAt(1);
        return !(letter >= 'A' &&  letter <= 'H' && digit >= '1' && digit <= '8');
    }
}
