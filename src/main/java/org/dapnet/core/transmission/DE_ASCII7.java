package org.dapnet.core.transmission;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

final class DE_ASCII7 extends Charset {

	private static final String[] ALIASES = { "DE-ASCII", "ASCII7_DE" };

	public DE_ASCII7() {
		super("DE-ASCII7", ALIASES);
	}

	@Override
	public boolean contains(Charset cs) {
		return (cs instanceof DE_ASCII7);
	}

	@Override
	public CharsetDecoder newDecoder() {
		return new Decoder(this);
	}

	@Override
	public CharsetEncoder newEncoder() {
		return new Encoder(this);
	}

	private static class Decoder extends CharsetDecoder {

		public Decoder(Charset cs) {
			super(cs, 1.0f, 1.0f);
		}

		@Override
		protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
			if (in.hasArray() && out.hasArray()) {
				return decodeArrayLoop(in, out);
			} else {
				return decodeBufferLoop(in, out);
			}
		}

		private static CoderResult decodeArrayLoop(ByteBuffer in, CharBuffer out) {
			byte[] sa = in.array();
			int sp = in.arrayOffset() + in.position();
			int sl = in.arrayOffset() + in.limit();
			assert (sp <= sl);
			sp = (sp <= sl ? sp : sl);

			char[] da = out.array();
			int dp = out.arrayOffset() + out.position();
			int dl = out.arrayOffset() + out.limit();
			assert (dp <= dl);
			dp = (dp <= dl ? dp : dl);

			try {
				while (sp < sl) {
					byte b = sa[sp];
					if (b >= 0) {
						if (dp >= dl) {
							return CoderResult.OVERFLOW;
						}

						da[dp] = toChar(b);
						++dp;
						++sp;
					} else {
						return CoderResult.malformedForLength(1);
					}
				}

				return CoderResult.UNDERFLOW;
			} finally {
				in.position(sp - in.arrayOffset());
				out.position(dp - out.arrayOffset());
			}
		}

		private static CoderResult decodeBufferLoop(ByteBuffer in, CharBuffer out) {
			int mark = in.position();
			try {
				while (in.hasRemaining()) {
					byte b = in.get();
					if (b >= 0) {
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}

						out.put(toChar(b));
						++mark;
					} else {
						return CoderResult.malformedForLength(1);
					}
				}

				return CoderResult.UNDERFLOW;
			} finally {
				in.position(mark);
			}
		}

		private static char toChar(byte b) {
			switch (b) {
			case 64:
				return 'ß';
			case 91:
				return 'Ä';
			case 92:
				return 'Ö';
			case 93:
				return 'Ü';
			case 123:
				return 'ä';
			case 124:
				return 'ö';
			case 125:
				return 'ü';
			case 126:
				return 'ß';
			default:
				return (char) b;
			}
		}

	}

	private static class Encoder extends CharsetEncoder {

		public Encoder(Charset cs) {
			super(cs, 1.0f, 1.0f);
		}

		@Override
		protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
			if (in.hasArray() && out.hasArray()) {
				return encodeArrayLoop(in, out);
			} else {
				return encodeBufferLoop(in, out);
			}
		}

		private static CoderResult encodeArrayLoop(CharBuffer in, ByteBuffer out) {
			char[] sa = in.array();
			int sp = in.arrayOffset() + in.position();
			int sl = in.arrayOffset() + in.limit();
			assert (sp <= sl);
			sp = (sp <= sl ? sp : sl);

			byte[] da = out.array();
			int dp = out.arrayOffset() + out.position();
			int dl = out.arrayOffset() + out.limit();
			assert (dp <= dl);
			dp = (dp <= dl ? dp : dl);

			try {
				while (sp < sl) {
					char c = sa[sp];
					if (dp >= dl) {
						return CoderResult.OVERFLOW;
					}

					da[dp] = toByte(c);
					++dp;
					++sp;
				}

				return CoderResult.UNDERFLOW;
			} finally {
				in.position(sp - in.arrayOffset());
				out.position(dp - out.arrayOffset());
			}
		}

		private static CoderResult encodeBufferLoop(CharBuffer in, ByteBuffer out) {
			int mark = in.position();
			try {
				while (in.hasRemaining()) {
					char c = in.get();
					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}

					out.put(toByte(c));
					++mark;
				}

				return CoderResult.UNDERFLOW;
			} finally {
				in.position(mark);
			}
		}

		private static byte toByte(char c) {
			switch (c) {
			case 'ß':
				return 64;
			case 'Ä':
				return 91;
			case 'Ö':
				return 92;
			case 'Ü':
				return 93;
			case 'ä':
				return 123;
			case 'ö':
				return 124;
			case 'ü':
				return 125;
			default:
				return (byte) (c & 0x7F);
			}
		}

	}

}
