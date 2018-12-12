package jmetal.experiments.studies;

import jmetal.util.Configuration;

import java.io.*;
import java.util.Locale;
import java.util.Scanner;

/**
 * Compare the pareto fronts from greedys to the pareto fronts of the NSGAII.
 */
public class CompareGreedy2NSGAII {
	public static void main(String[] args) {
		String pfDistanceGreedyDirectory = System.getProperty("user.dir")+"/src/greedys/paretoFronts/distance/";
		String pfFuelGreedyDirectory = System.getProperty("user.dir")+"/src/greedys/paretoFronts/fuel/";
		String pfNSGAIIDirectory = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/ExperimentalEvaluation/referenceFronts/";
		String outputFile = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/ExperimentalEvaluation/output.txt";
		String line;
		double distanceGreedyValue;
		double fuelGreedyValue;
		double nsgaiiDistanceValue = Double.MAX_VALUE;
		double nsgaiiFuelValue = Double.MAX_VALUE;

		String[] problemList = new String[30];
		for (int i = 0; i <= 9; i++) problemList[i] = "CVRPTWC110_" + (i+1);
		for (int i = 10; i <= 19; i++) problemList[i] = "CVRPTWR110_" + (i-9);
		for (int i = 20; i <= 29; i++) problemList[i] = "CVRPTWRC110_" + (i-19);

		try {
			/* Open the file to write the results from the comparisons */
			FileOutputStream fos   = new FileOutputStream(outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw      = new BufferedWriter(osw);

			bw.write("Instance       ");
			bw.write("Distance diff       ");
			bw.write("Distance %diff       ");
			bw.write("Fuel diff           ");
			bw.write("Fuel %diff");
			bw.newLine();

			// Compare the improvement of NSGAII respect to each greedy
			for (int i=0; i<problemList.length; i++) {
				// Get the distance value from distance greedy
				Scanner scannerDistance = new Scanner(new File(pfDistanceGreedyDirectory+problemList[i]+".pf")).useLocale(Locale.US);
				// Distance value is the first value, so apply nextDouble only one time
				distanceGreedyValue = scannerDistance.nextDouble();

				// Get the fuel value from fuel greedy
				Scanner scannerFuel = new Scanner(new File(pfFuelGreedyDirectory+problemList[i]+".pf")).useLocale(Locale.US);
				// Fuel value is the second value, so apply nextDouble two times
				fuelGreedyValue = scannerFuel.nextDouble();
				fuelGreedyValue = scannerFuel.nextDouble();

				// Get the best distance and fuel value from NSGAII
				Scanner scannerNSGAII = new Scanner(new File(pfNSGAIIDirectory+problemList[i]+".rf")).useLocale(Locale.US);
				do {
					double newDistance = scannerNSGAII.nextDouble();
					double newFuel = scannerNSGAII.nextDouble();

					if (newDistance < nsgaiiDistanceValue) nsgaiiDistanceValue = newDistance;
					if (newFuel < nsgaiiFuelValue) nsgaiiFuelValue = newFuel;
				} while (scannerNSGAII.hasNextDouble());

				// Write the absolute and percentage difference between NSGAII and greedys
//				bw.write(problemList[i]+" ");
				bw.write(problemList[i]+"&");
//				if (i != 9 & i != 19 && i != 29) bw.write(" ");
//				if (i < 20) bw.write(" ");
				bw.write(Double.toString(distanceGreedyValue-nsgaiiDistanceValue));
//				bw.write(" ");
				bw.write("&");
				bw.write(Double.toString(((distanceGreedyValue-nsgaiiDistanceValue) / distanceGreedyValue) * 100)+"\\%");
//				bw.write(" ");
				bw.write("&");
				bw.write(Double.toString(fuelGreedyValue-nsgaiiFuelValue));
//				bw.write(" ");
				bw.write("&");
				bw.write(Double.toString(((fuelGreedyValue-nsgaiiFuelValue) / fuelGreedyValue) * 100)+"\\%");
//				bw.write(" ");
				bw.write("\\\\");
				bw.newLine();
			}

			/* Close the output file */
			bw.close();
		}catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}
}
