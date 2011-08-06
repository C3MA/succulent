package util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

public class PicGetter implements Callable<byte[]> {

	private URL url = null;
	private String userAgent = "Mozilla/Firefox";
	private String cookie;

	public PicGetter(String url, String cookie) {
		this.cookie = cookie;
		try {
			//System.out.println(url);
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] call() throws Exception {

		InputStream is = null;
		HttpURLConnection c = null;

		int len = 0;
		int ch = 0;

		c = (HttpURLConnection) url.openConnection();
		c.addRequestProperty("User-Agent", userAgent);
		c.setRequestProperty("Cookie", cookie);
		is = c.getInputStream();
		len = c.getContentLength();

		byte[] bytes = new byte[len];

		if (len != -1) {
			// Read exactly Content-Length bytes
			for (int i = 0; i < len; i++)
				if ((ch = is.read()) != -1) {
					bytes[i] = (byte) ch;
				}
		}
		is.close();
		c.disconnect();
		return bytes;
	}
}