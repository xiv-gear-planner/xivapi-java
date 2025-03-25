package gg.xp.xivapi.exceptions;

/**
 * General exception for when we are trying to paginate and something goes wrong.
 */
public class XivApiPaginationException extends XivApiException {
	public XivApiPaginationException() {
	}

	public XivApiPaginationException(String message) {
		super(message);
	}

	public XivApiPaginationException(String message, Throwable cause) {
		super(message, cause);
	}

	public XivApiPaginationException(Throwable cause) {
		super(cause);
	}

	public XivApiPaginationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
