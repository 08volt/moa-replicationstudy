package moa.classifiers.meta;


import com.github.javacliparser.FloatOption;
import com.yahoo.labs.samoa.instances.Instance;

import java.util.*;

public class OOB extends ImprovedOOB {
    private static final long serialVersionUID = 1L;

    public FloatOption sizethreshold = new FloatOption("sizethreshold", 'z',
            "The minimum difference between class sizes to have imbalance", 0.6, 0, 1);
    public FloatOption recallthreshold = new FloatOption("recallthreshold", 'r',
            "The minimum difference between class recalls to have imbalance", 0.4, 0, 1);

    protected double[] classRecall; // time-decayed size of each class

    protected Set<Integer> majorities, minorities, normal;

    @Override
    public String getPurposeString() {
        return "Oversampling on-line bagging of Wang et al IJCAI 2016.";
    }

    public OOB() {
        super();
        majorities = new HashSet<>();
        minorities = new HashSet<>();
        normal  = new HashSet<>();
    }

    @Override
    protected void updateClassSize(Instance inst) {
        //RECALL UPDATE
        if (this.classRecall == null) {
            classRecall = new double[inst.numClasses()];

            // <---le19/01/18 modification to start class size as equal for all classes
            Arrays.fill(classRecall, 1d);
        }


        int classk = (int)inst.classValue();

        classRecall[classk] = theta.getValue() * classRecall[classk] + (1d - theta.getValue()) * (correctlyClassifies(inst) ? 1d:0d);

        super.updateClassSize(inst);

        //IMBALANCE DETECTION ALGORITHM O(classSize.lenght**2)

        for (int i=0; i<classSize.length-1; i++)
            for (int j=i; j<classSize.length; j++){
                if(classSize[j] - classSize[i] > sizethreshold.getValue() && classRecall[j] - classRecall[i] > recallthreshold.getValue()){
                    minorities.add(i);
                    majorities.add(j);
                }
                else if(classSize[i] - classSize[j] > sizethreshold.getValue() && classRecall[i] - classRecall[j] > recallthreshold.getValue()){
                    minorities.add(j);
                    majorities.add(i);
                }
            }
        for (int i=0; i<classSize.length; i++) {
            if (minorities.contains(i))
                majorities.remove(i);
            if (!minorities.contains(i) && !majorities.contains(i))
                normal.add(i);
        }

    }

    // classInstance is the class corresponding to the instance for which we want to calculate lambda
    // will result in an error if classSize is not initialised yet
    @Override
    public double calculatePoissonLambda(Instance inst) {

        if (minorities.contains((int)inst.classValue()))
            return 1/classSize[(int)inst.classValue()];
        return 1d;

    }

}
