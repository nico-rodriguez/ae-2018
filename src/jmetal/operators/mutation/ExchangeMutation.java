//  ExchangeMutation.java
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

package jmetal.operators.mutation;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayIntSolutionType;
import jmetal.encodings.variable.ArrayInt;
import jmetal.problems.CVRPTW;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.wrapper.XInt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements exchange (swap) mutation. The solution type of the solution
 * must be ArrayInt.
 */
public class ExchangeMutation extends Mutation{
  /**
   * Valid solution types to apply this operator
   */
  private static final List VALID_TYPES = Arrays.asList(ArrayIntSolutionType.class) ;

  private Double mutationProbability_ = null ;

  /**
   * Constructor
   */
  public ExchangeMutation(HashMap<String, Object> parameters) {
  	super(parameters) ;

  	if (parameters.get("probability") != null)
  		mutationProbability_ = (Double) parameters.get("probability") ;
  } // Constructor


  /**
   * Constructor
   */
  //public ExchangeMutation(Properties properties) {
  //  this();
  //} // Constructor

  /**
   * Performs the operation
   * @param probability Mutation probability
   * @param solution The solution to mutate
   * @throws JMException
   */
  public void doMutation(double probability, Solution solution) throws JMException {
    int array[] ;
    int arrayLength ;
	    if (solution.getType().getClass() == ArrayIntSolutionType.class) {

	      arrayLength = ((ArrayInt)solution.getDecisionVariables()[0]).getLength() ;
	      array = ((ArrayInt)solution.getDecisionVariables()[0]).array_ ;

	      if (PseudoRandom.randDouble() < probability) {
	        int pos1 ;
	        int pos2 ;

	        pos1 = PseudoRandom.randInt(0,arrayLength-1) ;
	        pos2 = PseudoRandom.randInt(0,arrayLength-1) ;

	        while (pos1 == pos2) {
	          if (pos1 == (arrayLength - 1))
	            pos2 = PseudoRandom.randInt(0, arrayLength- 2);
	          else
	            pos2 = PseudoRandom.randInt(pos1, arrayLength- 1);
	        } // while
	        // swap
	        int temp = array[pos1];
	        array[pos1] = array[pos2];
	        array[pos2] = temp;
	      } // if
	    } // if
	    else  {
	      Configuration.logger_.severe("ExchangeMutation.doMutation: invalid type. " +
	          ""+ solution.getDecisionVariables()[0].getVariableType());

	      Class cls = String.class;
	      String name = cls.getName();
	      throw new JMException("Exception in " + name + ".doMutation()") ;
	    }
  } // doMutation

  /**
   * Executes the operation
   * @param object An object containing the solution to mutate
   * @return an object containing the mutated solution
   * @throws JMException
   */
  public Object execute(Object object) throws JMException {
    Solution solution = (Solution)object;

		if (!VALID_TYPES.contains(solution.getType().getClass())) {
			Configuration.logger_.severe("ExchangeMutation.execute: the solution " +
					"is not of the right type. The type should be 'Int', but " + solution.getType() + " is obtained");

			Class cls = String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if 

    
    this.doMutation(mutationProbability_, solution);
    return solution;
  } // execute

	// For debugging
	public static void main(String[] args) throws ClassNotFoundException, JMException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("probability", 0.9);
		ExchangeMutation mutation = new ExchangeMutation(parameters);
		Problem problem = new CVRPTW("C102.txt");
		Solution solution = new Solution(problem);
		int arrayLength;
		arrayLength = ((ArrayInt) solution.getDecisionVariables()[0]).getLength();

		// Print soluttion before mutation
		System.out.print("Solution         = ");
		int zerosSolution = 0;
		for (int i = 0; i < arrayLength; i++) {
			if (((ArrayInt)solution.getDecisionVariables()[0]).getValue(i) == 0) zerosSolution++;
			System.out.print(" ");
			if (((ArrayInt)solution.getDecisionVariables()[0]).getValue(i) < 10) System.out.print(" ");
			System.out.print(((ArrayInt)solution.getDecisionVariables()[0]).getValue(i));
		}
		System.out.println("  Zeros solution         = " + zerosSolution);

		// Execute mutation
		mutation.doMutation(1.0, solution);

		// Print mutated solution
		System.out.print("Mutated solution = ");
		zerosSolution = 0;
		for (int i = 0; i < arrayLength; i++) {
			if (((ArrayInt)solution.getDecisionVariables()[0]).getValue(i) == 0) zerosSolution++;
			System.out.print(" ");
			if (((ArrayInt)solution.getDecisionVariables()[0]).getValue(i) < 10) System.out.print(" ");
			System.out.print(((ArrayInt)solution.getDecisionVariables()[0]).getValue(i));
		}
		System.out.print("  Zeros Mutated Solution = " + zerosSolution + "  ");

		// Check for duplicates in the non zero entries
		boolean duplicates = false;
		for (int j=0; j < arrayLength; j++) {
			for (int k = j + 1; k < arrayLength; k++) {
				if (k != j &&
						(((ArrayInt) solution.getDecisionVariables()[0]).getValue(j) ==
								((ArrayInt) solution.getDecisionVariables()[0]).getValue(k) &&
								((ArrayInt) solution.getDecisionVariables()[0]).getValue(j) != 0))
					duplicates = true;
			}
		}
		System.out.println("Duplicates Mutated Solution = " + duplicates);
	}
} // ExchangeMutation
