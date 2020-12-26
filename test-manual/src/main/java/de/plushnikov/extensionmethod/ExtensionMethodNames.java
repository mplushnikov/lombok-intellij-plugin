package de.plushnikov.extensionmethod;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionsX.class)
class ExtensionMethodNames {

	public void instanceCalls() {
		(new Test()).ext();

		Test t = new Test();
		t.ext();

		Test Test = new Test();
		Test.ext();
	}

	public void staticCalls() {
		TestX.ext();
    de.plushnikov.extensionmethod.TestX.ext();
	}
}

class ExtensionsX {
	public static void ext(Test t) {
	}
}

class TestX {
	public static void ext() {

	}
}
