package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class WriteToFile {

	public void writeToFile(String content, String file) {
		Writer fw = null;
		try {
			fw = new FileWriter(file + ".gexf");
			fw.write(content);
		} catch (IOException e) {
			System.err.println("[X] Could not write to file!!");
			e.printStackTrace();
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
				}
		}
	}
}
