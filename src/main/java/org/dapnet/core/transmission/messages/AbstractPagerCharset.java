package org.dapnet.core.transmission.messages;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * Abstract base class for pager charsets. Only single-byte character sets are
 * supported. Implementing classes must provide char-to-byte and byte-to-char
 * mappings to implement the charset.
 * 
 * @author Philipp Thiel
 */
abstract class AbstractPagerCharset extends Charset {

	/**
	 * Constructs a new object instance.
	 * 
	 * @param canonicalName Canonical name of the charset
	 * @param aliases       Aliases for the charset or {@code null} if no aliases
	 *                      are used
	 */
	protected AbstractPagerCharset(String canonicalName, String[] aliases) {
		super(canonicalName, aliases);
	}

	@Override
	public CharsetDecoder newDecoder() {
		return new Decoder(this);
	}

	@Override
	public CharsetEncoder newEncoder() {
		return new Encoder(this);
	}

	/**
	 * Maps a byte to a char. This is used by the decoder.
	 * 
	 * @param b Input byte
	 * @return Output char
	 */
	protected abstract char mapToChar(byte b);

	/**
	 * Maps a char to a byte. This is used by the encoder.
	 * 
	 * @param c Input char
	 * @return Output byte
	 */
	protected abstract byte mapToByte(char c);

	private class Decoder extends CharsetDecoder {

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

		private CoderResult decodeArrayLoop(ByteBuffer in, CharBuffer out) {
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

						da[dp] = mapToChar(b);
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

		private CoderResult decodeBufferLoop(ByteBuffer in, CharBuffer out) {
			int mark = in.position();
			try {
				while (in.hasRemaining()) {
					byte b = in.get();
					if (b >= 0) {
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}

						out.put(mapToChar(b));
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

	}

	private class Encoder extends CharsetEncoder {

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

		private CoderResult encodeArrayLoop(CharBuffer in, ByteBuffer out) {
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

					da[dp] = mapToByte(c);
					++dp;
					++sp;
				}

				return CoderResult.UNDERFLOW;
			} finally {
				in.position(sp - in.arrayOffset());
				out.position(dp - out.arrayOffset());
			}
		}

		private CoderResult encodeBufferLoop(CharBuffer in, ByteBuffer out) {
			int mark = in.position();
			try {
				while (in.hasRemaining()) {
					char c = in.get();
					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}

					out.put(mapToByte(c));
					++mark;
				}

				return CoderResult.UNDERFLOW;
			} finally {
				in.position(mark);
			}
		}

	}

}
