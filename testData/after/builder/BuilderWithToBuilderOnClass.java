// Generated by delombok at Sat Jun 11 11:12:44 CEST 2016

class BuilderWithToBuilderOnClass<T> {
	private String one;
	private String two;
	private T foo;
	private int bar;

	public static <K> K rrr(BuilderWithToBuilderOnClass<K> x) {
		return x.foo;
	}

	@java.lang.SuppressWarnings("all")
	BuilderWithToBuilderOnClass(final String one, final String two, final T foo, final int bar) {
		this.one = one;
		this.two = two;
		this.foo = foo;
		this.bar = bar;
	}


	@java.lang.SuppressWarnings("all")
	public static class BuilderWithToBuilderOnClassBuilder<T> {
		@java.lang.SuppressWarnings("all")
		private String one;
		@java.lang.SuppressWarnings("all")
		private String two;
		@java.lang.SuppressWarnings("all")
		private T foo;
		@java.lang.SuppressWarnings("all")
		private int bar;

		@java.lang.SuppressWarnings("all")
		BuilderWithToBuilderOnClassBuilder() {
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithToBuilderOnClassBuilder<T> one(final String one) {
			this.one = one;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithToBuilderOnClassBuilder<T> two(final String two) {
			this.two = two;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithToBuilderOnClassBuilder<T> foo(final T foo) {
			this.foo = foo;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithToBuilderOnClassBuilder<T> bar(final int bar) {
			this.bar = bar;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithToBuilderOnClass<T> build() {
			return new BuilderWithToBuilderOnClass<T>(this.one, this.two, this.foo, this.bar);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithToBuilderOnClass.BuilderWithToBuilderOnClassBuilder(one=" + this.one + ", two=" + this.two + ", foo=" + this.foo + ", bar=" + this.bar + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static <T> BuilderWithToBuilderOnClassBuilder<T> builder() {
		return new BuilderWithToBuilderOnClassBuilder<T>();
	}

	@java.lang.SuppressWarnings("all")
	public BuilderWithToBuilderOnClassBuilder<T> toBuilder() {
		return new BuilderWithToBuilderOnClassBuilder<T>().one(this.one).two(this.two).foo(BuilderWithToBuilderOnClass.rrr(this)).bar(this.bar);
	}
}
