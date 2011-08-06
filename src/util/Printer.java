package util;

public class Printer {

	public static void print(String text, boolean webapp){
		if (!webapp) {
			System.out.println(text);
		}
	}
	
}
