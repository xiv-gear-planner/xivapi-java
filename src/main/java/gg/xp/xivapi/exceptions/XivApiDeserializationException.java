package gg.xp.xivapi.exceptions;

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
