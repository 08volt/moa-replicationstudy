import numpy as np 
from VDM import VDM

class HVDM(VDM):
    def __init__(self, X , y_ix, cat_ix, normalised="variance"):
        """ Heterogeneous Value Difference Metric
        Distance metric class which initializes the parameters
        used in hvdm() function
        
        Parameters
        ----------
        X : array-like of shape = [n_rows, n_features]
            Dataset that will be used with HVDM. Needs to be provided
            here because minimum and maximimum values from numerical
            columns have to be extracted
            
        y_ix : int array-like, list of shape [1]
            Single element array with indices for the categorical output variable
            If y is numerical it should be converted to categorical (if it makes sense)
        
        cat_ix : array-like of shape = [cat_columns_number]
            List containing categorical feature indices
        
        cat_ix : array-like of shape = [x]
            List containing missing values indicators

        normalised: string
            Normalises euclidan distance function for numerical variables
            Can be set as "std". The other option is a column range

        Returns
        -------
        None
        """        
        # Initialize VDM object
        super().__init__(X, y_ix, cat_ix)
        self.n = 0
        self.cat_ix = cat_ix
        self.col_ix = [i for i in range(X.shape[1] -1)]
        # Get the normalization scheme for numerical variables
        if normalised == "std":
            self.range = 4* np.nanstd(X.drop(y_ix,axis = 1), axis = 0)
        else:
            self.range = np.nanmax(X.drop(y_ix,axis = 1), axis = 0) - np.nanmin(X.drop(y_ix,axis = 1), axis = 0)

    
    def hvdm(self, x, y):
        """ Heterogeneous Value Difference Metric
        Distance metric function which calculates the distance
        between two instances. Handles heterogeneous data and missing values.
        For categorical variables, it uses conditional probability 
        that the output class is given 'c' when attribute 'a' has a value of 'n'.
        For numerical variables, it uses a normalized Euclidan distance.
        It can be used as a custom defined function for distance metrics
        in Scikit-Learn
        
        Parameters
        ----------
        x : array-like of shape = [n_features]
            First instance 
            
        y : array-like of shape = [n_features]
            Second instance
        Returns
        -------
        result: float
            Returns the result of the distance metrics function
        """
        # Initialise results array
        results_array = np.zeros(x.shape)

        # Get categorical indices without missin

        # Calculate the distance for categorical elements
        results_array[self.cat_ix] = super().vdm(x, y)[self.cat_ix]
        # Get numerical indices without missing values elements
        num_ix = np.setdiff1d(self.col_ix, self.cat_ix)
        # Calculate the distance for numerical elements
        results_array[num_ix] = np.abs(x[num_ix] - y[num_ix]) / self.range[num_ix]
        # Return the final result
        # Square root is not computed in practice
        # As it doesn't change similarity between instances
        if self.n % 50000 == 0:
            print(int(self.n/50000))
        self.n += 1
        return np.sum(np.square(results_array))