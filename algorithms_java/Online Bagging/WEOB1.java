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
    public FloatOption recalltheta = new FloatOption("recalltheta", 't',
            "The time decay factor for class recall", 0.9, 0, 1);

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "trees.HoeffdingAdaptiveTree");

    public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's',
            "The number of models in the bag.", 10, 1, Integer.MAX_VALUE);

    public IntOption SmoothedRecallWindowSizeOption = new IntOption("SmoothedRecallWindowSizeOption", 'w',
            "The size of the window to compute the smoothed recall ", 1000, 1, Integer.MAX_VALUE);

    public IntOption randomSeedOption = new IntOption("randomSeed", 'r', "Seed for random behaviour of the classifier.", 1);


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

        //initialize the smoothed recalls of UOB and OOB
        if(classRecallUOB == null){
            oob.randomSeedOption.setValue(this.randomSeedOption.getValue());
            uob.randomSeedOption.setValue(this.randomSeedOption.getValue());
            classRecallOOB = new SmoothedRecall(inst.numClasses(),recalltheta.getValue(),SmoothedRecallWindowSizeOption.getValue());
            classRecallUOB = new SmoothedRecall(inst.numClasses(),recalltheta.getValue(),SmoothedRecallWindowSizeOption.getValue());
        }

        // compute the corresponding gmeans
        double uobGmean = classRecallUOB.getGmean();
        double oobGmean = classRecallOOB.getGmean();

        // normalize the gmeans values
        double alphaO = oobGmean / (oobGmean + uobGmean);
        double alphaU = uobGmean / (oobGmean + uobGmean);

        // sum the votes of the OOB and UOB weighted with the normalized gmeans
        for(int i = 0; i < finalVotes.length; i++){
            try {
                finalVotes[i] = alphaO * oobVotes[i] + alphaU * uobVotes[i];
            }catch (IndexOutOfBoundsException e){
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
            oob.randomSeedOption.setValue(this.randomSeedOption.getValue());
            uob.randomSeedOption.setValue(this.randomSeedOption.getValue());
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
