##########################################
#             Configurations             #
##########################################


clusterGen = True
real = True



Docker = True
Real = True

n_exp = 1

seeds = [0,11,13,17,23,30,39,912,1237,7591,12,213]

algorithms = ["HoeffdingAdaptiveTree"]#,"OzaBag","ImprovedOOB","ImprovedUOB","OOB","UOB","WEOB1","WEOB2","RebalanceStream"]

speeds = ["incremental"]#,"sudden", "periodic"]

reals = ["Elec"]#,"PAKDD", "KDDCup"]

drifts = ["appearing-minority"]#, "disappearing-minority", "jitter", "clusters-movement", "appearing-clusters",
             #"splitting-clusters", "borderline", "shapeshift", "minority-share"]

positives = ["1"]#,"2","3","4"]

real_eval = "FadingFactorClassificationPerformanceEvaluator -a 0.995 -r -f -g"

stream_len = "100000"

drift_time = {
    "sudden": ["50000","50000"],
    "periodic": ["45000","55000"],
    "incremental": ["45000","55000"]
}

safe_ratio = "0.5"
bord_ratio = "0.5"
out_ratio = "0.0"
rare_ratio = "0.0"


weval_artificial = {
    "sudden" : "50000",
    "periodic" : "45000 -j 10000",
    "incremental" : "45000 -j 10000"
}

stats = {

    "Fscore": [9, 10],
    "Recall": [11, 12],
    "Gmean": [13],
    "Ktemp": [7]
}


