package greedys;

import jmetal.util.Configuration;

import java.io.*;
import java.util.List;

public class Greedy {
    protected String instanceName_;
    protected Integer vehicleNumber_;
    protected double capacity_;
    protected Integer customerNumber_;
    protected  double[][] customers_;
    protected double [] demand_;
    protected double [] readyTime_;
    protected double [] dueDate_;
    protected double [] service_;
    protected String type_;     // Type of greedy: distance or fuel

    public Greedy(String instanceName, int vehicleNumber, double capacity, int customerNumber, double[][] customers,
                          double[] demand, double[] readyTime, double[] dueDate, double[] service, String type) {
        instanceName_ = instanceName;
        vehicleNumber_ = vehicleNumber;
        capacity_ = capacity;
        customerNumber_ = customerNumber;
        customers_ = customers;
        demand_ = demand;
        readyTime_ = readyTime;
        dueDate_ = dueDate;
        service_ = service;
        type_ = type;
    }

    public boolean isValid(List<Integer> sol) {
    	int zeros = 0;
        for (int i = 0; i < sol.size(); i++) {
        	if (sol.get(i) == 0) zeros++;
        }
//        System.out.println("Zeros: " + zeros);

        boolean repeated = false;
        for (int i = 0; i < sol.size() && !repeated; i++) {
        	for (int j = i+1; j < sol.size() && !repeated; j++) {
        		if (sol.get(i) == sol.get(j) && sol.get(i) != 0) repeated = true;
        	}
        }
//        System.out.println("Repeated: " + repeated);

      if (zeros > vehicleNumber_-1 || repeated) return false;

      return true;
    }

    public List<Integer> execute() {
        return null;
    }

    public double[] evaluate(List<Integer> sol) {
        double[] f = new double[2];
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
        while (i < sol.size()) {
            // Skip the contiguous zeros (unused vehicles)
            do {
                i++;
            } while (i < sol.size() && sol.get(i) == 0);

            if (i == sol.size()) continue;

            newVehicle = true;
            vehicleNumber++;
            do {
                if (newVehicle) {
                    // Distance from depot to first customer
                    f[0] += Math.sqrt(Math.pow(customers_[0][0] - customers_[0][sol.get(i)], 2) +
                            Math.pow(customers_[1][0] - customers_[1][sol.get(i)], 2));
                    newVehicle = false;
                }
                // Add capacity demanded by current customer
                capacityVehicle[vehicleNumber] += demand_[sol.get(i)];

                // If there is next customer, add the distance to him
                if (i+1 < sol.size() && sol.get(i+1) != 0) {
                    f[0] += Math.sqrt(Math.pow(customers_[0][sol.get(i)] - customers_[0][sol.get(i+1)], 2) +
                            Math.pow(customers_[1][sol.get(i)] - customers_[1][sol.get(i+1)], 2));
                }

                i++;
            } while (i < sol.size() && sol.get(i) != 0);

            // Add the distance from the last customer back to the depot
            f[0] += Math.sqrt(Math.pow(customers_[0][sol.get(i-1)] - customers_[0][0], 2) +
                    Math.pow(customers_[1][sol.get(i-1)] - customers_[1][0], 2));
            // Add capacity demanded by last customer
            capacityVehicle[vehicleNumber] += demand_[sol.get(i-1)];
        }

        // Evaluate the global fuel consumption based on the demand for each vehicle calculated before
        i = -1;
        vehicleNumber = -1;
        while (i < sol.size()) {
            // Skip the contiguous zeros (unused vehicles)
            do {
                i++;
            } while (i < sol.size() && sol.get(i) == 0);

            if (i == sol.size()) continue;

            newVehicle = true;
            vehicleNumber++;
            do {
                if (newVehicle) {
                    // Fuel consumption from depot to first customer
                    f[1] += (1 + capacityVehicle[vehicleNumber]/capacity_) *
                            Math.sqrt(Math.pow(customers_[0][0] - customers_[0][sol.get(i)], 2) +
                                    Math.pow(customers_[1][0] - customers_[1][sol.get(i)], 2));
                    newVehicle = false;
                }

                // If there is next customer, add fuel consumption to him
                if (i+1 < sol.size() && sol.get(i+1) != 0) {
                    capacityVehicle[vehicleNumber] -= demand_[sol.get(i)]; // Leave demanded capacity on customer
                    f[1] += (1 + capacityVehicle[vehicleNumber]/capacity_) *
                            Math.sqrt(Math.pow(customers_[0][sol.get(i)] - customers_[0][sol.get(i+1)], 2) +
                                    Math.pow(customers_[1][sol.get(i)] - customers_[1][sol.get(i+1)], 2));
                }

                i++;
            } while (i < sol.size() && sol.get(i) != 0);

            // Add fuel consumption from the last customer back to the depot
            capacityVehicle[vehicleNumber] -= demand_[sol.get(i-1)]; // Leave demanded capacity on customer
            assert capacityVehicle[vehicleNumber] == 0;
            f[1] += (1 + capacityVehicle[vehicleNumber]/capacity_) *
                    Math.sqrt(Math.pow(customers_[0][sol.get(i-1)] - customers_[0][0], 2) +
                            Math.pow(customers_[1][sol.get(i-1)] - customers_[1][0], 2));
        }

        return f;
    }

    // Evaluate sol and write the front (the values of the two objectives) in the file
    // /src/greedys/paretoFronts/<instanceName>.pf
    protected void generateFront(List<Integer> sol) {
        File pfDirectory;
        String paretoFrontDirectory = System.getProperty("user.dir")+"/src/greedys/paretoFronts/"+type_+"/";

        pfDirectory = new File(paretoFrontDirectory);

        if (!pfDirectory.exists()) {                          // Si no existe el directorio
            boolean result = new File(paretoFrontDirectory).mkdirs();        // Lo creamos
            System.out.println("Creating " + paretoFrontDirectory);
        }

        double[] fx = this.evaluate(sol);

        try {
            /* Open the file */
            FileOutputStream fos   = new FileOutputStream(paretoFrontDirectory+instanceName_+".pf");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw      = new BufferedWriter(osw);

            bw.write(fx[0]+" "+fx[1]);
            bw.newLine();

            /* Close the file */
            bw.close();
        }catch (IOException e) {
            Configuration.logger_.severe("Error acceding to the file");
            e.printStackTrace();
        }
    }
}
