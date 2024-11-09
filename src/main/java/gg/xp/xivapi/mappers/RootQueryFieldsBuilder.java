package gg.xp.xivapi.mappers;

import gg.xp.xivapi.exceptions.XivApiException;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RootQueryFieldsBuilder extends QueryFieldsBuilder {

	public RootQueryFieldsBuilder() {
		super(ROOT, false, false, List.of(), List.of());
	}



	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("RootQueryFieldsBuilder{");
		sb.append("fields=");
		sb.append(children.stream().filter(qf -> !qf.isTransient).flatMap(qf -> {
			try {
				return qf.toQueryStrings(true).stream();
			}
			catch (Throwable t) {
				return Stream.of("%s(ERROR)".formatted(qf.name));
			}
		}).collect(Collectors.joining(", ")));
		sb.append("; transients=");
		sb.append(children.stream().filter(qf -> qf.isTransient).flatMap(qf -> {
			try {
				return qf.toQueryStrings(true).stream();
			}
			catch (Throwable t) {
				return Stream.of("%s(ERROR)".formatted(qf.name));
			}
		}).collect(Collectors.joining(", ")));
		sb.append("}");
		return sb.toString();
	}

	public List<NameValuePair> formatQueryFields() {
		List<String> normalFields = new ArrayList<>();
		boolean normalAll = children.stream().anyMatch(child -> !child.isTransient && child.isAll());
		List<String> transientFields = new ArrayList<>();
		boolean transientAll = children.stream().anyMatch(child -> child.isTransient && child.isAll());
		for (QueryFieldsBuilder child : this.children) {
			List<String> queryStrings = child.toQueryStrings(true);
			// Hacky, would like to improve this
			if (child.isTransient) {
				if (transientAll) {
					for (String queryString : queryStrings) {
						if (queryString.contains("@")) {
							throw new XivApiException("Impossible combination of fields: cannot combine @XivApiThis with [%s]".formatted(queryString));
						}
					}
				}
				transientFields.addAll(queryStrings);
			}
			else {
				if (normalAll) {
					for (String queryString : queryStrings) {
						if (queryString.contains("@")) {
							throw new XivApiException("Impossible combination of fields: cannot combine @XivApiThis with [%s]".formatted(queryString));
						}
					}
				}
				normalFields.addAll(queryStrings);
			}
		}
		List<NameValuePair> out = new ArrayList<>();
		if (normalAll) {
			out.add(new BasicNameValuePair("fields", "*"));
		}
		else if (!normalFields.isEmpty()) {
			out.add(new BasicNameValuePair("fields", String.join(",", normalFields)));
		}
		if (transientAll) {
			out.add(new BasicNameValuePair("transient", "*"));
		}
		else if (!transientFields.isEmpty()) {
			out.add(new BasicNameValuePair("transient", String.join(",", transientFields)));
		}
		return out;
	}

}
