# Instance files

The are located inside the directory
```
jMetal/src/jmetal/data/CVRPTW
```
The ones with an underscore (30 in total) where used to compare the NSGAII against the greedys; the other ones (56 in total) where used in the parametric configuration.

# Executing the greedys

You just have to define a method which calls execute method of the class Greedy with an instance of DistanceGreedy or FuelGreedy.
GenerateFronts class generates the fronts from the greedy solution, writing them inside directory
```
jMetal/src/greedys/paretoFronts
```

# Executing NSGAII on an instance

Call the main method of NSGA_II in jmetal.metaheuristics.nsgaII.NSGAII_main with the parameter CVRPTW<instance name>.
For example, the parameter CVRPTWC101 represents the CVRPTW problem with the instance C101. The instance file name must correspond with the instance name, must end with .txt and must be located in
```
jMetal/src/jmetal/data/CVRPTW
```

# Experiments

All experiments (parametric configurations and experimental evaluations) are inside the directory
```
jMetal/src/jmetal/experiments/studies
```

## Parametric configurations

The are two different parametric configurations, called ParametricConfiguration.java and ParametricConfiguration2.java

## Experimental evaluation

Its called ExperimentalEvaluation.java