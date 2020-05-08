import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipIO {
	static Map<String, byte[]> read(File file, String namePattern) throws Exception {
		System.out.println("Read: " + file);
		ZipFile zf = new ZipFile(file);
		Map<String, byte[]> contents = new HashMap<>();
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
			ZipEntry entry = null;
			byte[] buffer = new byte[8192];
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().matches(namePattern)) {
					try (InputStream entryStream = zf.getInputStream(entry)) {
						int len;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						while ((len = entryStream.read(buffer)) != -1)
							baos.write(buffer, 0, len);
						contents.put(entry.getName(), baos.toByteArray());
					} finally {
						System.out.println(" - " + entry.getName());
					}
				}
			}
		} finally {
			System.out.println("Read: " + contents.size());
		}
		return contents;
	}
}