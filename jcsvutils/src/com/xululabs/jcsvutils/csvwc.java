package com.xululabs.jcsvutils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException;

import au.com.bytecode.opencsv.CSVReader;

public class csvwc {

	public static void main(String args[]) {

		CSVReader in = null;

		Options options = new Options();

		options.addOption("i", true, "input file");
		options.addOption("h", false, "skip header (will nout count header");
		options.addOption("l", false,
				"count the number of rows in a csv file");
		options.addOption("c", false,
				"count the maximum columns in a csv file");
		options.addOption("C", true,
				"count the cells in a csv file");
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException pe) {
			pe.printStackTrace(System.err);
		}

		String inputFile="";
		if (cmd.hasOption("i")) {
			inputFile = cmd.getOptionValue("i");
			try {
				in = new CSVReader(new FileReader(inputFile));
			} catch (FileNotFoundException fnfe) {
				System.err.println("Error Input file not found: " + inputFile);
				System.exit(1);
			}
		} else {
			in = new CSVReader(new InputStreamReader(System.in));
		}

		boolean skipHeader = false;
		if (cmd.hasOption("h")) {
			skipHeader = true;
		}
		
		String row[]=null;
		
		long rows=0;
		long columns=0;
		long cells=0;

		try {
			while ((row = in.readNext()) != null) {
				if (skipHeader) {
					skipHeader=false;
					continue;
				}
				rows++;
				if (row.length>columns) columns=row.length;
				cells+=row.length;
			}
			in.close();
		} catch (IOException ioe) {
			System.err.println("Error reading file " + ioe);
			System.exit(1);
		}
		System.out.println(cells +" " + rows + " " + columns + " " + inputFile);

	}

}
