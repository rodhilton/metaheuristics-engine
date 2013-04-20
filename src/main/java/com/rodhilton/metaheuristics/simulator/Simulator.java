package com.rodhilton.metaheuristics.simulator;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.rodhilton.metaheuristics.algorithms.MetaheuristicAlgorithm;
import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static com.google.common.collect.Collections2.transform;

public class Simulator {
    private List<SimulatorCallback<MetaheuristicAlgorithm>> callbacks;
    private boolean stopRequested;
    private boolean paused;
    private Supplier<MetaheuristicAlgorithm> supplier;

    public Simulator(Supplier<MetaheuristicAlgorithm> supplier) {

        this.callbacks = new ArrayList<SimulatorCallback<MetaheuristicAlgorithm>>();
        this.stopRequested = false;
        this.supplier = supplier;
    }

    public void registerCallback(SimulatorCallback<MetaheuristicAlgorithm> callback) {
        callbacks.add(callback);
    }


    @SuppressWarnings("unchecked")
    public void startSimulation() {
        String generationSizeProperty = System.getProperty("generationSize");

        int generationSize = 100;
        if (generationSizeProperty != null) {
            try {
                generationSize = Integer.parseInt(generationSizeProperty);
            } catch (NumberFormatException e) {
                System.err.println("Generation size is not a number: " + generationSizeProperty);
            }
        }

        List<MetaheuristicAlgorithm> generation = new ArrayList<MetaheuristicAlgorithm>();
        for (int i = 0; i < generationSize; i++) {
            MetaheuristicAlgorithm newElement = supplier.get();
            generation.add(newElement);
        }

        while (!stopRequested) {

            while (paused) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //Ignore
                }
            }

            ScoredSet<MetaheuristicAlgorithm> scoredGeneration = scoreGeneration(generation);

            for (SimulatorCallback<MetaheuristicAlgorithm> callback : callbacks) {
                callback.call(scoredGeneration);
            }

            MetaheuristicAlgorithm best = scoredGeneration.getBest();
            generation = best.combine(scoredGeneration);

            if (generation.size() != generationSize)
                throw new IllegalStateException("Generation size has grown (was " + generationSize + ", now " + generation.size() + ").  This is likely a memory leak");
        }
    }

    private ScoredSet<MetaheuristicAlgorithm> scoreGeneration(List<MetaheuristicAlgorithm> generation) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        ScoredSet<MetaheuristicAlgorithm> scored = new ScoredSet<MetaheuristicAlgorithm>();

        try {
            List<Future<Result>> futures = executor.invokeAll(scorersFor(generation));
            executor.shutdown();

            for (Future<Result> resultFuture : futures) {
                Result result = resultFuture.get();
                scored.add(result.getScore(), result.gene);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return scored;
    }

    public void pauseSimulation() {
        paused = true;
    }

    public void unpauseSimulation() {
        paused = false;
    }

    public void togglePause() {
        paused = !paused;
    }

    public boolean getPaused() {
        return paused;
    }

    public void stopSimulation() {
        this.stopRequested = true;
    }

    private Collection<Callable<Result>> scorersFor(Collection<MetaheuristicAlgorithm> population) {
        return transform(population, new Function<MetaheuristicAlgorithm, Callable<Result>>() {
            @Override
            public Callable<Result> apply(MetaheuristicAlgorithm input) {
                return new Scorer(input);
            }
        });
    }

    private class Scorer implements Callable<Result> {
        private MetaheuristicAlgorithm gene;

        Scorer(MetaheuristicAlgorithm gene) {
            this.gene = gene;
        }

        @Override
        public Result call() throws Exception {
            return new Result(gene, gene.fitness());
        }
    }

    private class Result {
        private MetaheuristicAlgorithm gene;
        private Number score;

        public Result(MetaheuristicAlgorithm gene, Number score) {
            this.gene = gene;
            this.score = score;
        }

        public MetaheuristicAlgorithm getGene() {
            return gene;
        }

        public Number getScore() {
            return score;
        }
    }
}




