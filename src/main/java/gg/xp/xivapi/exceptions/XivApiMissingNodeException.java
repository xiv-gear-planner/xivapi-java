package gg.xp.xivapi.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class XivApiMissingNodeException extends XivApiDeserializationException {
	private final String messageBase;
	private final JsonNode node;
	private final Type fieldType;
	private final Method method;

	private static String formatMethodNullable(@Nullable Method method) {
		if (method == null) {
			return "not specified";
		}
		else {
			return method.getDeclaringClass().getSimpleName() + '.' + method.getName();
		}
	}

	public XivApiMissingNodeException(String messageBase, @Nullable JsonNode node, Type fieldType, @Nullable Method method) {
		super("Error deserializing node into %s (method %s): %s%nNode: %s".formatted(fieldType, formatMethodNullable(method), messageBase, node));
		this.messageBase = messageBase;
		this.node = node;
		this.fieldType = fieldType;
		this.method = method;
	}

	public String getMessageBase() {
		return messageBase;
	}

	public JsonNode getNode() {
		return node;
	}

	public Type getFieldType() {
		return fieldType;
	}

	public Method getMethod() {
		return method;
	}
}
