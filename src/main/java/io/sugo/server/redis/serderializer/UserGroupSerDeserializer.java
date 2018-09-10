package io.sugo.server.redis.serderializer;

import io.sugo.server.redis.DataIOFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

/**
 * Created by janpychou on 下午8:32.
 * Mail: janpychou@qq.com
 */
public class UserGroupSerDeserializer {
	private final DataIOFactory dataIOFactory;
	private final StringBuilder stringBuilder;
	private int rowCount;

	public UserGroupSerDeserializer(DataIOFactory dataIOFactory) {
		this.dataIOFactory = dataIOFactory;
		stringBuilder = new StringBuilder();
	}

	public void add(String val) {
		stringBuilder.append(val).append('\t');
		rowCount++;
	}

	public int getRowCount() {
		return rowCount;
	}

	/**
	 * data format: total\tuid1\tuid2\tuid3\t, eg: 3\taaa\tbbb\tccc\t
	 */
	public void serialize() {
		byte[] bytes = stringBuilder.insert(0, rowCount + "\t").toString().getBytes();
		dataIOFactory.writeBytes(bytes);
		dataIOFactory.close();
	}

	public void deserialize(Map<byte[], Boolean> cache) {
		byte[] bytes = dataIOFactory.readBytes();
		deserialize(cache, bytes);
	}

	/**
	 * data format: total\tuid1\tuid2\tuid3\t, eg: 3\taaa\tbbb\tccc\t
	 * @param cache
	 * @param bytes
	 */
	public int deserialize(Map<byte[], Boolean> cache, byte[] bytes) {
		int total = 0;
		if (bytes != null && bytes.length > 0) {
			ByteBuffer buffer = ByteBuffer.allocate(256);
			int len = bytes.length;
			int idx = 0;
			byte b;
			byte[] buf;
			//read items count
			while (idx < len) {
				b = bytes[idx++];
				if (b == '\t') {
					if (buffer.position() > 0) {
						buffer.flip();
						buf = new byte[buffer.limit()];
						buffer.get(buf);
						total = Integer.valueOf(new String(buf));
					}
					buffer.clear();
					break;
				} else {
					buffer.put(b);
				}
			}
			//read items
			while (idx < len) {
				b = bytes[idx++];
				if (b == '\t') {
					if (buffer.position() > 0) {
						buffer.flip();
						buf = new byte[buffer.limit()];
						buffer.get(buf);
						cache.put(buf, Boolean.TRUE);
					}
					buffer.clear();
				} else {
					buffer.put(b);
				}
			}
		}
		return total;
	}

	public void deserialize(Set<String> cache) {
		byte[] bytes = dataIOFactory.readBytes();
		deserialize(cache, bytes);
	}

	/**
	 * data format: total\tuid1\tuid2\tuid3\t, eg: 3\taaa\tbbb\tccc\t
	 * @param cache
	 * @param bytes
	 */
	public int deserialize(Set<String> cache, byte[] bytes) {
		int total = 0;
		if (bytes != null && bytes.length > 0) {
			ByteBuffer buffer = ByteBuffer.allocate(256);
			int len = bytes.length;
			int idx = 0;
			byte b;
			byte[] buf;
			//read items count
			while (idx < len) {
				b = bytes[idx++];
				if (b == '\t') {
					if (buffer.position() > 0) {
						buffer.flip();
						buf = new byte[buffer.limit()];
						buffer.get(buf);
						total = Integer.valueOf(new String(buf));
					}
					buffer.clear();
					break;
				} else {
					buffer.put(b);
				}
			}
			//read items
			while (idx < len) {
				b = bytes[idx++];
				if (b == '\t') {
					if (buffer.position() > 0) {
						buffer.flip();
						buf = new byte[buffer.limit()];
						buffer.get(buf);
						cache.add(new String(buf));
					}
					buffer.clear();
				} else {
					buffer.put(b);
				}
			}
		}
		return total;
	}
}
