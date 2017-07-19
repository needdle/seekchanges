# seekchanges
Seek Changes -- Localising where to change in code structures according to the feature requests

Usage: 

1. Compile src/mainprocess/SeekChanges.java into SeekChanges.jar
2. Run Seekchanges.jar in the command line: java -jar SeekChanges.jar datasets_dir experiment_dir alpha rho lambda, where

datasets_dir is the directory storing the 4 java projects
experiment_dir is the directory saving the experiment result
alpha (recommended value =  0.9) and rho (recommended value = 0.8) is the parameter for the propagation strategy
lambda (recommended value = 0.1) is the dirichlet smoothing parameter.

3. Find output.xls in the experiment_dir for the evaluation result.

The datasets are to be uploaded...

