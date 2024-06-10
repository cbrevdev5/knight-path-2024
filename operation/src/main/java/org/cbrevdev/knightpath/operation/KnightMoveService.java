package org.cbrevdev.knightpath.operation;

import org.cbrevdev.knightpath.common.entity.KnightMove;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cbrevdev.knightpath.common.repository.KnightMoveRepository;
import org.cbrevdev.knightpath.operation.exception.NoSuchOperationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnightMoveService {
    private final KnightMoveRepository knightMoveRepository;

    public void solve(String operationId) throws NoSuchOperationException {
        log.info("Solve operationId = {}", operationId);
        KnightMove knightMove = knightMoveRepository.findById(operationId);
        if (knightMove == null) {
            throw new NoSuchOperationException(operationId);
        }
        KnightPath knightPath = determineShortestPath(knightMove.getSource(), knightMove.getTarget());
        if (knightPath == null) {
            knightMove.setUnreachable(true);
        } else {
            knightMove.setNumberOfMoves(knightPath.getNumberOfMoves());
            knightMove.setShortestPath(knightPath.getPath());
        }
        knightMoveRepository.save(knightMove);
    }

    protected KnightPath determineShortestPath(String source, String target) {
        Set<String> visited = new HashSet<>();
        Queue<KnightPath> stack = new LinkedList<>();
        stack.offer(new KnightPath(source));
        KnightPath shortestPath = null;
        while (!stack.isEmpty()) {
            KnightPath current = stack.poll();
            if (current.getLocation().equals(target)) {
                if (shortestPath == null || current.getNumberOfMoves() < shortestPath.getNumberOfMoves()) {
                    shortestPath = current;
                }
            } else {
                List<String> moves = getMoves(current.getLocation());
                for (String move : moves) {
                    if (!visited.contains(move)) {
                        stack.offer(new KnightPath(move, current));
                    }
                }
            }
            visited.add(current.getLocation());
        }
        return shortestPath;
    }

    protected List<String> getMoves(String location) {
        List<String> moves = new ArrayList<>();
        int x = location.charAt(0) - 'A';
        int y = location.charAt(1) - '1';

        // Move left
        if (x + 1 < 8) {
            if (y + 2 < 8) {
                moves.add(buildLocation(x + 1, y + 2));
            }
            if (y - 2 > -1) {
                moves.add(buildLocation(x + 1, y - 2));
            }
            if (x + 2 < 8) {
                if (y + 1 < 8) {
                    moves.add(buildLocation(x + 2, y + 1));
                }
                if (y - 1 > -1) {
                    moves.add(buildLocation(x + 2, y - 1));
                }
            }
        }
        // Move right
        if (x - 1 > -1) {
            if (y + 2 < 8) {
                moves.add(buildLocation(x - 1, y + 2));
            }
            if (y - 2 > -1) {
                moves.add(buildLocation(x - 1, y - 2));
            }
            if (x - 2 > -1) {
                if (y + 1 < 8) {
                    moves.add(buildLocation(x - 2, y + 1));
                }
                if (y - 1 > -1) {
                    moves.add(buildLocation(x - 2, y - 1));
                }
            }
        }
        return moves;
    }

    protected String buildLocation(int x, int y) {
        return String.valueOf((char) ('A' + x)) + (char) ('1' + y);
    }
}
