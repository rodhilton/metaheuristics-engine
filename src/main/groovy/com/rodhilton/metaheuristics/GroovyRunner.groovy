package com.rodhilton.metaheuristics

import com.google.common.base.Supplier
import com.rodhilton.metaheuristics.collections.ScoredSet
import com.rodhilton.metaheuristics.simulator.Simulator
import com.rodhilton.metaheuristics.simulator.SimulatorCallback

import javax.imageio.ImageIO
import java.awt.Image
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicInteger

class GroovyRunner {
    public static void main(String[] args) {
        final int size=15;
        long seed = System.currentTimeMillis()
//        long seed = 1366305638180L
        println("RNG Seed: "+seed)
        Random random = new Random(seed);
//        random.setSeed(12345L);
        final Simulator simulator = new Simulator(new Supplier<GraphThing>() {

            @Override
            GraphThing get() {
                return new GraphThing(size, random);
            }
        })

        final AtomicInteger generation = new AtomicInteger()

        SimulatorCallback<GraphThing> printer = new SimulatorCallback<GraphThing>() {
            @Override
            void call(ScoredSet<GraphThing> everything) {
                GraphThing best = everything.getBest()
                println("${generation.incrementAndGet()}: ${best.fitness()}/${(size * (size - 1))/2}")
            }
        }

        SimulatorCallback<GraphThing> stopper = new SimulatorCallback<GraphThing>() {

            @Override
            void call(ScoredSet<GraphThing> everything) {
                GraphThing best = everything.getBest()
                if (best.fitness() >= (size * (size - 1))/2) {
                    simulator.stopSimulation();
                    println(best)
                    println(best.fullString())
                    for(int i=0;i<size;i++) {
                        BufferedImage render = renderGraphThing(best, i+1)
                        saveRender(render, "Saved${i+1}.png")
                    }
                }
            }
        }

        simulator.registerCallback(printer);
        simulator.registerCallback(stopper);

        simulator.startSimulation();

//        FileOutputStream fileStream = new FileOutputStream("foo.ser");
//        ObjectOutputStream os = new ObjectOutputStream(fileStream);
//        os.writeObject(testing);
//        os.close();
//
//        FileInputStream fileInputStream = new FileInputStream("foo.ser");
//        ObjectInputStream oInputStream = new ObjectInputStream(fileInputStream);
//        Object one = oInputStream.readObject();
//        Testing secondTesting = (Testing)one;
//        os.close();

        //println(secondTesting.mutate())


    }


    static BufferedImage renderGraphThing(GraphThing gt, int howMany) {
        gt.render(800,800, howMany);
    }

    static def saveRender(BufferedImage image, String filename) {
        try {
            // retrieve image
            File outputfile = new File(filename);
            ImageIO.write(image, "png", outputfile);
            println("written to ${outputfile.absolutePath}")
        } catch (IOException e) {
            e.printStackTrace()
        }
    }
}

