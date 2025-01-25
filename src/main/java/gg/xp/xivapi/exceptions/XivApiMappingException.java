package gg.xp.xivapi.exceptions;

/**
 * Exception for when we are not able to create our tree of node mappers.
 */
public class XivApiMappingException extends XivApiException {
	public XivApiMappingException() {
	}

	public XivApiMappingException(String message) {
		super(message);
	}

	public XivApiMappingException(String message, Throwable cause) {
		super(message, cause);
	}

	public XivApiMappingException(Throwable cause) {
		super(cause);
	}

	public XivApiMappingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
