/*
 The class is a template for a packet.
 */
public class packet {
	int index;
	packet link = null;
	String data;

	public packet(int index, String data) {
		this.index = index;
		this.data = data;
	}
}
