##########################################
#             Configurations             #
##########################################


clusterGen = True
real = True


n_exp = 2

seeds = [0,11,13,17,23,30,39,912,1237,7591,12,213]

algorithms = ["HoeffdingAdaptiveTree"]#["OzaBag","ImprovedOOB","ImprovedUOB","OOB","UOB","WEOB1","WEOB2","RebalanceStream"]

speeds = ["incremental"]#,"sudden", "periodic"]

reals = ["Elec","PAKDD", "KDDCup"]

drifts = ["appearing-minority"]#, "disappearing-minority", "jitter", "clusters-movement", "appearing-clusters",
             #"splitting-clusters", "borderline", "shapeshift", "minority-share"]

positives = ["1","2","3","4"]

stats = {

    "Fscore": [9, 10],
    "Recall": [11, 12],
    "Gmean": [13],
    "Ktemp": [7]
}