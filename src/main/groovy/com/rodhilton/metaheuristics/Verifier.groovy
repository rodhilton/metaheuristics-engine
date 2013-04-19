package com.rodhilton.metaheuristics

class Verifier {

    public static void main(String[] args) {
        File file = new File(args[0])
        String fileContents = file.readLines().join("\n")
        def matcher = fileContents =~ /(?mis)Rectangle Visibility Diagram on (\d+) rectangles with (\d+) edges.*\{(.*)\}/
        def size = matcher[0][1].toInteger()
        def edgeCount = matcher[0][2].toInteger()
        def rectLines = matcher[0][3].trim().split("\n")

        List<Rectangle> rects = new ArrayList<Rectangle>(size)
        for(String rectLine: rectLines) {
            def lineMatcher = rectLine =~ /\s*(\d+): \(([\d.]+),\s*([\d.]+),\s*([\d.]+),\s*([\d.]+)\s*\)/
            if(lineMatcher.matches()) {
                int index = lineMatcher[0][1].toInteger()
                double east = lineMatcher[0][2].toDouble()
                double north = lineMatcher[0][3].toDouble()
                double west = lineMatcher[0][4].toDouble()
                double south = lineMatcher[0][5].toDouble()
                rects[index-1] = new Rectangle(north: north, east: east, south: south, west: west)
            }
        }

        GraphThing thing = new GraphThing(size, new Random(), rects)

        println("Visibility Diagram's Calculated Fitness: ${thing.fitness()}")
        println("Visibility Diagram's Believed Fitness: ${edgeCount}")

    }
}
