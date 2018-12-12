//  CVRPTW.java
//
//  Author:
//       Nicolás Rodríguez <marco.nicolas.rodriguez@fing.edu.uy>
//       Ignacio Ferreira <ignacio.ferreira@fing.edu.uy>
//
//  Copyright (c) 2018 Nicolás Rodríguez, Ignacio Ferreira
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

package jmetal.problems;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayIntSolutionType;
import jmetal.encodings.variable.ArrayInt;
import jmetal.util.JMException;
import jmetal.util.wrapper.XInt;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
/**
 * Class representing problem CVRPTW
 */
public class CVRPTW extends Problem {
    private Integer vehicleNumber_;
    private double capacity_;
    private Integer customerNumber_;
    private double [][] customers_;     // customer i has coordinates (customers[0][i], customers[1][i])
    private double [] demand_;
    private double [] readyTime_;
    private double [] dueDate_;
    private double [] service_;
    private String instanceName_;
    private String instanceFile_;
    private String outputDirectory_;

    public Integer getVehicleNumber_() {
        return vehicleNumber_;
    }

    public double getCapacity_() {
        return capacity_;
    }

    public Integer getCustomerNumber_() {
        return customerNumber_;
    }

    public double[][] getCustomers_() {
        return customers_;
    }

    public double[] getDemand_() {
        return demand_;
    }

    public double[] getReadyTime_() {
        return readyTime_;
    }

    public double[] getDueDate_() {
        return dueDate_;
    }

    public double[] getService_() {
        return service_;
    }

    /**
  * Creates a new instance of problem CVRPTW.
  * @param instanceName Name of the instance. Must be in jMetal/data/CVRPTW
  */
  public CVRPTW(String instanceName) {

    outputDirectory_ = "/home/nico/IdeaProjects/jMetal/src/jmetal/data/CVRPTW/output";

    // Parse instance files and assign private attributes
    readInstanceFile(instanceName);

    numberOfVariables_  = customerNumber_ + (vehicleNumber_ - 1);
    numberOfObjectives_ =  2;
    numberOfConstraints_=  2;
    problemName_        = "CVRPTW";

    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];

    // Establishes upper and lower limits for the variables
    for (int var = 0; var < numberOfVariables_; var++)
    {
        lowerLimit_[var] = 0;                   // 0 represents the vehicle separator
        upperLimit_[var] = customerNumber_;
    } // for

    solutionType_ = new ArrayIntSolutionType(this);

  } // CVRPTW

    /**
    * Evaluates a solution.
    * @param solution The solution to evaluate.
    * @throws JMException for using the methods of XInt
    */
    public void evaluate(Solution solution) throws JMException {
        XInt x = new XInt(solution);

        double [] f = new double[numberOfObjectives_];
        f[0]        = 0.0;  // Total distance
        f[1]        = 0.0;  // Fuel consumption

        // Evaluate total distance
        int i = -1;
        boolean newVehicle;         // Indicates whether we are processing a new vehicle
        int vehicleNumber = -1;     // Number of used vehicles - 1

        // Evaluate global fuel consumption
        double[] capacityVehicle = new double[vehicleNumber_];
        for (int j = 0; j < capacityVehicle.length; j++) capacityVehicle[j] = 0.0;

        // Evaluate de total distance and calculate the demand for each vehicle
        while (i < x.getNumberOfDecisionVariables()) {
            // Skip the contiguous zeros (unused vehicles)
            do {
                i++;
            } while (i < x.getNumberOfDecisionVariables() && x.getValue(i) == 0);

            if (i == x.getNumberOfDecisionVariables()) continue;

            newVehicle = true;
            vehicleNumber++;
            do {
                if (newVehicle) {
                    // Distance from depot to first customer
                    f[0] += Math.sqrt(Math.pow(customers_[0][0] - customers_[0][x.getValue(i)], 2) +
                            Math.pow(customers_[1][0] - customers_[1][x.getValue(i)], 2));
                    newVehicle = false;
                }
                // Add capacity demanded by current customer
                try {
                    capacityVehicle[vehicleNumber] += demand_[x.getValue(i)];
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    int zeros = 0;
                    for (int k = 0; k < ((ArrayInt)solution.getDecisionVariables()[0]).array_.length; k++) {
                        System.out.print(((ArrayInt)solution.getDecisionVariables()[0]).array_[k] + " ");
                        if (((ArrayInt)solution.getDecisionVariables()[0]).array_[k] == 0) zeros++;
                    }
                    System.out.println();
                    System.out.println("Zeros: " + zeros);
                }

                // If there is next customer, add the distance to him
                if (i+1 < x.getNumberOfDecisionVariables() && x.getValue(i+1) != 0) {
                    f[0] += Math.sqrt(Math.pow(customers_[0][x.getValue(i)] - customers_[0][x.getValue(i+1)], 2) +
                            Math.pow(customers_[1][x.getValue(i)] - customers_[1][x.getValue(i+1)], 2));
                }

                i++;
            } while (i < x.getNumberOfDecisionVariables() && x.getValue(i) != 0);

            // Add the distance from the last customer back to the depot
            f[0] += Math.sqrt(Math.pow(customers_[0][x.getValue(i-1)] - customers_[0][0], 2) +
                    Math.pow(customers_[1][x.getValue(i-1)] - customers_[1][0], 2));
            // Add capacity demanded by last customer
            capacityVehicle[vehicleNumber] += demand_[x.getValue(i-1)];
        }

        // Evaluate the global fuel consumption based on the demand for each vehicle calculated before
        i = -1;
        vehicleNumber = -1;
        while (i < x.getNumberOfDecisionVariables()) {
            // Skip the contiguous zeros (unused vehicles)
            do {
                i++;
            } while (i < x.getNumberOfDecisionVariables() && x.getValue(i) == 0);

            if (i == x.getNumberOfDecisionVariables()) continue;

            newVehicle = true;
            vehicleNumber++;
            do {
                if (newVehicle) {
                    // Fuel consumption from depot to first customer
                    f[1] += (1 + capacityVehicle[vehicleNumber]/capacity_) *
                            Math.sqrt(Math.pow(customers_[0][0] - customers_[0][x.getValue(i)], 2) +
                            Math.pow(customers_[1][0] - customers_[1][x.getValue(i)], 2));
                    newVehicle = false;
                }

                // If there is next customer, add fuel consumption to him
                if (i+1 < x.getNumberOfDecisionVariables() && x.getValue(i+1) != 0) {
                    capacityVehicle[vehicleNumber] -= demand_[x.getValue(i)]; // Leave demanded capacity on customer
                    f[1] += (1 + capacityVehicle[vehicleNumber]/capacity_) *
                            Math.sqrt(Math.pow(customers_[0][x.getValue(i)] - customers_[0][x.getValue(i+1)], 2) +
                            Math.pow(customers_[1][x.getValue(i)] - customers_[1][x.getValue(i+1)], 2));
                }

                i++;
            } while (i < x.getNumberOfDecisionVariables() && x.getValue(i) != 0);

            // Add fuel consumption from the last customer back to the depot
            capacityVehicle[vehicleNumber] -= demand_[x.getValue(i-1)]; // Leave demanded capacity on customer
            assert capacityVehicle[vehicleNumber] == 0;
            f[1] += (1 + capacityVehicle[vehicleNumber]/capacity_) *
                    Math.sqrt(Math.pow(customers_[0][x.getValue(i-1)] - customers_[0][0], 2) +
                    Math.pow(customers_[1][x.getValue(i-1)] - customers_[1][0], 2));
        }

        solution.setObjective(0, f[0]);
        solution.setObjective(1, f[1]);
    } // evaluate

    /**
     * Evaluates the constraint overhead of a solution
     * @param solution The solution
     * @throws JMException for using the methods of XInt
     */
    public void evaluateConstraints(Solution solution) throws JMException {
        double [] constraint = new double[this.getNumberOfConstraints()];
        constraint[0] = constraint[1] = 0.0;

        XInt x = new XInt(solution);

        // Evaluate used capacity and check time windows at the same time
        int i = -1;
        double usedCapacity;    // Capacity used in the current vehicle
        double timeSpent;       // Time spent by the current vehicle

        while (i < x.getNumberOfDecisionVariables()) {
            // Skip the contiguous zeros (unused vehicles)
            do {
                i++;
            } while (i < x.getNumberOfDecisionVariables() && x.getValue(i) == 0);

            if (i == x.getNumberOfDecisionVariables()) continue;

            usedCapacity = 0.0;
            timeSpent = 0.0;

            do {
                // If arrived before time window begin, wait
                if (timeSpent < readyTime_[x.getValue(i)]) {
                    timeSpent = readyTime_[x.getValue(i)];
                }
                // Check if we are inside the time window
                if (timeSpent > dueDate_[x.getValue(i)]) {
                    constraint[0] += -(timeSpent - dueDate_[x.getValue(i)]);
                }
                // Add service time for current customer
                timeSpent += service_[x.getValue(i)];
                // Accumulate used capacity
                usedCapacity += demand_[x.getValue(i)];

                i++;
            } while (i < x.getNumberOfDecisionVariables() && x.getValue(i) != 0);

            // Check if we make it back to the depot on time
            if (timeSpent > dueDate_[0]) constraint[0] += -(timeSpent - dueDate_[0]);

            // Check if we didn't exceed the capacity
            if (usedCapacity > capacity_) constraint[1] += -(usedCapacity - capacity_);
        } // while

        double total = 0.0;
        int number = 0;
        for (int j = 0; j < this.getNumberOfConstraints(); j++) {
            if (constraint[j] < 0.0) {
                total += constraint[j];
                number++;
            }
        }

        solution.setOverallConstraintViolation(total);
        solution.setNumberOfViolatedConstraint(number);
    } // evaluateConstraints

    /**
     * Evaluates if the solution is feasible
     * @param solution The solution
     * @return True iff solution is a feasible solution
     */
    public boolean isFeasible(List<Integer> solution) {
        // Evaluate used capacity and check time windows at the same time
        int i = -1;
        double usedCapacity;    // Capacity used in the current vehicle
        double timeSpent;       // Time spent by the current vehicle

        while (i < solution.size()) {
            // Skip the contiguous zeros (unused vehicles)
            do {
                i++;
            } while (i < solution.size() && solution.get(i) == 0);

            if (i >= solution.size()) continue;

            usedCapacity = 0.0;
            timeSpent = 0.0;

            do {
                // If arrived before time window begin, wait
                if (timeSpent < readyTime_[solution.get(i)]) timeSpent = readyTime_[solution.get(i)];
                // Check if we are inside the time window
                if (timeSpent > dueDate_[solution.get(i)]) return false;
                // Add service time for current customer
                timeSpent += service_[solution.get(i)];
                // Accumulate used capacity
                usedCapacity += demand_[solution.get(i)];

                i++;
            } while (i < solution.size() && solution.get(i) != 0);

            // Check if we make it back to the depot on time
            if (timeSpent > dueDate_[0]) return false;

            // Check if we didn't exceed the capacity
            if (usedCapacity > capacity_) return false;
        } // while

        return true;
    } // isFeasible

    /**
     * Reads the instance file and get the information of the problem variables
     * Sets the class atributes corresponding to the problem variables
     * @param instanceName Name of the instance file. It must be one of the instances in jMetal/data/CVRPTW folder
     */
    private void readInstanceFile(String instanceName) {

        String instanceFileDirectory;               // Directory with instance files
        String instanceFile             = "";       // Route to the instance file
        Integer vehicleNumber           = -1;       // Number of vehicles
        double capacity                 = 0.0;      // Capacity of the vehicles
        Integer customerNumber          = -1;       // Number of customers. Depot doesn't count
        double [][] customers           = null;     // Coordinates of customers. It includes depot
        double [] demand                = null;     // Demand of each customer. Zero for depot
        double [] readyTime             = null;     // Beginning of each customer's time window. It includes depot
        double [] dueDate               = null;     // End of each customer's time window. It includes depot
        double [] service               = null;     // Time spent serving each customer. Zero for depot
        String outputDirectory          = "";       // Route to output directory

        try {
            // Set instance files directory
            instanceFileDirectory = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/";
            instanceFile = instanceFileDirectory + instanceName;

            File fin = new File(instanceFile);
            FileInputStream fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            // First, count the file's number of lines to get the number of customers
            String line = br.readLine();
            int numberOfLines = 0;
            while (line != null) {
                line = br.readLine();
                numberOfLines = numberOfLines + 1;
            }
            customerNumber = numberOfLines - 10;
            br.close();

            // Second, read again the file to get the remaining data
            Scanner scanner = new Scanner(new File(instanceFile));
            // Skip the first four lines
            for (int i = 0; i < 4; i++) scanner.nextLine();

            // Get the vehicle number and capacity
            vehicleNumber = scanner.nextInt();
            capacity = scanner.nextInt();

            // Allocate memory for the arrays
            customers   = new double[2][customerNumber + 1];
            demand      = new double[customerNumber + 1];
            readyTime   = new double[customerNumber + 1];
            dueDate     = new double[customerNumber + 1];
            service     = new double[customerNumber + 1];

            // Get the customers' location, their demands, ready time, due date and service

            // Skip the next four lines
            for (int i = 0; i < 4; i++) scanner.nextLine();

            for (int i = 0; i < customerNumber + 1; i++) {
                // Go to next line
                scanner.nextLine();

                // Skip the first integer of the line, which is the customer number
                scanner.nextInt();

                // Read customer's coordinates
                customers[0][i] = scanner.nextInt();
                customers[1][i] = scanner.nextInt();

                // Read demand
                demand[i] = scanner.nextInt();

                // Read time window
                readyTime[i] = scanner.nextInt();
                dueDate[i] = scanner.nextInt();

                // Read service
                service[i] = scanner.nextInt();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Set attributes of the problem
        vehicleNumber_      = vehicleNumber;
        capacity_           = capacity;
        customerNumber_     = customerNumber;
        customers_          = customers;
        demand_             = demand;
        readyTime_          = readyTime;
        dueDate_            = dueDate;
        service_            = service;
        instanceName_       = instanceName;
        instanceFile_       = instanceFile;
        outputDirectory_    = outputDirectory;
    } // readInstanceFile

    // For debugging
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String s = br.readLine();
            CVRPTW p = new CVRPTW(s);
            p.readInstanceFile(s);

            System.out.println("Vehicle number: " + p.vehicleNumber_);
            System.out.println("Capacity: " + p.capacity_);
            System.out.println("Customer number: " + p.customerNumber_);
            System.out.println("Customers: " + Arrays.deepToString(p.customers_));
            System.out.println("Demand: " + Arrays.toString(p.demand_));
            System.out.println("Ready Time: " + Arrays.toString(p.readyTime_));
            System.out.println("Due Date: " + Arrays.toString(p.dueDate_));
            System.out.println("Service: " + Arrays.toString(p.service_));
            System.out.println("Intance name: " + p.instanceName_);
            System.out.println("Instance file: " + p.instanceFile_);
            System.out.println("Output directory: " + p.outputDirectory_);

            Solution solution = new Solution(p);
            p.evaluate(solution);
            System.out.print("Solution: ");
            for (int i = 0; i < ((ArrayInt)solution.getDecisionVariables()[0]).getLength(); i++) {
                System.out.print(" " + ((ArrayInt)solution.getDecisionVariables()[0]).getValue(i));
            }
            System.out.println();
            System.out.println(("Total distance=" + solution.getObjective(0)));
            System.out.println("Fuel consumption=" + solution.getObjective(1));
            solution.getProblem().evaluateConstraints(solution);
            System.out.println("Constraint evaluation=" + solution.getOverallConstraintViolation());
            System.out.println("Constraints violated=" + solution.getNumberOfViolatedConstraint());
            List<Integer> arrayList = new ArrayList<>();
            for (int i = 0; i < ((ArrayInt)solution.getDecisionVariables()[0]).array_.length; i++) {
                arrayList.add(i, ((ArrayInt)solution.getDecisionVariables()[0]).array_[i]);
            }
            System.out.println("Is feasible: " + p.isFeasible(arrayList));

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // main

} // CVRPTW
