//  ParametricConfiguration.java
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
import jmetal.experiments.util.Friedman;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class implementing parametric configuration. Distinct parametric configurations of NSGAII algorithms are
 * compared when solving the CVRPTW benchmarks, and the hypervolume,
 * spread and additive epsilon indicators are used for performance assessment.
 * In this experiment, we assume that the true Pareto fronts are unknown, so 
 * they must be calculated automatically. 
 */
public class ParametricConfiguration2 extends Experiment {

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


        double[] mutProb = new double[3];
        mutProb[0] = 0.1; mutProb[1] = 0.01; mutProb[2] = 0.001;
        double[] crossProb = new double[3];
        crossProb[0] = 0.7; crossProb[1] = 0.8; crossProb[2] = 0.9;
        for (int i = 0; i < 3; i++) {
          for (int j = 0; j < 3; j++) {
            parameters[3*i+j].put("crossoverProbability_", crossProb[j]);
            parameters[3*i+j].put("mutationProbability_", mutProb[i]);
            algorithm[3*i+j] = new NSGAII_Settings(problemName).configure(parameters[3*i+j]);
          }
        }
      } catch (IllegalArgumentException ex) {
      Logger.getLogger(ParametricConfiguration2.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(ParametricConfiguration2.class.getName()).log(Level.SEVERE, null, ex);
    } catch  (JMException ex) {
      Logger.getLogger(ParametricConfiguration2.class.getName()).log(Level.SEVERE, null, ex);
    }
  } // algorithmSettings

  /**
   * Main method
   * @param args
   * @throws JMException
   * @throws IOException
   */
  public static void main(String[] args) throws JMException, IOException {
    ParametricConfiguration2 exp = new ParametricConfiguration2();

    exp.experimentName_ = "ParametricConfiguration";
    exp.algorithmNameList_ = new String[9];
    for (int i = 0; i < 9; i++) exp.algorithmNameList_[i] = "NSGAII" + (i+1);

    exp.problemList_ = new String[29];
    for (int i = 0; i <= 8; i++) exp.problemList_[i] = "CVRPTW" + "C10" + (i+1);
    for (int i = 9; i <= 17; i++) exp.problemList_[i] = "CVRPTW" + "R10" + (i-8);
    for (int i = 18; i <= 20; i++) exp.problemList_[i] = "CVRPTW" + "R1" + (i-8);
    for (int i = 21; i <= 28; i++) exp.problemList_[i] = "CVRPTW" + "RC10" + (i-20);

    exp.paretoFrontFile_ = new String[29]; // Space allocation for 56 fronts

    exp.indicatorList_ = new String[]{"HV", "IGD", "SPREAD", "EPSILON"};

    int numberOfAlgorithms = exp.algorithmNameList_.length;

    exp.experimentBaseDirectory_ = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/" +
                                   exp.experimentName_;
    exp.paretoFrontDirectory_ = "" ; // This directory must be empty

    exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

    exp.independentRuns_ = 20;

    exp.initExperiment();

    // Run the experiments
    int numberOfThreads;
    exp.runExperiment(numberOfThreads = 4);
    exp.generateQualityIndicators();

    // Generate latex tables
    exp.generateLatexTables();

    // Applying Friedman test
    Friedman test = new Friedman(exp);
    test.executeTest("HV");
    test.executeTest("IGD");
    test.executeTest("SPREAD");
    test.executeTest("EPSILON");
  } // main
} // ParametricConfiguration


