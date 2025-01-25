package gg.xp.xivapi.exceptions;

/**
 * General exception for when we are trying to deserialize a response and something goes wrong with that.
 */
public class XivApiDeserializationException extends XivApiException {
	public XivApiDeserializationException() {
	}

	public XivApiDeserializationException(String message) {
		super(message);
	}

	public XivApiDeserializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public XivApiDeserializationException(Throwable cause) {
		super(cause);
	}

	public XivApiDeserializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
