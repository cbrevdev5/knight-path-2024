package org.cbrevdev.knightpath.operation;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class KnightPath {
    private String location;
    private String path;
    private int numberOfMoves = 0;

    public KnightPath(String location) {
        this.location = location;
        this.path = location;
    }

    public KnightPath(String location, KnightPath parent) {
        this.location = location;
        this.path = parent.path + ":" + location;
        this.numberOfMoves = parent.numberOfMoves + 1;
    }

}
