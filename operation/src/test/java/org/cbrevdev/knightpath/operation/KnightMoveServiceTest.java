package org.cbrevdev.knightpath.operation;

import org.cbrevdev.knightpath.common.repository.KnightMoveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class KnightMoveServiceTest {
    @Mock
    private KnightMoveRepository knightMoveRepository;

    @InjectMocks
    private KnightMoveService knightMoveService;

    @Test
    void printMoves() {
        char[] letters = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
        char[] digits = new char[] { '1', '2', '3', '4', '5', '6', '7', '8' };
        for (char c : letters) {
            for (char d : digits) {
                String location = String.valueOf(new char[] { c, d });
                List<String> moves = knightMoveService.getMoves(location);
                System.out.println(location + " => " + moves);
            }
        }
    }

    @Test
    void getMoves() {
        assertThat(knightMoveService.getMoves("A1")).containsOnlyOnceElementsOf(List.of("B3", "C2"));
        assertThat(knightMoveService.getMoves("A5")).containsOnlyOnceElementsOf(List.of("B3", "B7", "C4", "C6"));
        assertThat(knightMoveService.getMoves("A8")).containsOnlyOnceElementsOf(List.of("B6", "C7"));
        assertThat(knightMoveService.getMoves("D1")).containsOnlyOnceElementsOf(List.of("B2", "C3", "E3", "F2"));
        assertThat(knightMoveService.getMoves("D8")).containsOnlyOnceElementsOf(List.of("B7", "C6", "E6", "F7"));
        assertThat(knightMoveService.getMoves("E4")).containsOnlyOnceElementsOf(List.of("C3", "C5", "D2", "D6", "F2", "F6", "G3", "G5"));
        assertThat(knightMoveService.getMoves("H1")).containsOnlyOnceElementsOf(List.of("F2", "G3"));
        assertThat(knightMoveService.getMoves("H5")).containsOnlyOnceElementsOf(List.of("F4", "F6", "G3", "G7"));
        assertThat(knightMoveService.getMoves("H8")).containsOnlyOnceElementsOf(List.of("F7", "G6"));
    }

    @Test
    void solveSingle() {
        KnightPath knightPath = knightMoveService.determineShortestPath("A1", "B3");
        assertThat(knightPath).isNotNull();
        assertThat(knightPath.getNumberOfMoves()).isEqualTo(1);
        assertThat(knightPath.getPath()).isEqualTo("A1:B3");
        knightPath = knightMoveService.determineShortestPath("H1", "F2");
        assertThat(knightPath).isNotNull();
        assertThat(knightPath.getNumberOfMoves()).isEqualTo(1);
        assertThat(knightPath.getPath()).isEqualTo("H1:F2");
        knightPath = knightMoveService.determineShortestPath("C4", "E5");
        assertThat(knightPath).isNotNull();
        assertThat(knightPath.getNumberOfMoves()).isEqualTo(1);
        assertThat(knightPath.getPath()).isEqualTo("C4:E5");
    }

    @Test
    void solveDouble() {
        KnightPath knightPath = knightMoveService.determineShortestPath("A1", "D4");
        assertThat(knightPath).isNotNull();
        assertThat(knightPath.getNumberOfMoves()).isEqualTo(2);
        assertThat(knightPath.getPath()).isIn("A1:B3:D4", "A1:C2:D4");
        knightPath = knightMoveService.determineShortestPath("H8", "D8");
        assertThat(knightPath).isNotNull();
        assertThat(knightPath.getNumberOfMoves()).isEqualTo(2);
        assertThat(knightPath.getPath()).isEqualTo("H8:F7:D8");
        knightPath = knightMoveService.determineShortestPath("C5", "F8");
        assertThat(knightPath).isNotNull();
        assertThat(knightPath.getNumberOfMoves()).isEqualTo(2);
        assertThat(knightPath.getPath()).isIn("C5:E6:G8", "C5:D7:F8");
    }

    @Test
    void solveTriple() {
        KnightPath knightPath = knightMoveService.determineShortestPath("A1", "D5");
        assertThat(knightPath).isNotNull();
        assertThat(knightPath.getNumberOfMoves()).isEqualTo(3);
        assertThat(knightPath.getPath()).isIn("A1:C2:E3:D5", "A1:C2:B4:D5");
    }
}
