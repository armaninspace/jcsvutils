package com.xululabs.jcsvutils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class csvcut {

	public static void main(String args[]) {

		CSVReader in = null;
		CSVWriter out = null;

		Options options = new Options();

		options.addOption("i", true, "input file");
		options.addOption("o", true, "output file");
		options.addOption("h", false, "skip header");
		options.addOption("f", true,
				"cut fields: comma separated list or ranges");
		options.addOption("c", true,
				"cut columns: comma separated column names");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException pe) {
			pe.printStackTrace(System.err);
		}

		if (cmd.hasOption("i")) {
			String inputFile = cmd.getOptionValue("i");
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

		if (cmd.hasOption("f") && cmd.hasOption("c")) {
			System.err.println("Error cannot have both c and f options");
			System.exit(1);
		}

		ArrayList<Integer> columnList = new ArrayList<Integer>();
		boolean sufficientArgs = false;
		if (cmd.hasOption("f")) {
			String parts[] = cmd.getOptionValue("f").split(",");
			for (String part : parts) {
				if (part.contains("-")) {

					String range[] = part.split("-");
					if (range.length != 2) {
						System.err.println("Error in field arguments: "
								+ cmd.getOptionValue("f"));
						System.exit(1);
					}
					try {
						int start = Integer.parseInt(range[0]);
						int end = Integer.parseInt(range[1]);
						for (int count = start; count <= end; count++) {
							if (!columnList.contains(count))
								columnList.add(count);
						}
					} catch (NumberFormatException nfe) {
						System.err.println("Error in field arguments: "
								+ cmd.getOptionValue("f"));
						System.exit(1);
					}
				} else {
					try {
						int col = Integer.parseInt(part);
						if (!columnList.contains(col))
							columnList.add(col);
					} catch (NumberFormatException nfe) {
						System.err.println("Error in field arguments: "
								+ cmd.getOptionValue("f"));
						System.exit(1);
					}
				}
			}
			sufficientArgs = true;
		}
		String header[] = null;
		HashMap<String, Integer> headerMap = new HashMap<String, Integer>();

		if (!skipHeader) {
			try {
				// read header
				header = in.readNext();
				int count = 0;
				for (String headerLabel : header) {
					headerLabel = headerLabel.trim();
					if (headerMap.containsKey(headerLabel)) {
						System.err.println("Error Duplicate Header Label: "
								+ headerLabel);
						System.exit(1);
					}
					headerMap.put(headerLabel, count);
					count++;
				}
			} catch (IOException ioe) {
				System.err.println("Error creading header ");
				System.exit(1);
			}
		}

		if (cmd.hasOption("c")) {
			if (skipHeader) {
				System.err
						.println("Error column names cannot be used if skip header is chosen ");
				System.exit(1);
			}

			String parts[] = cmd.getOptionValue("c").split(",");

			for (String part : parts) {
				if (!headerMap.containsKey(part)) {
					System.err.println("Error column not found: " + part);
					System.exit(1);
				}
				columnList.add(headerMap.get(part));
			}
			sufficientArgs = true;
		}
		if (columnList.size() == 0) {
			System.err.println("Error no column selected");
			System.exit(1);
		}

		Collections.sort(columnList);

		if (cmd.hasOption("o")) {
			String outFile = cmd.getOptionValue("o");
			try {
				out = new CSVWriter(new FileWriter(outFile));
			} catch (IOException ioe) {
				System.err.println("Error Output file cannot be write: "
						+ outFile);
				System.exit(1);
			}
		} else {
			out = new CSVWriter(new OutputStreamWriter(System.out));
		}

		String row[] = null;

		if (!skipHeader) {
			String outRow[] = new String[columnList.size()];
			int count = 0;
			for (int i : columnList) {
				outRow[count] = header[i];
				count++;
			}
			out.writeNext(outRow);
		}

		try {
			while ((row = in.readNext()) != null) {
				String outRow[] = new String[columnList.size()];
				int count = 0;
				for (int i : columnList) {
					outRow[count] = row[i];
					count++;
				}
				out.writeNext(outRow);
			}
			out.flush();
			out.close();
			in.close();
		} catch (IOException ioe) {
			System.err.println("Error reading file " + ioe);
			System.exit(1);
		}

	}

}
