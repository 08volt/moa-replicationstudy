package moa.classifiers.meta;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.capabilities.CapabilitiesHandler;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import moa.options.ClassOption;

import java.util.Arrays;

public class WEOB1 extends AbstractClassifier implements MultiClassClassifier,
        CapabilitiesHandler {

    private static final long serialVersionUID = 1L;

    public FloatOption oobtheta = new FloatOption("oobtheta", 'o',
            "The time decay factor for oob classifier", 0.9, 0, 1);
    public FloatOption uobtheta = new FloatOption("uobtheta", 'u',
            "The time decay factor for uob classifier", 0.9, 0, 1);
    public FloatOption recalltheta = new FloatOption("recalltheta", 'r',
            "The time decay factor for class recall", 0.9, 0, 1);

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "trees.HoeffdingAdaptiveTree");

    public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's',
            "The number of models in the bag.", 10, 1, Integer.MAX_VALUE);

    public IntOption SmoothedRecallWindowSizeOption = new IntOption("SmoothedRecallWindowSizeOption", 'w',
            "The size of the window to compute the smoothed recall ", 51, 1, Integer.MAX_VALUE);

    protected ImprovedOOB oob;
    protected ImprovedUOB uob;
    SmoothedRecall classRecallOOB;
    SmoothedRecall classRecallUOB;

    public WEOB1(){
        super();
        oob = new ImprovedOOB();
        uob = new ImprovedUOB();
        oob.theta.setValue(this.oobtheta.getValue());
        oob.ensembleSizeOption.setValue(ensembleSizeOption.getValue());
        oob.baseLearnerOption = this.baseLearnerOption;
        uob.theta.setValue(this.uobtheta.getValue());
        uob.ensembleSizeOption.setValue(ensembleSizeOption.getValue());
        uob.baseLearnerOption = this.baseLearnerOption;
        oob.prepareForUse();
        uob.prepareForUse();


    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        double[] oobVotes = oob.getVotesForInstance(inst);
        double[] uobVotes = uob.getVotesForInstance(inst);
        double[] finalVotes = new double[oobVotes.length];

        if(classRecallUOB == null){
            classRecallOOB = new SmoothedRecall(inst.numClasses(),recalltheta.getValue(),SmoothedRecallWindowSizeOption.getValue());
            classRecallUOB = new SmoothedRecall(inst.numClasses(),recalltheta.getValue(),SmoothedRecallWindowSizeOption.getValue());
        }

        double uobGini = classRecallUOB.getGmean();
        double oobGini = classRecallOOB.getGmean();

        double alphaO = oobGini / (oobGini + uobGini);
        double alphaU = uobGini / (oobGini + uobGini);

        for(int i = 0; i < oobVotes.length; i++){
            try {
                finalVotes[i] = alphaO * oobVotes[i] + alphaU * uobVotes[i];
            }catch (IndexOutOfBoundsException e){
                System.out.println(Arrays.toString(uobVotes));
                System.out.println(Arrays.toString(oobVotes));
                finalVotes[i] = 0;
            }
        }
        return finalVotes;


    }

    @Override
    public void resetLearningImpl() {
        oob.resetLearningImpl();
        uob.resetLearningImpl();
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        //initialize recalls
        if(classRecallUOB == null){
            classRecallOOB = new SmoothedRecall(inst.numClasses(),recalltheta.getValue(),SmoothedRecallWindowSizeOption.getValue());
            classRecallUOB = new SmoothedRecall(inst.numClasses(),recalltheta.getValue(),SmoothedRecallWindowSizeOption.getValue());
        }
        classRecallOOB.insertPrediction((int)inst.classValue(),oob.correctlyClassifies(inst));
        classRecallUOB.insertPrediction((int)inst.classValue(),uob.correctlyClassifies(inst));

        oob.trainOnInstanceImpl(inst);
        uob.trainOnInstanceImpl(inst);


    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return oob.getModelMeasurementsImpl();
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {

    }

    @Override
    public boolean isRandomizable() {
        return true;
    }
}
