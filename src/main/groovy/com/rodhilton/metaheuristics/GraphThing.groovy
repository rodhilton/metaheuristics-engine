package com.rodhilton.metaheuristics

import com.rodhilton.metaheuristics.algorithms.MetaheuristicAlgorithm
import com.rodhilton.metaheuristics.collections.ScoredSet
import com.rodhilton.metaheuristics.rectangles.RectUtils

class GraphThing implements Serializable, MetaheuristicAlgorithm<GraphThing>{
    private int size
    private transient Random random
    List<Rectangle> rects;

    public GraphThing(int size, Random random) {

        this.random = random
        this.size = size
        rects = new ArrayList<Rectangle>(size)

        Rectangle previousRect = new Rectangle(north: 1.0, south: 0.0, west: 0.0, east: 1.0)

        for(int i=0;i<size;i++) {
            double ns1 = random.nextDouble()
            double ns2 = random.nextDouble()
            double ew1 = random.nextDouble()
            double ew2 = random.nextDouble()
            //It seems like we should always create rectangles which overlap the previous one, otherwise its not even close to valid

            //To do this, you generate one point of the new rectange.
            // if its INSIDE the previous rectangle, you can pick any point at all
            // if its outside, you need to know its orientation to the previous rectangle.
            // So if the point is to the north east of the rectange, then to pick a second point you can only go, for X, between
            //0 and rect.east, and for y you go between 0 and rect.north

            double y = random.nextDouble()
            double x = random.nextDouble()

            Rectangle rect = previousRect.generateOverlappingRectangleWithPoint(x, y, random)

//            Rectangle rect = new Rectangle(
//                    north: Math.max(ns1, ns2),
//                    south: Math.min(ns1, ns2),
//                    east: Math.max(ew1, ew2),
//                    west: Math.min(ew1, ew2)
//            )
            rects.add(rect)

            previousRect = rect
        }
    }

    protected GraphThing(int size, Random random, List<Rectangle> rects) {
        this.size = size
        this.random = random
        this.rects = rects
    }


    @Override
    Number fitness() {
        //count how many rectanges can see each other.
        //There are size * size-1 total pairs, so the max score is (size*size-1)/2 since (a,b) is the same as (b,a)
        int count = 0;
        def visiblePairs = []
        for(int a=0;a<size;a++) {
            for(int b=a+1;b<size;b++) {
                Rectangle bottom = rects[a]
                Rectangle top = rects[b]
                List<Rectangle> inbetween = new ArrayList<Rectangle>()
                if(b > a+1) {
                    inbetween=rects[a+1..b-1]
                }

                if(isOverlapping(bottom, top) && isFreeCornerBetween(bottom,top,inbetween)) {
                    visiblePairs << [a, b]
                    count++;
                }
            }
        }
        //println("fitness of ${count}!!")
        //println(visiblePairs.collect{ pair -> "(${pair[0]+1}, ${pair[1]+1})"}.join(", "));
        return count
    }

    private boolean isFreeCornerBetween(Rectangle a, Rectangle b, List<Rectangle> inbetween) {
        int count=0;
        for(Rectangle r: inbetween) {
            boolean fcne = r.east < Math.min(a.east, b.east) || r.north < Math.min(a.north, b.north)
            boolean fcnw = r.north < Math.min(a.north, b.north) || r.north < Math.min(a.north, b.north)
            boolean fcsw = r.west < Math.min(a.west, b.west) || r.north < Math.min(a.north, b.north)
            boolean fcse = r.south < Math.min(a.south, b.south) || r.north < Math.min(a.north, b.north)
            if(fcne || fcnw || fcsw || fcse) count++;
        }
        count == inbetween.size()
    }

    private boolean isOverlapping(Rectangle bottom, Rectangle top) {
        bottom.west < top.east && bottom.east > top.west &&
                bottom.south < top.north && bottom.north > top.south
    }

    @Override
    List<GraphThing> combine(ScoredSet<GraphThing> scoredGeneration) {
        def parents = scoredGeneration.getTop(2);

        int pivot=random.nextInt(size-1)

        def parent1Bottom = parents[0].rects[0..pivot]
        def parent2Bottom = parents[1].rects[0..pivot]
        def parent1Top=parents[0].rects[pivot+1..-1]
        def parent2Top=parents[1].rects[pivot+1..-1]

        GraphThing child1 = new GraphThing(size, random, parent1Bottom + parent2Top)
        GraphThing child2 = new GraphThing(size, random, parent2Bottom + parent1Top)

        int mutations=scoredGeneration.size()-1

        List<GraphThing> child1Offspring = []
        List<GraphThing> child2Offspring = []

        for(int i=0;i<mutations;i++) {
            child1Offspring << child1.mutate()
            child2Offspring << child2.mutate()
        }


        def things = child1Offspring + child2Offspring + scoredGeneration.getBest()
        return things
    }

    private GraphThing mutate() {
        int whichRect = random.nextInt(size);
        int whichDimension = random.nextInt(4);
        List<Rectangle> copyRects = new ArrayList<Rectangle>()
        for(Rectangle rectToCopy: rects) {
            Rectangle newRect = new Rectangle(
                    north: rectToCopy.north,
                    east: rectToCopy.east,
                    south: rectToCopy.south,
                    west: rectToCopy.west
            )
            copyRects.add(newRect);
        }
        Rectangle mutating = copyRects[whichRect]

        switch(whichDimension) {
            case 0: //north
                mutating.setNorth(mutating.south+(random.nextDouble()*(1.0-mutating.south)))
                break;
            case 1: //east
                mutating.setEast(mutating.west+(random.nextDouble()*(1.0-mutating.west)))
                break;
            case 2: //south
                mutating.setSouth(random.nextDouble()*mutating.north)
                break;
            case 3: //west
                mutating.setWest(random.nextDouble()*mutating.east)
                break;
        }

        copyRects.set(whichRect, mutating)

        return new GraphThing(size, random, copyRects)
    }

    @Override
    String toString() {
        "F${fitness()}"
        //RectUtils.printRectangles((Rectangle[])rects.toArray())
//        StringBuilder sb = new StringBuilder()
//        sb.append("{\n")
//        for(Rectangle rect: rects) {
//            sb.append("   (${rect.east},${rect.north},${rect.west},${rect.south})\n")
//        }
//        sb.append("}")
//        sb.toString()
    }
}


class Rectangle {
    double east, north, west, south

    def boolean contains(double x, double y) {
        west <= x && x <= east && south <= y && y <= north
    }

    def Rectangle generateOverlappingRectangleWithPoint(double x, double y, Random random) {
        if(this.contains(x, y)) {
            double x2 = random.nextDouble()
            double y2 = random.nextDouble()

            return new Rectangle(
                    north: Math.max(y, y2),
                    south: Math.min(y, y2),
                    east: Math.max(x, x2),
                    west: Math.min(x, x2)
            )
        } else {
            double minX=0.0
            double maxX=1.0
            double minY=0.0
            double maxY=1.0
            if(x > east) maxX = east
            if(x < west) minX = west
            if(y > north) maxY = north
            if(y < south) minY = south

            double x2 = (random.nextDouble()*(maxX-minX))+minX
            double y2 = (random.nextDouble()*(maxY-minY))+minY

            return new Rectangle(
                    north: Math.max(y, y2),
                    south: Math.min(y, y2),
                    east: Math.max(x, x2),
                    west: Math.min(x, x2)
            )
        }

    }

    def String toString() {
        "(${round(west)}, ${round(north)})->(${round(east)}, ${round(south)})"
    }

    private def String round(double d) {
        return sprintf("%4.2f", [d])
    }
}