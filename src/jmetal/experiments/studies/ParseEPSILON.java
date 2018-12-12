package jmetal.experiments.studies;

import jmetal.util.Configuration;

import java.io.*;
import java.util.Locale;
import java.util.Scanner;

public class ParseEPSILON {

	public static void main(String[] args) {
		String NSGAIIDirectory = System.getProperty("user.dir")+"/src/jmetal/data/CVRPTW/ExperimentalEvaluation0/data/NSGAII/";
		String outputFile = System.getProperty("user.dir")+"/epsilonData.cvs";

		String[] problemList = new String[30];
		for (int i = 0; i <= 9; i++) problemList[i] = "CVRPTWC110_" + (i+1);
		for (int i = 10; i <= 19; i++) problemList[i] = "CVRPTWR110_" + (i-9);
		for (int i = 20; i <= 29; i++) problemList[i] = "CVRPTWRC110_" + (i-19);

		try {
			Scanner[] scanners = new Scanner[problemList.length];
			for (int i=0; i<problemList.length; i++) {
				scanners[i] = new Scanner(new File(NSGAIIDirectory+problemList[i]+"/EPSILON")).useLocale(Locale.US);
				System.out.println(NSGAIIDirectory+problemList[i]+"/EPSILON");
			}

			/* Open the file to write the data */
			FileOutputStream fos   = new FileOutputStream(outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw      = new BufferedWriter(osw);

			// There are 50 EPSILON values in each file
			for (int i=0; i<50; i++) {
				for (int j=0; j<problemList.length-1; j++) {
					System.out.print("i: "+i);
					System.out.println("   j: "+j);
					bw.write(scanners[j].nextDouble()+",");
				}
				bw.write(Double.toString(scanners[problemList.length-1].nextDouble()));
				bw.newLine();
			}

			/* Close the output file */
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}
}
