package com.rodhilton.metaheuristics.rectanglevisibility

class Rectangle implements Serializable {
    double east, north, west, south

    def boolean contains(double x, double y) {
        west <= x && x <= east && south <= y && y <= north
    }

    def Rectangle generateOverlappingRectangleWithPoint(double x, double y, Random random) {
        double minX = 0.0
        double maxX = 1.0
        double minY = 0.0
        double maxY = 1.0
        if (x > east) maxX = east
        if (x < west) minX = west
        if (y > north) maxY = north
        if (y < south) minY = south

        double x2 = (random.nextDouble() * (maxX - minX)) + minX
        double y2 = (random.nextDouble() * (maxY - minY)) + minY

        return new Rectangle(
                north: Math.max(y, y2),
                south: Math.min(y, y2),
                east: Math.max(x, x2),
                west: Math.min(x, x2),
        )
    }

    def String toString() {
        "(${round(east)}, ${round(north)}, ${round(west)}, ${round(south)})"
    }

    private def String round(double d) {
        return sprintf("%4.2f", [d])
    }
}
