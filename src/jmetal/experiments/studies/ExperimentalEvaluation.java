//  ExperimentalEvaluation.java
//
//  Authors:
//       Nicolás Rodríguez <marconicolasrodriguez@gmail.com>
//
//  Copyright (c) 2018 Nicolás Rodríguez
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.experiments.studies;

import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;
import jmetal.experiments.Settings;
import jmetal.experiments.settings.NSGAII_Settings;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class implementing experimental evaluation. The configuration paramenters for NSGAII is
 * taken from the parametric configuration.
 * Hypervolume, spread, additive epsilon and inter generational distance indicators are used for performance assessment.
 * In this experiment, we assume that the true Pareto fronts are unknown, so 
 * they must be calculated automatically. 
 */
public class ExperimentalEvaluation extends Experiment {

  /**
   * Configures the algorithms in each independent run
   * @param problemName The problem to solve
   * @param problemIndex
   * @throws ClassNotFoundException 
   */
  public void algorithmSettings(String problemName, 
  		                          int problemIndex, 
  		                          Algorithm[] algorithm) throws ClassNotFoundException {
    try {
      int numberOfAlgorithms = algorithmNameList_.length;

      HashMap[] parameters = new HashMap[numberOfAlgorithms];

      for (int i = 0; i < numberOfAlgorithms; i++) {
        parameters[i] = new HashMap();
      } // for

      if (!(paretoFrontFile_[problemIndex] == null) && !paretoFrontFile_[problemIndex].equals("")) {
        for (int i = 0; i < numberOfAlgorithms; i++)
          parameters[i].put("paretoFrontFile_", paretoFrontFile_[problemIndex]);
        } // if

        double mutProb = 0.1;
        double crossProb = 0.9;
        int popNum = 50;
        parameters[0].put("populationSize_", popNum);
        parameters[0].put("crossoverProbability_", crossProb);
        parameters[0].put("mutationProbability_", mutProb);
        algorithm[0] = new NSGAII_Settings(problemName).configure(parameters[0]);
      } catch (IllegalArgumentException ex) {
      Logger.getLogger(ExperimentalEvaluation.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(ExperimentalEvaluation.class.getName()).log(Level.SEVERE, null, ex);
    } catch  (JMException ex) {
      Logger.getLogger(ExperimentalEvaluation.class.getName()).log(Level.SEVERE, null, ex);
    }
  } // algorithmSettings

  /**
   * Main method
   * @param args
   * @throws JMException
   * @throws IOException
   */
  public static void main(String[] args) throws JMException, IOException {
    ExperimentalEvaluation exp = new ExperimentalEvaluation();

    exp.experimentName_ = "ExperimentalEvaluation";
    exp.algorithmNameList_ = new String[1];
    exp.algorithmNameList_[0] = "NSGAII";

    exp.problemList_ = new String[30];
    for (int i = 0; i <= 9; i++) exp.problemList_[i] = "CVRPTW" + "C110_" + (i+1);
    for (int i = 10; i <= 19; i++) exp.problemList_[i] = "CVRPTW" + "R110_" + (i-9);
    for (int i = 20; i <= 29; i++) exp.problemList_[i] = "CVRPTW" + "RC110_" + (i-19);

    exp.paretoFrontFile_ = new String[30]; // Space allocation for 30 fronts

    exp.indicatorList_ = new String[]{"HV", "IGD", "SPREAD", "EPSILON"};

    int numberOfAlgorithms = exp.algorithmNameList_.length;

    exp.experimentBaseDirectory_ = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/" +
                                   exp.experimentName_;
    exp.paretoFrontDirectory_ = "" ; // This directory must be empty

    exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

    exp.independentRuns_ = 1;

    exp.initExperiment();

    // Run the experiments
    int numberOfThreads;
    exp.runExperiment(numberOfThreads = 4);
    exp.generateQualityIndicators();

    // Generate latex tables
    exp.generateLatexTables();

  } // main
} // ParametricConfiguration


