package com.rodhilton.metaheuristics.simulator;

import com.google.common.io.Files;
import com.rodhilton.metaheuristics.algorithms.Metaheuristic;
import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulator<T> {
    private List<SimulatorCallback<T>> callbacks;
    private boolean stopRequested;
    private boolean paused;
    private Metaheuristic<T> algorithm;
    private String journalName;
    private String logName;
    private BigInteger iterations;
    private int generationSize;
    private Number bestKnownScore;

    public Simulator(Metaheuristic<T> algorithm) {
        this.callbacks = new ArrayList<SimulatorCallback<T>>();
        this.stopRequested = false;
        this.algorithm = algorithm;
        this.iterations = BigInteger.ZERO;
        this.bestKnownScore = null;
    }

    public void registerCallback(SimulatorCallback<T> callback) {
        callbacks.add(callback);
    }

    public void setJournalName(String name) {
        this.journalName = name + ".journal";
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public BigInteger getIterations() {
        return this.iterations;
    }


    @SuppressWarnings("unchecked")
    public void startSimulation() {
        String generationSizeProperty = System.getProperty("generationSize");


        generationSize = 100;
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
            iterations = BigInteger.ONE;
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
            //It's possible for the generation size to change, perhaps from loading a previous journal, or the size even being set dynamically.
            //So we need to, in the case of the generation size shrinking, only take the first n, and in the case of growing, duplicate random
            //elements until we get to the correct size

            if(generation.size() != generationSize) {
                if(generation.size() > generationSize) {
                    System.err.printf("Current generation size %d, should be %d, trimming", generation.size(), generationSize);
                } else {
                    System.err.printf("Current generation size %d, should be %d, expanding with repeats", generation.size(), generationSize);
                }

                //For too many
                generation = generation.subList(0, Math.min(generationSize, generation.size()));
                //For not enough
                while(generation.size() < generationSize) {
                    generation.add(generation.get(new Random().nextInt(generation.size())));
                }
            }

            iterations = iterations.add(BigInteger.ONE);

            saveJournal(iterations, generation);
            saveLog(scoredGeneration);
        }
    }

    private void saveLog(ScoredSet<T> generation) {
        if(!isLogging()) return;
        T best = generation.getBest();
        Number score = generation.getBestScore();

        if(bestKnownScore == null || score.doubleValue()>=bestKnownScore.doubleValue()) {
            try {
                String bestString = best.toString();
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(bestString.getBytes());
                BigInteger hash = new BigInteger(1, md5.digest());
                String hashFromContent = hash.toString(16);
                String hashedWithScore = score.toString() + "_" + hashFromContent;
                String newName = this.logName.replaceFirst("\\*", hashedWithScore);

                File dir = new File("logs");
                File logFile = new File(dir, newName);

                PrintWriter pw = new PrintWriter(new FileWriter(logFile));
                pw.write(bestString);
                pw.close();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isLogging() {
        if (logName == null) return false;
        File dir = new File("logs");
        dir.mkdirs();
        return dir.exists() && dir.canWrite();
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
            for (int i = 0; i < Math.min(size, generationSize); i++) {
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
                  .map((T t) -> new Result(t, algorithm.score(t)))
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




