package greedys;

import jmetal.problems.CVRPTW;

import java.util.List;

public class GenerateFronts {
	public static void main(String[] args) {
		String instanceFilesDirectory = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/";

		String[] problemList = new String[30];
		for (int i = 0; i <= 9; i++) problemList[i] = "C110_" + (i+1);
		for (int i = 10; i <= 19; i++) problemList[i] = "R110_" + (i-9);
		for (int i = 20; i <= 29; i++) problemList[i] = "RC110_" + (i-19);

		// Execute greedys for all instances
		for (int i = 0; i < problemList.length; i++) {
			CVRPTW p = new CVRPTW(problemList[i]+".txt");

			// Execute distance greedy
			DistanceGreedy dg = new DistanceGreedy(
							"CVRPTW"+problemList[i],
							p.getVehicleNumber_(),
							p.getCapacity_(),
							p.getCustomerNumber_(),
							p.getCustomers_(),
							p.getDemand_(),
							p.getReadyTime_(),
							p.getDueDate_(),
							p.getService_());
			List<Integer> solD = dg.execute();
			dg.generateFront(solD);

			// Execute fuel greedy
			FuelGreedy fg = new FuelGreedy(
							"CVRPTW"+problemList[i],
							p.getVehicleNumber_(),
							p.getCapacity_(),
							p.getCustomerNumber_(),
							p.getCustomers_(),
							p.getDemand_(),
							p.getReadyTime_(),
							p.getDueDate_(),
							p.getService_());
			List<Integer> solF = fg.execute();
			fg.generateFront(solF);
		}
	}
}
