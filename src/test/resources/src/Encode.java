public class Encode {
	private static final int BLOCKSIZE = 8;

	// Stripped down to be just this single method
	private static String encode(String text, long iv) {
		int len = text.length();
		byte[] buf = new byte[((len << 1) & 0xfffffff8) + 8];
		int pos = 0;
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			buf[pos++] = (byte) ((c >> 8) & 0x0ff);
			buf[pos++] = (byte) (c & 0x0ff);
		}
		byte pad = (byte) (buf.length - (len << 1));
		while (pos < buf.length)
			buf[pos++] = pad;
		byte[] head = new byte[BLOCKSIZE];
		longToByteArray(iv, head, 0);
		return bytesToBinHex(head, 0, BLOCKSIZE) + bytesToBinHex(buf, 0, buf.length);
	}

	private static void longToByteArray(long iv, byte[] head, int offset) {}
	private static String bytesToBinHex(byte[] head, int offset, int size) { return ""; }
}