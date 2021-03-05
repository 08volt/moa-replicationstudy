package moa.classifiers.meta;


import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.core.Measurement;
import moa.core.MiscUtils;

import java.util.*;

public class OOB extends OzaBag {
    private static final long serialVersionUID = 1L;

    public FloatOption sizethreshold = new FloatOption("sizethreshold", 'z',
            "The minimum difference between class sizes to have imbalance", 0.6, 0, 1);
    public FloatOption recallthreshold = new FloatOption("recallthreshold", 'h',
            "The minimum difference between class recalls to have imbalance", 0.4, 0, 1);


    public FloatOption theta = new FloatOption("theta", 't',
            "The time decay factor for class size.", 0.9, 0, 1);

    public IntOption randomSeedOption = new IntOption("randomSeed", 'r', "Seed for random behaviour of the classifier.", 1);

    protected double[] classSize; // time-decayed size of each class

    protected double[] classRecall; // time-decayed size of each class

    protected Set<Integer> majorities, minorities, normal;
    private Random random_obj;

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

    protected void updateClassSize(Instance inst) {

        // Give all the classes the same weight at the beginning of the stream
        if (this.classSize == null) {
            classSize = new double[inst.numClasses()];
            Arrays.fill(classSize, 1d / classSize.length);
        }

        // update the class size with the decaying factor theta for all the classes
        for (int i=0; i<classSize.length; ++i) {
            classSize[i] = theta.getValue() * classSize[i] + (1d - theta.getValue()) * ((int) inst.classValue() == i ? 1d:0d);
        }
        updateClassRecall(inst);
    }

    protected void updateClassRecall(Instance inst){
        // Start with the same recall for all the classes at the beginning of the stream
        if (this.classRecall == null) {
            classRecall = new double[inst.numClasses()];
            Arrays.fill(classRecall, 1d);
        }
        int classk = (int)inst.classValue();
        // update the recall of the instance class
        classRecall[classk] = theta.getValue() * classRecall[classk] + (1d - theta.getValue()) * (correctlyClassifies(inst) ? 1d:0d);

        //compare the recall and size of the classes to assign them to the correct group
        for (int i=0; i<classSize.length-1; i++)
            for (int j=i; j<classSize.length; j++){
                // if the difference of the class sizes and recalls is above the corresponding thresholds assign them to the minorities and majorities groups
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
            // fill the normal group with the classes in both or none of the other groups
            if (minorities.contains(i))
                majorities.remove(i);
            if (!minorities.contains(i) && !majorities.contains(i))
                normal.add(i);
        }

    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        if (this.random_obj == null){
            this.random_obj = new Random(this.randomSeedOption.getValue());
        }
        // update the class sizes and recalls
        updateClassSize(inst);
        //compute the lambda for the poisson extraction
        double lambda = calculatePoissonLambda(inst);
        for (moa.classifiers.Classifier classifier : this.ensemble) {
            //extract the instance weight
            int k = MiscUtils.poisson(lambda, random_obj);
            if (k > 0) {
                Instance weightedInst = inst.copy();
                weightedInst.setWeight(inst.weight() * k);
                classifier.trainOnInstance(weightedInst);
            }
        }
    }

    //OOB lambda
    protected double calculatePoissonLambda(Instance inst) {
        // increase the lambda if the class is in the minorities group
        if (minorities.contains((int)inst.classValue()))
            return 1/classSize[(int)inst.classValue()];
        return 1d;
    }



    // will result in an error if classSize is not initialised yet
    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        Measurement [] measure = super.getModelMeasurementsImpl();
        Measurement [] measurePlus = null;

        if (classSize != null) {
            measurePlus = new Measurement[measure.length + classSize.length];
            System.arraycopy(measure, 0, measurePlus, 0, measure.length);

            for (int i=0; i<classSize.length; ++i) {
                String str = "size of class " + i;
                measurePlus[measure.length+i] = new Measurement(str,classSize[i]);
            }
        } else
            measurePlus = measure;

        return measurePlus;
    }

}
