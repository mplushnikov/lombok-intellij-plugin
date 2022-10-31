package before.constructors;

import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor(force = true)
public class NoArgsWithDefinedConstructorsIsForced {
    private final String test;
    private final String test2;
    private final int test3;

    public NoArgsWithDefinedConstructorsIsForced(String param, String param2) {
        this.test = param;
        this.test2 = param2;
        this.test3 = 1;
    }

    public NoArgsWithDefinedConstructorsIsForced(String param) {
        this.test = param;
        this.test2 = param;
        this.test3 = 1;
    }

    public NoArgsWithDefinedConstructorsIsForced(String param, String param2, int param3) {
        this.test = param;
        this.test2 = param2;
        this.test3 = param3;
    }

    public static void main(String[] args) {
        final NoArgsWithDefinedConstructorsIsForced testClass = new NoArgsWithDefinedConstructorsIsForced();
        System.out.println(testClass);
    }
}