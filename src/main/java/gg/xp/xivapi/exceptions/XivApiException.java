package gg.xp.xivapi.exceptions;

/**
 * Top-level exception for XivApi-related errors.
 */
public class XivApiException extends RuntimeException {
	public XivApiException() {
	}

	public XivApiException(String message) {
		super(message);
	}

	public XivApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public XivApiException(Throwable cause) {
		super(cause);
	}

	public XivApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
