package com.rodhilton.metaheuristics

import com.google.common.base.Supplier
import com.rodhilton.metaheuristics.collections.ScoredSet
import com.rodhilton.metaheuristics.simulator.Simulator
import com.rodhilton.metaheuristics.simulator.SimulatorCallback

class GroovyRunner {
    public static void main(String[] args) {
        final int size=9;
        Random random = new Random();
        final Simulator simulator = new Simulator(new Supplier<GraphThing>() {

            @Override
            GraphThing get() {
                return new GraphThing(size, random);
            }
        })

        SimulatorCallback printer = new SimulatorCallback<GraphThing>() {
            @Override
            void call(ScoredSet<GraphThing> everything) {
                GraphThing best = everything.getBest()
                println("${best}  -->  ${best.fitness()}")
            }
        }

        SimulatorCallback stopper = new SimulatorCallback<GraphThing>() {

            @Override
            void call(ScoredSet<GraphThing> everything) {
                GraphThing best = everything.getBest()
                if (best.fitness() >= (size * (size - 1))/2)
                    simulator.stopSimulation();
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
}

