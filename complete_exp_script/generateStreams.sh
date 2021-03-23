mkdir drifts_arff
echo 'generating experiment 0 drifts'
java -Xmx14g -Xss50M -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask 'WriteStreamToARFFFile -s (moa.dabrze.streams.generators.ImbalancedDriftGenerator -d appearing-minority/incremental,start=0,end=100000,value-start=0.0,value-end=1.0 -n 2 -m 0.1 -s 0.5 -b 0.5 -r 0) -f ./drifts_arff/appearing-minority-incremental-1-0.arff -m 100000' 
