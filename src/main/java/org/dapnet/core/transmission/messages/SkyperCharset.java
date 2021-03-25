package org.dapnet.core.transmission.messages;

import java.nio.charset.Charset;

/**
 * Implements the (German) Skyper charset.
 * 
 * @author Philipp Thiel
 */
final class SkyperCharset extends AbstractPagerCharset {

	private static final String[] ALIASES = { "DE-ASCII7", "ASCII7_DE" };

	/**
	 * Construct a new charset instance.
	 */
	public SkyperCharset() {
		super("DE-SKYPER", ALIASES);
	}

	@Override
	public boolean contains(Charset cs) {
		return (cs instanceof SkyperCharset);
	}

	@Override
	protected char mapToChar(byte b) {
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

	@Override
	protected byte mapToByte(char c) {
		switch (c) {
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
		case 'ß':
			return 126;
		default:
			return (byte) (c & 0x7F);
		}
	}

}
