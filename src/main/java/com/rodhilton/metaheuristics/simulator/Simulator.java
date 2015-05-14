package com.rodhilton.metaheuristics.simulator;

import com.google.common.io.Files;
import com.rodhilton.metaheuristics.algorithms.EvolutionaryAlgorithm;
import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Simulator<T> {
    private List<SimulatorCallback<T>> callbacks;
    private boolean stopRequested;
    private boolean paused;
    private EvolutionaryAlgorithm<T> algorithm;
    private String journalName;
    private BigInteger iterations;

    public Simulator(EvolutionaryAlgorithm<T> algorithm) {
        this.callbacks = new ArrayList<SimulatorCallback<T>>();
        this.stopRequested = false;
        this.algorithm = algorithm;
        this.iterations = BigInteger.ZERO;
    }

    public void registerCallback(SimulatorCallback<T> callback) {
        callbacks.add(callback);
    }

    public void setJournalName(String name) {
        this.journalName = name + ".journal";
    }

    public BigInteger getIterations() {
        return this.iterations;
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

        List<T> generation = new ArrayList<T>();

        boolean resumed = loadJournal(generation);

        if (isJournaling()) {
            System.err.println("Saving progress to journal file " + journalName);
        }

        if (!resumed) {
            for (int i = 0; i < generationSize; i++) {
                T newElement = algorithm.initialize();
                generation.add(newElement);
            }
        }

        while (!stopRequested) {

            while (paused) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //Ignore
                }
            }

            ScoredSet<T> scoredGeneration = scoreGeneration(generation);

            for (SimulatorCallback<T> callback : callbacks) {
                callback.call(scoredGeneration);
            }

            generation = algorithm.combine(scoredGeneration);
            iterations = iterations.add(BigInteger.ONE);

            if (generation.size() != generationSize)
                throw new IllegalStateException("Generation size has grown (was " + generationSize + ", now " + generation.size() + ").  This is likely a memory leak");

            saveJournal(iterations, generation);
        }
    }

    private boolean loadJournal(List<T> generation) {
        if (!isJournaling()) return false;
        //Save to journal if name is set and
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(journalName);
            ois = new ObjectInputStream(fis);
            iterations = (BigInteger) ois.readObject();
            int size = ois.readInt();
            for (int i = 0; i < size; i++) {
                generation.add((T) ois.readObject());
            }
            System.err.println("Loading iteration " + iterations + " from journal file " + journalName);
            ois.close();
            fis.close();
            return true;
        } catch (IOException e) {
            System.err.println("Problem loading journal file " + journalName + ", will not resume from previous run");
            e.printStackTrace(System.err);
        } catch (ClassNotFoundException e) {
            System.err.println("Problem loading journal file " + journalName + ", will not resume from previous run");
            e.printStackTrace(System.err);
        }
        return false;
    }

    private boolean isJournaling() {
        if (journalName == null) return false;
        File f = new File(journalName);
        return f.exists() && f.canWrite();
    }

    private void saveJournal(BigInteger iterations, List<T> generation) {
        //Save to journal if name is set and
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        if (journalName != null) {
            try {
                //This should be another file, and then copied.  In case the process is killed while writing
                fos = new FileOutputStream(journalName + ".tmp", false);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(iterations);
                oos.writeInt(generation.size());
                for (T ma : generation) {
                    oos.writeObject(ma);
                }
                oos.close();
                fos.close();
                Files.move(new File(journalName + ".tmp"), new File(journalName));
            } catch (IOException e) {
                System.err.println("Problem opening journal file " + journalName + ", progress will NOT be saved.");
                e.printStackTrace(System.err);
            }
        }
    }

    private ScoredSet<T> scoreGeneration(List<T> generation) {

        ScoredSet<T> scored = new ScoredSet<T>();

        generation.parallelStream()
                  .map((T t) -> new Result(t, algorithm.fitness(t)))
                  .forEach(result -> scored.add(result.getScore(), result.getGene()));


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

    private class Result {
        private T gene;
        private Number score;

        public Result(T gene, Number score) {
            this.gene = gene;
            this.score = score;
        }

        public T getGene() {
            return gene;
        }

        public Number getScore() {
            return score;
        }
    }
}




