
### Replication study on Stream Machine Learning algorithms for Class Imbalance and Concept Drift

![](https://camo.githubusercontent.com/1ef7c1925e77c6f8b1c9f5adfdcea37dba30c98478264503067a91076db66144/687474703a2f2f6d6f612e636d732e7761696b61746f2e61632e6e7a2f77702d636f6e74656e742f75706c6f6164732f323031342f31312f4c6f676f4d4f412e6a7067)


**Table of Contents**

[TOCM]

#Introduction

Data Stream analysis is a rising field of Machine Learning where the data to be analyzed are coming from an infinite flow.
This is introducing new challenges regarding the use of a finite number of resourses and the need to be at least as fast as the this flow.
The difficulty on analyzind Data streams doesn't stop here. They usually have a very skewed class distribution known as **Class Imbalance**, and we need to be alert for **Concept Drifts**, indeed their distribution can change unpredictively over time.


#State of Art

I collected all the paper that i am referring to [here](papers/) 


#Dataset generation

I analyzed 3 types of drift with 4 imbalance rates for each Sea and Sine [datasets](datasets/Sea and Sine).
I generated 9 type with this data [genarator](datasets/Generator) each with 4 imbalance rates.
I also analyzed 3 real dataset you can find [here](datasets/Real). 


#Algorithms implementation

I implemented the algorithms in moa [here](algorithms code).
Here I uploaded only the corresponding java classes, for the complete moa fork refer to this [moa fork](https://github.com/08volt/moa "moa fork")).

#Experiments
I run 10 experiments for each Algorithm on each Dataset using an AWS virtual machine.
[Here] you can find the code to build the bash to run the experiments and the code to build the query to extract the results from influx.

#Results

F1 score:

![](results processing/plots/F1_%_4x3.png)

Recall:

![](results processing/plots/Recall_%_4x3.png)

Time and Memory:

![](results processing/plots/TM_GRPTYPE_ALL.png)
