public class E {
    public static class A {
        public class A1 {
            public int foo() {
                return 1;		
            }			
        }
        public class A2 {
        }		
        public int foo() {
            return 2;		
        }
    }
}
class F {
    public int goo(int param0) {
        class C extends E.A {
            A1 b;
            public int foo() {
                return 3;		
            }
        }		
        return 4;
    }
}