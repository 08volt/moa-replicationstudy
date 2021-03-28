import csv
import os
import subprocess

from Config import *


def buildDocker():
    dock = open("buildDocker.sh", "w")
    dock.write("echo 'test_moa'\n")
    dock.write("docker-compose up -d\n")
    dock.write("docker build . -t test_moa\n")
    dock.close()
    subprocess.run(["chmod", "+x", "./buildDocker.sh"])
    succ_docker = subprocess.call(['sh', './buildDocker.sh'])
    if succ_docker == 0:
        print("|" * 60)
        print(f"DOCKER IS UP")
        print("|" * 60)

def createDir():
    subprocess.run(["mkdir", "stats"])
    subprocess.run(["mkdir", "results"])
    for di, d in enumerate(drifts):
        subprocess.run(["mkdir", f"results/{d}"])
        for o in positives:
            subprocess.run(["mkdir", f"results/{d}/{str(o)}"])
            for alg in algorithms:
                subprocess.run(["mkdir", f"results/{d}/{str(o)}/{alg}"])
    if Real:
        for r in reals:
            subprocess.run(["mkdir", f"results/{r}"])
            for alg in algorithms:
                subprocess.run(["mkdir", f"results/{r}/{alg}"])

def generateArff():

    test = open("generateStreams.sh", "w")
    test.write("mkdir drifts_arff\n")
    for i in range(n_exp):
        test.write(f"echo 'generating experiment {i} drifts'\n")
        for o in positives:
            for d in drifts:
                for s in speeds:
                    if not os.path.isfile(f'./drifts_arff/{d}-{s}-{o}-{i}.arff'):
                        test.write(f"java -Xmx14g -Xss50M -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask '"
                                   f'WriteStreamToARFFFile -s '
                                   f'(moa.dabrze.streams.generators.ImbalancedDriftGenerator'
                                   f' -d {d}/{s},rs={drift_time[s][0]},re={drift_time[s][1]},start=0,end={stream_len},value-start=0.0,value-end=1.0'
                                   f' -n 2 -m 0.{o} -s {safe_ratio} -b {bord_ratio} -o {out_ratio} -p {rare_ratio} -r {seeds[i]}) -f '
                                   f"./drifts_arff/{d}-{s}-{o}-{i}.arff -m {stream_len}' \n")

    test.close()
    subprocess.run(["chmod", "+x", "./generateStreams.sh"])
    succ_gen = subprocess.call(['sh', './generateStreams.sh'])
    if succ_gen == 0:
        print("|" * 60)
        print(f"ARFF GENERATION SUCCESSFUL")
        print("|" * 60)

def createTest():
    test = open("Experiments.sh", "w")

    for i in range(n_exp):
        for alg in algorithms:

            l = f'(meta.{alg})'

            if alg in alg_strings:
                l = alg_strings[alg]

            for o in positives:
                for s in speeds:
                    test.write(f"echo '{alg} imb:{o} speed:{s} exp:{i}'\n")
                    for di, d in enumerate(drifts):


                        if Docker:
                            test.write(
                                f'sudo docker run --rm --name="{d}_{alg}_{s}_{str(o)}_{str(i)}" '
                                f'-v $(pwd)/results:/results test_moa bash -c '
                                f'"java -Xmx14g -Xss50M -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask \\"'
                                f'EvaluatePrequential -l {l} -s '
                                f'(ArffFileStream -f ./drifts_arff/{d}-{s}-{o}-{i}.arff) -e '
                                f'(WindowFixedClassificationPerformanceEvaluator -w {weval_artificial[s]} -r -f -g) -i -1 -f 5000\\" '
                                f'1> ./results/{d}/{str(o)}/{alg}/{s}_{str(i)}_err.csv 2> ./results/{d}/{str(o)}/{alg}/{s}_{str(i)}.csv"\n')
                        else:
                            test.write(
                                f"java -Xmx14g -Xss50M -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask '",
                                f'EvaluatePrequential -l {l} -s ',
                                f'(ArffFileStream -f ./drifts_arff/{d}-{s}-{o}-{i-1}.arff) -e ',
                                f"(WindowFixedClassificationPerformanceEvaluator -w {weval_artificial[s]} -r -f -g) -i -1 -f 5000' ",
                                f"1> results/Drifts/{d}/{str(o)}/{alg}/{s}_{str(i)}_err.csv 2> results/Drifts/{d}/{str(o)}/{alg}/{s}_{str(i)}.csv\n")
    if Real:
        for i in range(n_exp):
            for alg in algorithms:
                l = f'(meta.{alg})'

                if alg in alg_strings:
                    l = alg_strings[alg]

                for r in reals:
                    test.write(f"echo '{alg} ds:{r} exp:{i}'\n")
                    if Docker:
                        test.write(
                            f'sudo docker run --rm --name="{r}_{alg}_{str(i)}" '
                            f'-v $(pwd)/results:/results test_moa bash -c '
                            f'"java -Xmx14g -Xss50M -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask \\"'
                            f'EvaluatePrequential -l {l} -s '
                            f'(ArffFileStream -f ./real_arff/{r}.arff) -e '
                            f'({real_eval}) -i -1 -f 5000\\" '
                            f'1> ./results/{r}/{alg}/{str(i)}_err.csv 2> ./results/{r}/{alg}/{str(i)}.csv"\n')
                    else:
                        test.write(
                            f"java -Xmx14g -Xss50M -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask '",
                            f'EvaluatePrequential -l {l} -s ',
                            f'(ArffFileStream -f ./real_arff/{r}.arff) -e ',
                            f"({real_eval}) -i -1 -f 5000' ",
                            f"1> results/{r}/{alg}/{str(i)}_err.csv 2> results/{r}/{alg}/{str(i)}.csv\n")
    test.close()

def executeTest():
    subprocess.run(["chmod", "+x", "./Experiments.sh"])
    succ_test = subprocess.call(['sh', './Experiments.sh'])
    if succ_test == 0:
        print("|"*60 )
        print(f"EXPERIMENTS SUCCESSFUL")
        print("|" * 60)

def import_csv(csvfilename):
    data = []
    with open(csvfilename, "r") as scraped:
        reader = csv.reader(scraped, delimiter=',')
        row_index = 0
        for row in reader:
            if row:
                row_index += 1
                columns = [str(row_index)] + [row[i] for i in range(len(row))]
                data.append(columns)

    return data

def summarizeResults():
    writers = {}

    main_dir = "./results"

    for stat, size in stats.items():
        csv_file = open(f'stats/{stat}.csv', 'w')
        writer = csv.writer(csv_file)
        if len(size) == 1:
            writer.writerow(["drift", "imbalance", "speed", "alg", "exp", "instance", stat])
        elif len(size) == 2:
            writer.writerow(["drift", "imbalance", "speed", "alg", "exp", "instance", stat + "_0", stat + "_1"])
        writers[stat] = writer
    for d in drifts:
        path = main_dir + "/" + d

        for unbalance in positives:

            path1 = path + "/" + str(unbalance)

            for alg in os.listdir(path1):
                if "DS_Store" in alg:
                    continue
                path2 = path1 + "/" + alg

                print(f"{d} {unbalance} {alg}")
                # scan 10 experiments:
                for result in os.listdir(path2):
                    if "err" not in result or "DS_Store" in result:
                        continue

                    exp = -1

                    for i in range(n_exp):
                        if str(i) in result:
                            exp = i
                            break
                    assert exp != -1

                    speed = result[:result.index("_")]

                    data = import_csv(path2 + "/" + result)
                    start = False

                    for data_row in data:
                        if data_row[1] == "learning evaluation instances":
                            start = True
                            continue

                        if not start:
                            continue
                        for stat, pos in stats.items():
                            if data_row[pos[0]] == "?":
                                data_row[pos[0]] = "0.0"
                            if len(pos) == 1:

                                writers[stat].writerow(
                                    [d, unbalance, speed, alg, exp, int(float(data_row[1])), float(data_row[pos[0]])])
                            else:
                                if data_row[pos[1]] == "?":
                                    data_row[pos[1]] = "0.0"
                                writers[stat].writerow(
                                    [d, unbalance, speed, alg, exp, int(float(data_row[1])), float(data_row[pos[0]]),
                                     float(data_row[pos[1]])])
    if Real:
        for stat, size in stats.items():
            csv_file = open(f'stats/{stat}_real.csv', 'w')
            writer = csv.writer(csv_file)
            if len(size) == 1:
                writer.writerow(["dataset", "alg", "exp", "instance", stat])
            elif len(size) == 2:
                writer.writerow(["dataset", "alg", "exp", "instance", stat + "_0", stat + "_1"])
            writers[stat] = writer
        for r in reals:
            path = main_dir + "/" + r

            for alg in os.listdir(path):
                if "DS_Store" in alg:
                    continue
                path2 = path + "/" + alg

                # scan experiments:
                for result in os.listdir(path2):
                    if "err" not in result or "DS_Store" in result:
                        continue

                    exp = -1

                    for i in range(n_exp):
                        if str(i) in result:
                            exp = i
                            break
                    assert exp != -1

                    data = import_csv(path2 + "/" + result)
                    start = False

                    for data_row in data:
                        if data_row[1] == "learning evaluation instances":
                            start = True
                            continue

                        if not start:
                            continue
                        for stat, pos in stats.items():
                            if data_row[pos[0]] == "?":
                                data_row[pos[0]] = "0.0"
                            if len(pos) == 1:

                                writers[stat].writerow(
                                    [r, alg, exp, int(float(data_row[1])), float(data_row[pos[0]])])
                            else:
                                if data_row[pos[1]] == "?":
                                    data_row[pos[1]] = "0.0"
                                writers[stat].writerow(
                                    [r, alg, exp, int(float(data_row[1])), float(data_row[pos[0]]),float(data_row[pos[1]])])



if __name__ == '__main__':
    createDir()
    generateArff()
    testfile = createTest()
    if Docker:
        buildDocker()
    executeTest()
    summarizeResults()







