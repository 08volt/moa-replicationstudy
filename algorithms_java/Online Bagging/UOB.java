package moa.classifiers.meta;

import com.yahoo.labs.samoa.instances.Instance;

public class UOB extends OOB {

    //UOB lambda
    @Override
    public double calculatePoissonLambda(Instance inst) {
        // decrease the lambda if the class is in the majorities group
        if (majorities.contains((int)inst.classValue()))
            return 1 - classSize[(int)inst.classValue()];
        return 1d;

    }


}
