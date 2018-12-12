package jmetal.experiments.studies;

import jmetal.util.Configuration;

import java.io.*;
import java.util.Locale;
import java.util.Scanner;

/**
 * Compare the fuel consumption from distance greedy to NSGAII.
 */
public class CompareFuel {
	public static void main(String[] args) {
		String pfDistanceGreedyDirectory = System.getProperty("user.dir")+"/src/greedys/paretoFronts/distance/";
		String pfNSGAIIDirectory = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/ExperimentalEvaluation/referenceFronts/";
		String outputFile = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/ExperimentalEvaluation/output2.txt";
		double fuelGreedyValue;
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
			bw.write("Fuel diff           ");
			bw.write("Fuel %diff");
			bw.newLine();

			// Compare the improvement of NSGAII respect to fuel consumption of distance greedy
			for (int i=0; i<problemList.length; i++) {
				// Get the fuel value from distance greedy
				Scanner scannerFuel = new Scanner(new File(pfDistanceGreedyDirectory+problemList[i]+".pf")).useLocale(Locale.US);
				// Fuel value is the second value, so apply nextDouble two times
				scannerFuel.nextDouble();
				fuelGreedyValue = scannerFuel.nextDouble();

				// Get the best fuel value from NSGAII
				Scanner scannerNSGAII = new Scanner(new File(pfNSGAIIDirectory+problemList[i]+".rf")).useLocale(Locale.US);
				do {
					// Fuel value is the second value, so apply nextDouble two times
					scannerNSGAII.nextDouble();
					double newFuel = scannerNSGAII.nextDouble();

					if (newFuel < nsgaiiFuelValue) nsgaiiFuelValue = newFuel;
				} while (scannerNSGAII.hasNextDouble());

				// Write the absolute and percentage difference between NSGAII and greedys
//				bw.write(problemList[i]+" ");
				bw.write(problemList[i]+"&");
//				if (i != 9 & i != 19 && i != 29) bw.write(" ");
//				if (i < 20) bw.write(" ");
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
