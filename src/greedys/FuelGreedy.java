package greedys;

import jmetal.problems.CVRPTW;

import java.util.*;

public class FuelGreedy extends Greedy {

    public FuelGreedy(String instanceName, int vehicleNumber, double capacity, int customerNumber, double[][] customers,
                          double[] demand, double[] readyTime, double[] dueDate, double[] service) {
    	super(instanceName, vehicleNumber, capacity, customerNumber, customers,
                demand, readyTime, dueDate, service, "fuel");
    }

    // Returns the greedy solution for the instance data in the attributes of this class
    public List<Integer> execute() {
        List<Integer> solution = new ArrayList<>();
        // uncoveredCustomers.get(i) is true iff customer i is not covered
        BitSet uncoveredCustomers = new BitSet(customerNumber_+1);
        for (int i = 0; i < customerNumber_; i++) {
            uncoveredCustomers.set(i+1);
        }
        // Last customer covered
        int prevCustomer = 0;
        // number of currently used vehicles
        int usedVehicles = 0;
        // Flag to know if we need a new vehicle
        boolean newVehicle;
        // Used capacity of the current vehicle
        double usedCapacity;
        // Time spent by current vehicle
        double usedTime;
        // Contains the mapping distance to customer -> customer index, ordered increasingly by key (distance)
        // The distance is relative to the current vehicle position. Need to calculate Map every time
        Map<Double, Set<Integer>> lastCustomers = new TreeMap<>();

        // Continue until all customers are covered
        while ( !uncoveredCustomers.isEmpty() ) {
            // Assign all posible customers to current vehicle
            newVehicle = false;
            usedVehicles++;
            usedCapacity = 0;
            usedTime = 0;
            prevCustomer = 0;
            while ( !newVehicle ) {
                // Calculate distance to left customers
                for (int i = uncoveredCustomers.nextSetBit(0); i > 0; i = uncoveredCustomers.nextSetBit(i+1)) {
                    // Check capacity constraint
                    if (usedCapacity + demand_[i] <= capacity_) {
                        // Check time window
                        if (usedTime <= dueDate_[i]) {
                            double demandedCapacity = demand_[i];
                            if (!lastCustomers.containsKey(demandedCapacity)) {
                                lastCustomers.put(demandedCapacity, new HashSet<>());
                            }
                            lastCustomers.get(demandedCapacity).add(i); // Add capacity demanded by customer i
                        }
                    }
                }
                // Get first customer in Map if not empty
                if (lastCustomers.isEmpty()) {
                    newVehicle = true;
                    if (usedVehicles > vehicleNumber_) {
                        return solution;
                    } else{
                        if ( !uncoveredCustomers.isEmpty() ) {
                            solution.add(0);
                        }
                    }
                } else {
                    // Last entry in the ordered map has a possible customer to visit next with the highest demand
                    prevCustomer = ((TreeMap<Double, Set<Integer>>) lastCustomers).lastEntry().getValue().iterator().next();
                    usedCapacity += demand_[prevCustomer];
                    if (usedTime < readyTime_[prevCustomer]) usedTime = readyTime_[prevCustomer];
                    usedTime += service_[prevCustomer];
                    uncoveredCustomers.clear(prevCustomer);
                    solution.add(prevCustomer);
                }
                lastCustomers.clear();
            }
        }
        return solution;
    }

    public static void main(String[] args) {

        String[] problemList1 = new String[56];
        for (int i = 0; i <= 8; i++) problemList1[i] = "C10" + (i+1);
        for (int i = 9; i <= 16; i++) problemList1[i] = "C20" + (i-8);
        for (int i = 17; i <= 25; i++) problemList1[i] = "R10" + (i-16);
        for (int i = 26; i <= 28; i++) problemList1[i] = "R1" + (i-16);
        for (int i = 29; i <= 37; i++) problemList1[i] = "R20" + (i-28);
        for (int i = 38; i <= 39; i++) problemList1[i] = "R2" + (i-28);
        for (int i = 40; i <= 47; i++) problemList1[i] = "RC10" + (i-39);
        for (int i = 48; i <= 55; i++) problemList1[i] = "RC20" + (i-47);

        for (int i=0; i<problemList1.length; i++) {
            CVRPTW p = new CVRPTW(problemList1[i]+".txt");
            FuelGreedy fg = new FuelGreedy(
                    "CVRPTW"+problemList1[i],
                    p.getVehicleNumber_(),
                    p.getCapacity_(),
                    p.getCustomerNumber_(),
                    p.getCustomers_(),
                    p.getDemand_(),
                    p.getReadyTime_(),
                    p.getDueDate_(),
                    p.getService_());
            List<Integer> sol = fg.execute();
//            sol.forEach(elem -> {
//                System.out.print(elem + " ");
//            });
//            System.out.println();
            if ( !fg.isValid(sol) ) {
                System.out.println("Fuel Greedy, "+problemList1[i]+" no factible");
            };
        }

        String[] problemList2 = new String[30];
        for (int i = 0; i <= 9; i++) problemList2[i] = "C110_" + (i+1);
        for (int i = 10; i <= 19; i++) problemList2[i] = "R110_" + (i-9);
        for (int i = 20; i <= 29; i++) problemList2[i] = "RC110_" + (i-19);

        for (int i=0; i<problemList2.length; i++) {
            CVRPTW p = new CVRPTW(problemList2[i]+".txt");
            FuelGreedy fg = new FuelGreedy(
                    "CVRPTW"+problemList2[i],
                    p.getVehicleNumber_(),
                    p.getCapacity_(),
                    p.getCustomerNumber_(),
                    p.getCustomers_(),
                    p.getDemand_(),
                    p.getReadyTime_(),
                    p.getDueDate_(),
                    p.getService_());
            List<Integer> sol = fg.execute();
//            sol.forEach(elem -> {
//                System.out.print(elem + " ");
//            });
//            System.out.println();
            if ( !fg.isValid(sol) ) {
                System.out.println("Fuel Greedy, "+problemList2[i]+" no factible");
            };
        }
    }
}
