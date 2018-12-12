//  PMXCrossoverArrayInt.java
//
//  Author:
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

package jmetal.operators.crossover;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.ArrayIntSolutionType;
import jmetal.encodings.variable.ArrayInt;
import jmetal.problems.CVRPTW;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.wrapper.XInt;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

/**
 * This class allows to apply a PMX crossover operator using two parent
 * solutions.
 * NOTE: the operator is applied to the first encodings.variable of the solutions, and
 * the type of those variables must be VariableType_.ArrayInt.
 */
public class PMXCrossoverArrayInt extends Crossover {

  /**
   * Valid solution types to apply this operator
   */
  private static final List VALID_TYPES = Arrays.asList(ArrayIntSolutionType.class);

  private Double crossoverProbability_ = null;

  /**
   * Constructor
   */
  public PMXCrossoverArrayInt(HashMap<String, Object> parameters) {
    super(parameters);

    if (parameters.get("probability") != null)
      crossoverProbability_ = (Double) parameters.get("probability");
  } // PMXCrossoverArrayInt

  /**
   * Perform the crossover operation
   *
   * @param probability Crossover probability
   * @param parent1     The first parent
   * @param parent2     The second parent
   * @return An array containig the two offsprings
   * @throws JMException
   */
  public Solution[] doCrossover(double probability,
                                Solution parent1,
                                Solution parent2) throws JMException {

    int zerosOffspring1 = 0;
    int zerosOffspring2 = 0;

    Solution[] offspring = new Solution[2];

    offspring[0] = new Solution(parent1);
    offspring[1] = new Solution(parent2);

    Variable offspring1 = offspring[0].getDecisionVariables()[0];
    Variable offspring2 = offspring[1].getDecisionVariables()[0];

    int arrayLength = ((ArrayInt) parent1.getDecisionVariables()[0]).getLength();

    XInt parent1Array = new XInt(parent1);
    XInt parent2Array = new XInt(parent2);

    if (PseudoRandom.randDouble() < probability) {
      int cuttingPoint1;
      int cuttingPoint2;

      //      STEP 1: Get two cutting points
      cuttingPoint1 = PseudoRandom.randInt(0, arrayLength - 1);
      cuttingPoint2 = PseudoRandom.randInt(0, arrayLength - 1);
      while (cuttingPoint2 == cuttingPoint1)
        cuttingPoint2 = PseudoRandom.randInt(0, arrayLength - 1);

      if (cuttingPoint1 > cuttingPoint2) {
        int swap;
        swap = cuttingPoint1;
        cuttingPoint1 = cuttingPoint2;
        cuttingPoint2 = swap;
      } // if

      //      STEP 2: Get the subchains to interchange
      int replacementLength;
      if (parent1.getProblem().getName().equals("CVRPTW")) {
        replacementLength = ((CVRPTW)parent1.getProblem()).getCustomerNumber_() + 1;
      } else {
        Configuration.logger_.severe("PMXCrossoverArrayInt.doCrossover: the problem selected "
                + " is not allowed with this operator. Problem must be CVRPTW");
        Class cls = java.lang.String.class;
        String name = cls.getName();
        throw new JMException("Exception in " + name + ".doCrossover()");
      }
      // For mapping between non zero entries
      int replacement1[] = new int[replacementLength];
      int replacement2[] = new int[replacementLength];
      for (int i = 0; i < replacementLength; i++) {
        replacement1[i] = replacement2[i] = -1;
      }
      // For mapping from zeros (initially all false)
      BitSet recoverNonZero1 = new BitSet(replacementLength);
      BitSet recoverNonZero2 = new BitSet(replacementLength);

      //      STEP 3: Interchange
      for (int i = cuttingPoint1; i <= cuttingPoint2; i++) {
        ((ArrayInt)offspring1).setValue(i, parent2Array.getValue(i));
        ((ArrayInt)offspring2).setValue(i, parent1Array.getValue(i));

        // Save mappings
        if (parent1Array.getValue(i) != 0 && parent2Array.getValue(i) != 0 &&
                parent1Array.getValue(i) != parent2Array.getValue(i)) {
          replacement1[parent2Array.getValue(i)] = parent1Array.getValue(i);
          replacement2[parent1Array.getValue(i)] = parent2Array.getValue(i);
        } else if (parent1Array.getValue(i) != 0 && parent2Array.getValue(i) == 0) {
            recoverNonZero1.set(parent1Array.getValue(i));
            replacement2[parent1Array.getValue(i)] = 0;
        } else if (parent1Array.getValue(i) == 0 && parent2Array.getValue(i) != 0) {
          replacement1[parent2Array.getValue(i)] = 0;
          recoverNonZero2.set(parent2Array.getValue(i));
        }
      } // for
      // Correct the replacement mappings
      for (int i = recoverNonZero1.nextSetBit(1); i >= 1; i = recoverNonZero1.nextSetBit(i+1)) {
        if (replacement1[i] == 0) {
          replacement1[i] = -1;
          recoverNonZero1.clear(i);
        }
      } // for
      for (int i = recoverNonZero2.nextSetBit(1); i >= 1; i = recoverNonZero2.nextSetBit(i+1)) {
        if (replacement2[i] == 0) {
          replacement2[i] = -1;
          recoverNonZero2.clear(i);
        }
      } // for
      // Correct the recoverNonZero mappings
      for (int i = recoverNonZero1.nextSetBit(1); i >= 1; i = recoverNonZero1.nextSetBit(i+1)) {
        int n = i;
        int m = replacement1[n];
        while (m != -1) {
          n = m;
          m = replacement1[n];
        }
        if (n != i) {
          recoverNonZero1.set(n);
          recoverNonZero1.clear(i);
        }
      } // for
      for (int i = recoverNonZero2.nextSetBit(1); i >= 1; i = recoverNonZero2.nextSetBit(i+1)) {
        int n = i;
        int m = replacement2[n];
        while (m != -1) {
          n = m;
          m = replacement2[n];
        }
        if (n != i) {
          recoverNonZero2.set(n);
          recoverNonZero2.clear(i);
        }
      } // for

      //      STEP 4: Repair offsprings
      // Apply mapping between non zeros
      for (int i = 0; i < arrayLength; i++) {
        if ((i >= cuttingPoint1) && (i <= cuttingPoint2))
          continue;

        // Repair offspring[0]
        int n1 = parent1Array.getValue(i);
        int m1 = replacement1[n1];

        while (m1 != -1) {
          n1 = m1;
          m1 = replacement1[m1];
        } // while
        ((ArrayInt)offspring1).setValue(i, n1);

        // Repair offspring[1]
        int n2 = parent2Array.getValue(i);
        int m2 = replacement2[n2];

        while (m2 != -1) {
          n2 = m2;
          m2 = replacement2[m2];
        } // while
        ((ArrayInt)offspring2).setValue(i, n2);
      }

      // Recover non zeros lost in a cross between a zero and a non zero
      for (int i = 0; i < arrayLength; i++) {
        if ((i >= cuttingPoint1) && (i <= cuttingPoint2))
          continue;

        // Repair offspring[0]
        if (((ArrayInt)offspring1).getValue(i) == 0) {
          int z1 = recoverNonZero1.nextSetBit(((ArrayInt) offspring1).getValue(i));
          if (z1 != -1) {
            recoverNonZero1.clear(z1);
            ((ArrayInt) offspring1).setValue(i, z1);
          }
        }
        // Repair offspring[1]
        if (((ArrayInt)offspring2).getValue(i) == 0) {
          int z2 = recoverNonZero2.nextSetBit(((ArrayInt) offspring2).getValue(i));
          if (z2 != -1) {
            recoverNonZero2.clear(z2);
            ((ArrayInt) offspring2).setValue(i, z2);
          }
        }
      }
    } // if

    // Check number of separators (only to prevent crossover from returning offspring with wrong number of separators)
    for (int i = 0; i < arrayLength; i++) {
      if (((ArrayInt) offspring1).getValue(i) == 0) zerosOffspring1++;
      if (((ArrayInt) offspring2).getValue(i) == 0) zerosOffspring2++;
    }
    if (zerosOffspring1 != ( ((CVRPTW)parent1.getProblem()).getVehicleNumber_() - 1) ||
            zerosOffspring2 != ( ((CVRPTW)parent1.getProblem()).getVehicleNumber_() - 1)) {
      offspring[0] = parent1;
      offspring[1] = parent2;
    }

    return offspring;
  } // doCrossover

  /**
   * Executes the operation
   *
   * @param object An object containing an array of two solutions
   * @throws JMException
   */
  public Object execute(Object object) throws JMException {
    Solution[] parents = (Solution[]) object;
    Double crossoverProbability = null;

    if (!(VALID_TYPES.contains(parents[0].getType().getClass()) &&
            VALID_TYPES.contains(parents[1].getType().getClass()))) {

      Configuration.logger_.severe("PMXCrossoverArrayInt.execute: the solutions " +
              "are not of the right type. The type should be 'ArrayInt', but " +
              parents[0].getType() + " and " +
              parents[1].getType() + " are obtained");
    }

    //crossoverProbability = (Double)parameters_.get("probability");
    crossoverProbability = (Double) getParameter("probability");

    if (parents.length < 2) {
      Configuration.logger_.severe("PMXCrossoverArrayInt.execute: operator needs two " +
              "parents");
      Class cls = String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    }

    Solution[] offspring = doCrossover(crossoverProbability.doubleValue(),
            parents[0],
            parents[1]);

    return offspring;
  } // execute

  // For debugging
  public static void main(String[] args) throws ClassNotFoundException, JMException {
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("probability", 1.0);
    PMXCrossoverArrayInt crossover = new PMXCrossoverArrayInt(parameters);
    Problem problem = new CVRPTW("C102.txt");
    Solution parent1 = new Solution(problem);
    Solution parent2 = new Solution(problem);
    int customers = ((CVRPTW)parent1.getProblem()).getCustomerNumber_();
    int arrayLength;
    arrayLength = ((ArrayInt) parent1.getDecisionVariables()[0]).getLength();

    for (int h = 0; h < 1000; h++) {
      // Print parents before crossover
      System.out.print("Parent 1    = ");
      int zerosParent1 = 0;
      for (int i = 0; i < arrayLength; i++) {
        if (((ArrayInt)parent1.getDecisionVariables()[0]).getValue(i) == 0) zerosParent1++;
        System.out.print(" ");
        if (((ArrayInt)parent1.getDecisionVariables()[0]).getValue(i) < 10) System.out.print(" ");
        System.out.print(((ArrayInt)parent1.getDecisionVariables()[0]).getValue(i));
      }
      System.out.println("  Zeros Parent 1    = " + zerosParent1);

      System.out.print("Parent 2    = ");
      int zerosParent2 = 0;
      for (int i = 0; i < arrayLength; i++) {
        if (((ArrayInt)parent2.getDecisionVariables()[0]).getValue(i) == 0) zerosParent2++;
        System.out.print(" ");
        if (((ArrayInt)parent2.getDecisionVariables()[0]).getValue(i) < 10) System.out.print(" ");
        System.out.print(((ArrayInt)parent2.getDecisionVariables()[0]).getValue(i));
      }
      System.out.println("  Zeros Parent 2    = " + zerosParent2);

      // Execute crossover
      Solution[] offspring = crossover.doCrossover(1.0, parent1, parent2);

      // Print offspring of crossover
      System.out.print("Offspring 1 = ");
      int zerosOffspring1 = 0;
      for (int i = 0; i < arrayLength; i++) {
        if (((ArrayInt)offspring[0].getDecisionVariables()[0]).getValue(i) == 0) zerosOffspring1++;
        System.out.print(" ");
        if (((ArrayInt)offspring[0].getDecisionVariables()[0]).getValue(i) < 10) System.out.print(" ");
        System.out.print(((ArrayInt)offspring[0].getDecisionVariables()[0]).getValue(i));
      }
      System.out.print("  Zeros Offspring 1 = " + zerosOffspring1 + "  ");

      // Check for duplicates in the non zero entries
      System.out.print("Duplicates Offspring 1 = ");
      for (int j=0; j < arrayLength; j++) {
        for (int k = j + 1; k < arrayLength; k++) {
          if (k != j &&
                  (((ArrayInt) offspring[0].getDecisionVariables()[0]).getValue(j) ==
                          ((ArrayInt) offspring[0].getDecisionVariables()[0]).getValue(k) &&
                          ((ArrayInt) offspring[0].getDecisionVariables()[0]).getValue(j) != 0))
            System.out.print(((ArrayInt) offspring[0].getDecisionVariables()[0]).getValue(k) + " ");
        }
      }

      // Check missing elemens
      System.out.print("Missings Offspring 1 = ");
      boolean found = false;
      for (int j = 1; j <= customers; j++) {
        for (int k = 0; k < arrayLength; k++) {
          if (((ArrayInt) offspring[0].getDecisionVariables()[0]).getValue(k) == j) found = true;
        }
        if (!found) System.out.print(j + " ");
        found = false;
      }
      System.out.println("");

      System.out.print("Offspring 2 = ");
      int zerosOffspring2 = 0;
      for (int i = 0; i < arrayLength; i++) {
        if (((ArrayInt)offspring[1].getDecisionVariables()[0]).getValue(i) == 0) zerosOffspring2++;
        System.out.print(" ");
        if (((ArrayInt)offspring[1].getDecisionVariables()[0]).getValue(i) < 10) System.out.print(" ");
        System.out.print(((ArrayInt)offspring[1].getDecisionVariables()[0]).getValue(i));
      }
      System.out.print("  Zeros Offspring 2 = " + zerosOffspring2 + "  ");

      // Check for duplicates in the non zero entries
      System.out.print("Duplicates Offspring 2 = ");
      for (int j=0; j < arrayLength; j++) {
        for (int k = j + 1; k < arrayLength; k++) {
          if (k != j &&
                  ((ArrayInt) offspring[1].getDecisionVariables()[0]).getValue(j) ==
                          ((ArrayInt) offspring[1].getDecisionVariables()[0]).getValue(k) &&
                  ((ArrayInt) offspring[1].getDecisionVariables()[0]).getValue(j) != 0)
            System.out.print(((ArrayInt) offspring[1].getDecisionVariables()[0]).getValue(k) + " ");
        }
      }

      // Check missing elemens
      System.out.print("Missings Offspring 2 = ");
      found = false;
      for (int j = 1; j <= customers; j++) {
        for (int k = 0; k < arrayLength; k++) {
          if (((ArrayInt) offspring[1].getDecisionVariables()[0]).getValue(k) == j) found = true;
        }
        if (!found) System.out.print(j + " ");
        found = false;
      }
      System.out.println("");

      if (zerosOffspring1 > 0 || zerosOffspring2 > 0) continue;
      for (int l = 0; l < arrayLength; l++) {
        ((ArrayInt) parent1.getDecisionVariables()[0]).setValue(l, ((ArrayInt) offspring[0].getDecisionVariables()[0]).getValue(l));
        ((ArrayInt) parent2.getDecisionVariables()[0]).setValue(l, ((ArrayInt) offspring[1].getDecisionVariables()[0]).getValue(l));
      }
    }
  } // PMXCrossoverArrayInt
}
