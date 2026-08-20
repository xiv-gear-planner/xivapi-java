package gg.xp.xivapi.exceptions;

/**
 * Exception for when we get the standard-form error response from Xivapi, where we have an unsuccessful status code and
 * a JSON response with 'code' and 'message' fields.
 */
public class XivApiErrorResponseException extends XivApiHttpException {

	private final int code;
	private final String message;

	public XivApiErrorResponseException(int code, String message) {
		super("Xivapi returned error. Code %s, message '%s'".formatted(code, message));
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
