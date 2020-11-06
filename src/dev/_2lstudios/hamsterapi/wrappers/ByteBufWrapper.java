package dev._2lstudios.hamsterapi.wrappers;

import io.netty.buffer.ByteBuf;

public class ByteBufWrapper {
	private final ByteBuf byteBuf;

	public ByteBufWrapper(ByteBuf byteBuf) {
		this.byteBuf = byteBuf;
	}

	public ByteBuf get() {
		return this.byteBuf;
	}

	public int readInt() {
		return this.byteBuf.readInt();
	}

	public boolean readBoolean() {
		return this.byteBuf.readBoolean();
	}

	public byte readByte() {
		return this.byteBuf.readByte();
	}

	public char readChar() {
		return this.byteBuf.readChar();
	}

	public double readDouble() {
		return this.byteBuf.readDouble();
	}

	public float readFloat() {
		return this.byteBuf.readFloat();
	}

	public long readLong() {
		return this.byteBuf.readLong();
	}

	public short readShort() {
		return this.byteBuf.readShort();
	}

	public String readString() {
		String output = null;
		for (int i = 0; i < this.byteBuf.capacity(); i++) {
			if (output == null)
				output = "";
			byte b = this.byteBuf.getByte(i);
			output = output.concat(String.valueOf((char) b));
		}

		return output;
	}

	public boolean isReadeable() {
		return this.byteBuf.isReadable();
	}
}
