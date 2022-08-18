package sfs2x.client.examples;

import java.util.Scanner;

public class MainClass {
	public static void main(String[] args) throws Exception {
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("Please enter you username: ");
			String username = scanner.next();
			SFS2XConnector sfs = new SFS2XConnector(username);
			sfs.putData();
			sfs.showAllRecords();
		}
	}
}