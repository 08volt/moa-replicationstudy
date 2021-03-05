/**
 * Author: Leandro L. Minku (leandro.minku@leicester.ac.uk)
 * Implementation of Oversampling Online Bagging as in
 * WANG, S.; MINKU, L.L.; YAO, X. "Dealing with Multiple Classes in Online Class Imbalance Learning",
 * Proceedings of the 25th International Joint Conference on Artificial Intelligence (IJCAI'16), July 2016
 *
 * Please note that this was not the implementation used in the experiments done in the paper above.
 * However, it implements the algorithm proposed in that paper. So, it should reflect those results.
 *
 */
//Deeply modified by me
// <---le19/01/18 modification to start class size as equal for all classes

package moa.classifiers.meta;
import com.yahoo.labs.samoa.instances.Instance;

public class ImprovedOOB extends OOB {
	
	private static final long serialVersionUID = 1L;

	@Override
    public String getPurposeString() {
        return "Oversampling on-line bagging of Wang et al IJCAI 2016.";
    }


	public ImprovedOOB() {
		super();
	}

	@Override
	protected double calculatePoissonLambda(Instance inst) {
		int majClass = getMajorityClass();
		return classSize[majClass] / classSize[(int) inst.classValue()];

	}

	// find the index of the class with the bigger size
	protected int getMajorityClass() {
		int indexMaj = 0;

		for (int i=1; i<classSize.length; ++i) {
			if (classSize[i] > classSize[indexMaj]) {
				indexMaj = i;
			}
		}
		return indexMaj;
	}



}