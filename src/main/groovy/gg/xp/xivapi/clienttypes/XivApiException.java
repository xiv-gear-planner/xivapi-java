package gg.xp.xivapi.clienttypes;

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
