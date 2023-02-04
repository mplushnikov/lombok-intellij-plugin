// Generated by delombok at Sun Nov 06 17:15:05 CET 2022

//version 8: Jackson deps are at least Java7+.
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonBuilderSingular.JacksonBuilderSingularBuilder.class)
public class JacksonBuilderSingular {
	@JsonAnySetter
	private Map<String, Object> any;
	@JsonProperty("v_a_l_u_e_s")
	private List<String> values;
	@JsonAnySetter
	private ImmutableMap<String, Object> guavaAny;
	@JsonProperty("guava_v_a_l_u_e_s")
	private ImmutableList<String> guavaValues;

	@java.lang.SuppressWarnings("all")
	JacksonBuilderSingular(final Map<String, Object> any, final List<String> values, final ImmutableMap<String, Object> guavaAny, final ImmutableList<String> guavaValues) {
		this.any = any;
		this.values = values;
		this.guavaAny = guavaAny;
		this.guavaValues = guavaValues;
	}


	@java.lang.SuppressWarnings("all")
	@com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
	public static class JacksonBuilderSingularBuilder {
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> any$key;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<Object> any$value;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> values;
		@java.lang.SuppressWarnings("all")
		private com.google.common.collect.ImmutableMap.Builder<String, Object> guavaAny;
		@java.lang.SuppressWarnings("all")
		private com.google.common.collect.ImmutableList.Builder<String> guavaValues;

		@java.lang.SuppressWarnings("all")
		JacksonBuilderSingularBuilder() {
		}

		@JsonAnySetter
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder any(final String anyKey, final Object anyValue) {
			if (this.any$key == null) {
				this.any$key = new java.util.ArrayList<String>();
				this.any$value = new java.util.ArrayList<Object>();
			}
			this.any$key.add(anyKey);
			this.any$value.add(anyValue);
			return this;
		}

		@java.lang.SuppressWarnings("all")                                                                    //reduced '? extends Object' to '?'
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder any(final java.util.Map<? extends String, ?> any) {
			if (any == null) {
				throw new java.lang.NullPointerException("any cannot be null");
			}
			if (this.any$key == null) {
				this.any$key = new java.util.ArrayList<String>();
				this.any$value = new java.util.ArrayList<Object>();
			}
			for (final java.util.Map.Entry<? extends String, ?> $lombokEntry : any.entrySet()) {
				this.any$key.add($lombokEntry.getKey());
				this.any$value.add($lombokEntry.getValue());
			}
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder clearAny() {
			if (this.any$key != null) {
				this.any$key.clear();
				this.any$value.clear();
			}
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder value(final String value) {
			if (this.values == null) this.values = new java.util.ArrayList<String>();
			this.values.add(value);
			return this;
		}

		@JsonProperty("v_a_l_u_e_s")
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder values(final java.util.Collection<? extends String> values) {
			if (values == null) {
				throw new java.lang.NullPointerException("values cannot be null");
			}
			if (this.values == null) this.values = new java.util.ArrayList<String>();
			this.values.addAll(values);
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder clearValues() {
			if (this.values != null) this.values.clear();
			return this;
		}

		@JsonAnySetter
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder guavaAny(final String key, final Object value) {
			if (this.guavaAny == null) this.guavaAny = com.google.common.collect.ImmutableMap.builder();
			this.guavaAny.put(key, value);
			return this;
		}

    @java.lang.SuppressWarnings("all")                                                                                  //reduced '? extends Object' to '?'
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder guavaAny(final java.util.Map<? extends String, ?> guavaAny) {
			if (guavaAny == null) {
				throw new java.lang.NullPointerException("guavaAny cannot be null");
			}
			if (this.guavaAny == null) this.guavaAny = com.google.common.collect.ImmutableMap.builder();
			this.guavaAny.putAll(guavaAny);
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder clearGuavaAny() {
			this.guavaAny = null;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder guavaValue(final String guavaValue) {
			if (this.guavaValues == null) this.guavaValues = com.google.common.collect.ImmutableList.builder();
			this.guavaValues.add(guavaValue);
			return this;
		}

		@JsonProperty("guava_v_a_l_u_e_s")
		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder guavaValues(final java.lang.Iterable<? extends String> guavaValues) {
			if (guavaValues == null) {
				throw new java.lang.NullPointerException("guavaValues cannot be null");
			}
			if (this.guavaValues == null) this.guavaValues = com.google.common.collect.ImmutableList.builder();
			this.guavaValues.addAll(guavaValues);
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular.JacksonBuilderSingularBuilder clearGuavaValues() {
			this.guavaValues = null;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public JacksonBuilderSingular build() {
			java.util.Map<String, Object> any;
			switch (this.any$key == null ? 0 : this.any$key.size()) {
			case 0:
				any = java.util.Collections.emptyMap();
				break;
			case 1:
				any = java.util.Collections.singletonMap(this.any$key.get(0), this.any$value.get(0));
				break;
			default:
				any = new java.util.LinkedHashMap<String, Object>(this.any$key.size() < 1073741824 ? 1 + this.any$key.size() + (this.any$key.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				for (int $i = 0; $i < this.any$key.size(); $i++) any.put(this.any$key.get($i), (Object) this.any$value.get($i));
				any = java.util.Collections.unmodifiableMap(any);
			}
			java.util.List<String> values;
			switch (this.values == null ? 0 : this.values.size()) {
			case 0:
				values = java.util.Collections.emptyList();
				break;
			case 1:
				values = java.util.Collections.singletonList(this.values.get(0));
				break;
			default:
				values = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.values));
			}
			com.google.common.collect.ImmutableMap<String, Object> guavaAny = this.guavaAny == null ? com.google.common.collect.ImmutableMap.<String, Object>of() : this.guavaAny.build();
			com.google.common.collect.ImmutableList<String> guavaValues = this.guavaValues == null ? com.google.common.collect.ImmutableList.<String>of() : this.guavaValues.build();
			return new JacksonBuilderSingular(any, values, guavaAny, guavaValues);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "JacksonBuilderSingular.JacksonBuilderSingularBuilder(any$key=" + this.any$key + ", any$value=" + this.any$value + ", values=" + this.values + ", guavaAny=" + this.guavaAny + ", guavaValues=" + this.guavaValues + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static JacksonBuilderSingular.JacksonBuilderSingularBuilder builder() {
		return new JacksonBuilderSingular.JacksonBuilderSingularBuilder();
	}
}