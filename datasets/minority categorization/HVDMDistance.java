package moa.classifiers.lazy.neighboursearch;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import org.kramerlab.autoencoder.math.matrix.Mat;

import javax.print.attribute.standard.MediaSize;
import java.util.*;


public class HVDMDistance extends NormalizableDistance
        implements Cloneable{

    ArrayList<Integer> categorical_indexes;
    ArrayList<Double> range = new ArrayList<>();
    HashMap<Integer, ArrayList<int[]> > count;


    public HVDMDistance(Instances df, ArrayList<Integer> categorical_indexes){
        this.categorical_indexes = categorical_indexes;
        for(int r = 0; r<41;r++)
            range.add(1.0);
        this.setInstances(df);

    }

    @Override
    public double distance(Instance first, Instance second, double cutOffValue) {
        //if(cutOffValue == cutOffValue & !Double.isInfinite(cutOffValue))
        //    System.out.println(cutOffValue);
        return this.distance(first, second);
    }

    @Override
    public double distance(Instance first, Instance second) {
        int numAtt = first.numAttributes();
        double[] result = new double[numAtt];
        for(int i = 1; i< numAtt; i++){
            if(categorical_indexes.contains(i)){
                int Na_first = count.get(i).get((int)first.value(i))[2];
                int Na_second = count.get(i).get((int)second.value(i))[2];

                int[] Nac_first = Arrays.copyOfRange(count.get(i).get((int)first.value(i)),0,2);
                int[] Nac_second = Arrays.copyOfRange(count.get(i).get((int)second.value(i)),0,2);

                if( Na_first != 0 && Na_second != 0) {
                    double[] p_first = {(double)Nac_first[0]/Na_first, (double)Nac_first[1]/Na_first};
                    double[] p_second = {(double)Nac_second[0]/Na_second, (double)Nac_second[1]/Na_second};
                    result[i] = Math.abs(p_first[0] - p_second[0]) + Math.abs(p_first[1] - p_second[1]);
                }
                else{
                    System.out.println("Division by 0 is not allowed");
                }
            }else{
                //System.out.println(Math.abs(first.value(i) -second.value(i))/this.range.get(i));
                result[i] = Math.abs(first.value(i) -second.value(i))/this.range.get(i);

            }
            result[i] *= result[i];
        }
        //System.out.println(Arrays.stream(result).sum());
        return Arrays.stream(result).sum();
    }

    @Override
    public String globalInfo() {
        return "Implementing Heterogeneous Value Difference Metric";
    }

    @Override
    public void setInstances(Instances df) {
        for(int col = 0; col < 40; col++){
            if(!categorical_indexes.contains(col)){
                double std = df.meanAndStd(col)[1];
                if(std == 0)
                {
                    System.out.println("COLONNA "+col+" STD=0 !!!!!!!!!!!!!!!!!!");
                    std = 0.00001;
                }
                range.set(col, 4 * std);
            }
        }
        this.count = new HashMap<>();
        int[] unique_values = {3,66,11,2,2};
        int u = 0;
        System.out.println(Arrays.toString(categorical_indexes.toArray()));
        for(int cat : categorical_indexes) {
            ArrayList<int[]> cat_count = new ArrayList<>();

            for (int att = 0; att< unique_values[u]; att++) {
                int[] class_count = {0, 0, 0};
                for (int i = 0; i < df.numInstances(); i++)
                    if( df.get(i).value(cat) == att)
                        class_count[(int) df.get(i).classValue()]++;
                class_count[2] = class_count[1] + class_count[0];
                System.out.println(cat +" " + att +  " counts: " + Arrays.toString(class_count));
                cat_count.add(class_count);
            }
            u++;
            this.count.put(cat, cat_count);
        }
    }

    @Override
    protected double updateDistance(double currDist, double diff) {
        System.out.println("INVALID CALL");
        return 0.0;
    }

    public ArrayList<Integer> getCategorical_indexes() {
        return categorical_indexes;
    }

    public void setCategorical_indexes(ArrayList<Integer> categorical_indexes) {
        this.categorical_indexes = categorical_indexes;
    }
}
