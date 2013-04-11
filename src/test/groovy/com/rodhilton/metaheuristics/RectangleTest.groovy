package com.rodhilton.metaheuristics

import com.rodhilton.metaheuristics.rectangles.RectUtils
import org.junit.Before
import org.junit.Test

class RectangleTest {
    Random random

    @Before
    public void setUp() {
        random = new Random()
        random.setSeed(123457)
    }

    @Test
    void testGenerateOverlappingRectangleWithPoint() {
        Rectangle current = randomRectangle()
        runManyTimes(20, {
            Rectangle newRect = current.generateOverlappingRectangleWithPoint(random.nextDouble(), random.nextDouble(), random)
            assert isOverlapping(current, newRect)
            println("WORKED!")
        })
    }

    def Rectangle randomRectangle() {
        double ns1 = random.nextDouble()
        double ns2 = random.nextDouble()
        double ew1 = random.nextDouble()
        double ew2 = random.nextDouble()

        return new Rectangle(
                north: Math.max(ns1, ns2),
                south: Math.min(ns1, ns2),
                east: Math.max(ew1, ew2),
                west: Math.min(ew1, ew2)
        )
    }

    def runManyTimes(int times, Closure what) {
        for(int i=0;i<times;i++) {
            what()
        }
    }


    private boolean isOverlapping(Rectangle bottom, Rectangle top) {
        println(RectUtils.printRectangles(bottom, top))

        bottom.west < top.east && bottom.east > top.west &&
                bottom.south < top.north && bottom.north > top.south

    }
}