package moa;

import com.yahoo.labs.samoa.instances.*;
import moa.classifiers.lazy.neighboursearch.HVDMDistance;
import moa.classifiers.lazy.neighboursearch.LinearNNSearch;
import moa.classifiers.lazy.neighboursearch.NearestNeighbourSearch;
import java.io.*;
import java.util.*;

/**
 * CREATE A DICT-LIKE FILE:
 * KEY : [LIST OF VALUE]
 * Where the key is the index of the instance and
 * the list is composed by the indexes of the 5 nearest neighbours of the instance key.
 */
public class Categorize_minority {

    public Categorize_minority(){
    }


    public static void main(String[] args) throws IOException {
        Categorize_minority exp = new Categorize_minority();

        System.out.println(args[0]+ "preprocessing_KDDCup.csv");
        exp.kneighbours(args[0]);

    }

    public void kneighbours(String path){
        String dir = path;
        String csvFile = dir + "preprocessing_KDDCup.csv";
        Integer[] cat_idx = {2,3,4,7,12};
        ArrayList<Integer> cat_idxx = new ArrayList<>();
        Collections.addAll(cat_idxx, cat_idx);
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        Attribute[] attributes = new Attribute[40];
        for(int a = 0; a<40; a++)
        {
            attributes[a] = new Attribute();

        }

        Instances df = new Instances("KDDCup",attributes,500000);
        int cnt = 0;
        try {

            br = new BufferedReader(new FileReader(csvFile));

            while ((line = br.readLine()) != null) {


                String[] l = line.split(cvsSplitBy);
                InstanceImpl inst = new InstanceImpl(l.length -1);

                for (int i=0; i< l.length-1; i++)
                    inst.setValue(i, Double.parseDouble(l[i]));


                inst.setClassValue(Double.parseDouble(l[l.length-1]));

                df.add(inst);
                cnt++;
                if(cnt%10000 == 0)
                    System.out.println(cnt);

            }


            System.out.println("start NN search");
            NearestNeighbourSearch search = new LinearNNSearch(df);

            search.setDistanceFunction(new HVDMDistance(df, cat_idxx));
            Instances kn;
            for(int i=0; i<df.numInstances(); i++) {
                kn = search.kNearestNeighbours(df.get(i), 6);
                int[] res = new int[5];
                for( int k = 0; k<5; k++){
                    res[k] = (int)kn.get(k).value(0);

                }

                System.out.println(df.get(i).classValue());
                System.out.println(Arrays.toString(res));

                File f1 = new File("KDDCup_results.txt");
                if(!f1.exists()) {
                    f1.createNewFile();
                }
                FileWriter fileWritter = new FileWriter(f1.getName(),true);
                BufferedWriter bw = new BufferedWriter(fileWritter);
                bw.write(df.get(i).value(0) + " : "+ Arrays.toString(res) + "\n");
                bw.close();


            }





        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}