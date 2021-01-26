package moa.classifiers.meta;

import com.yahoo.labs.samoa.instances.Instance;

public class WEOB2 extends WEOB1{

    @Override
    public double[] getVotesForInstance(Instance inst) {
        double[] oobVotes = oob.getVotesForInstance(inst);
        double[] uobVotes = uob.getVotesForInstance(inst);

        if(classRecallUOB == null){
            classRecallOOB = new SmoothedRecall(inst.numClasses(),recalltheta.getValue(),SmoothedRecallWindowSizeOption.getValue());
            classRecallUOB = new SmoothedRecall(inst.numClasses(),recalltheta.getValue(),SmoothedRecallWindowSizeOption.getValue());
        }

        double uobGini = classRecallUOB.getGmean();
        double oobGini = classRecallUOB.getGmean();

        if (oobGini>uobGini){
            return oobVotes;
        }
        return uobVotes;


    }
}
