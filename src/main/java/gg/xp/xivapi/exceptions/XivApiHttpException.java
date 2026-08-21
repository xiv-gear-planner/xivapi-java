package gg.xp.xivapi.exceptions;

public class XivApiHttpException extends XivApiException {
	public XivApiHttpException() {
	}

	public XivApiHttpException(String message) {
		super(message);
	}

	public XivApiHttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public XivApiHttpException(Throwable cause) {
		super(cause);
	}

	public XivApiHttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
