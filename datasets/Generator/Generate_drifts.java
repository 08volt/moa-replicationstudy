package moa;


import moa.tasks.WriteStreamToARFFFile;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Generate csv files
 * for each drift in the driftsByName list
 * for each speed in the speeds array
 * for each imbalance in the minority_shares array
 *
 */

public class Generate_drifts {
    public void run(String root_path, double borderline){
        ArrayList<String> driftsByName = new ArrayList<>();
        driftsByName.add("appearing-minority");
        driftsByName.add("disappearing-minority");
        driftsByName.add("jitter");
        driftsByName.add("clusters-movement");
        driftsByName.add("appearing-clusters");
        driftsByName.add("splitting-clusters");
        driftsByName.add("borderline");
        driftsByName.add("shapeshift");
        driftsByName.add("minority-share");

        String[] speeds = {"incremental","sudden","periodic"};
        int[][] startend = new int[3][2];
        startend[1][0] = 50000;
        startend[1][1] = 50000;
        startend[0][0] = 45000;
        startend[0][1] = 55000;
        startend[2][0] = 45000;
        startend[2][1] = 55000;

        int[] minority_shares = {4,2,3,1};

        for(String name: driftsByName){
            for (int minority: minority_shares){
                try {

                    Path path = Paths.get(root_path + name + "/" + minority);

                    Files.createDirectories(path);

                    System.out.println("Directory is created!");

                } catch (IOException e) {

                    System.err.println("Failed to create directory!" + e.getMessage());

                }

                for(int s = 0; s < speeds.length; s++) {

                    String cli = "moa.dabrze.streams.generators.ImbalancedDriftGenerator -d " +
                            name +
                            "/" + speeds[s] +
                            ",start=0,end=1000000,value-start=0.0,value-end=1.0" +
                            " -n 2 -m 0." + minority +
                            " -s "+(1 - borderline) + " -b "+(borderline);
                    WriteStreamToARFFFile file = new WriteStreamToARFFFile();
                    file.arffFileOption.setValue(root_path + name +
                            "/" + minority + "/" + name + speeds[s] + ".arff");
                    file.streamOption.setValueViaCLIString(cli);
                    file.maxInstancesOption.setValue(100000);
                    file.prepareForUse();
                    System.out.println(cli);
                    file.doTask();
                    System.out.println(minority + " - " + name + " - " + speeds[s] + " arff");
                }


            }


        }

    }

    public static void main(String[] args) throws IOException {
        String path= "/Users/08volt/Desktop/Drifts/";
        double borderline = 0.5;

        Generate_drifts exp = new Generate_drifts();
        exp.run(path, borderline);
    }
}
