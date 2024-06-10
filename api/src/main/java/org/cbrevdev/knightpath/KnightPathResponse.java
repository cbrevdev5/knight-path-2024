package org.cbrevdev.knightpath;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class KnightPathResponse {
    private String shortestPath;
    private int numberOfMoves;
    private String starting;
    private String ending;
    private String operationId;
}
