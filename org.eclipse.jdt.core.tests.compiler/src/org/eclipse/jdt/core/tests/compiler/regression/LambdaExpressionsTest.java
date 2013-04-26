/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
public class LambdaExpressionsTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testSuperReference03"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaExpressionsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public void test001() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"  int add(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = (x, y) -> {\n" +
				"      return x + y;\n" +
				"    };\n" +
				"    System.out.println(i.add(1234, 5678));\n" +
				"  }\n" +
				"}\n",
			},
			"6912"
			);
}
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface Greetings {\n" +
				"  void greet(String head, String tail);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    Greetings g = (x, y) -> {\n" +
				"      System.out.println(x + y);\n" +
				"    };\n" +
				"    g.greet(\"Hello, \", \"World!\");\n" +
				"  }\n" +
				"}\n",
			},
			"Hello, World!"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406178,  [1.8][compiler] Some functional interfaces are wrongly rejected
public void test003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"  void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    BinaryOperator<String> binOp = (x,y) -> { return x+y; };\n" +
				"    System.out.println(\"SUCCESS\");\n" +
				"    // System.out.println(binOp.apply(\"SUCC\", \"ESS\")); // when lambdas run\n" +
				"  }\n" +
				"}\n",
				"BiFunction.java",
				"@FunctionalInterface\n" + 
				"public interface BiFunction<T, U, R> {\n" + 
				"    R apply(T t, U u);\n" + 
				"}",
				"BinaryOperator.java",
				"@FunctionalInterface\n" + 
				"public interface BinaryOperator<T> extends BiFunction<T,T,T> {\n" + 
				"}"
			},
			"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406178,  [1.8][compiler] Some functional interfaces are wrongly rejected
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"  void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    BinaryOperator binOp = (x,y) -> { return x+y; };\n" +
				"    System.out.println(\"SUCCESS\");\n" +
				"    // System.out.println(binOp.apply(\"SUCC\", \"ESS\")); // when lambdas run\n" +
				"  }\n" +
				"}\n",
				"BiFunction.java",
				"@FunctionalInterface\n" + 
				"public interface BiFunction<T, U, R> {\n" + 
				"    R apply(T t, U u);\n" + 
				"}",
				"BinaryOperator.java",
				"@FunctionalInterface\n" + 
				"public interface BinaryOperator<T> extends BiFunction<T,T,T> {\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	BinaryOperator binOp = (x,y) -> { return x+y; };\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"BinaryOperator is a raw type. References to generic type BinaryOperator<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	BinaryOperator binOp = (x,y) -> { return x+y; };\n" + 
			"	                                         ^^^\n" + 
			"The operator + is undefined for the argument type(s) java.lang.Object, java.lang.Object\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test005() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String id(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> s;\n" +
				"		System.out.println(i.id(\"Hello\"));\n" +
				"	}\n" +
				"}\n"
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test006() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String id(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> s + s;\n" +
				"		System.out.println(i.id(\"Hello\"));\n" +
				"	}\n" +
				"}\n"
			},
			"HelloHello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test007() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void print(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> System.out.println(s);\n" +
				"		i.print(\"Hello\");\n" +
				"	}\n" +
				"}\n"
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test008() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String print(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> new String(s).toUpperCase();\n" +
				"		System.out.println(i.print(\"Hello\"));\n" +
				"	}\n" +
				"}\n"
			},
			"HELLO");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test009() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String print(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> new String(s);\n" +
				"		System.out.println(i.print(\"Hello\"));\n" +
				"	}\n" +
				"}\n"
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test010() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int unbox(Integer i);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> s;\n" +
				"		System.out.println(i.unbox(new Integer(1234)));\n" +
				"	}\n" +
				"}\n"
			},
			"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test011() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer box(int i);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> s;\n" +
				"		System.out.println(i.box(1234));\n" +
				"	}\n" +
				"}\n"
			},
			"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test012() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	X subType();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = () -> new Y();\n" +
				"		System.out.println(i.subType());\n" +
				"	}\n" +
				"}\n" +
				"class Y extends X {\n" +
				"    public String toString() {\n" +
				"        return \"Some Y\";\n" +
				"    }\n" +
				"}"
			},
			"Some Y");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test013() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        int in = 12345678;\n" +
				"        I i = (s) -> {\n" +
				"            I j = (s2) -> {\n" +
				"                System.out.println(s + s2 + in);  \n" +
				"            };\n" +
				"            j.foo(\"Number=\");\n" +
				"        };\n" +
				"        i.foo(\"The \");\n" +
				"    }\n" +
				"}\n"
			},
			"The Number=12345678");
}
public void test014() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void nonmain(String[] args) {\n" + 
					"    int var = 2;\n" + 
					"    I x2 = () -> {\n" + 
					"      System.out.println(\"Argc = \" + args.length);\n" + 
					"      for (int i = 0; i < args.length; i++) {\n" +
					"          System.out.println(\"Argv[\" + i + \"] = \" + args[i]);\n" +
					"      }\n" +
					"    };\n" +
					"    x2.doit();\n" +
					"    var=2;\n" + 
					"  }\n" +
					"  public static void main(String[] args) {\n" + 
					"      nonmain(new String[] {\"Hello! \", \"World!\" });\n" +
					"  }\n" +
					"}" ,
				},
				"Argc = 2\n" + 
				"Argv[0] = Hello! \n" + 
				"Argv[1] = World!");
}
public void test015() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    try {\n" + 
					"      new java.io.File((String) null).getCanonicalPath();\n" + 
					"    } catch (NullPointerException | java.io.IOException ioe) {\n" + 
					"      I x2 = () -> {\n" + 
					"        System.out.println(ioe.getMessage()); // OK: args is not re-assignment since declaration/first assignment\n" + 
					"      };\n" +
					"      x2.doit();\n" +
					"    };\n"+
					"  }\n" +
					"}\n"
				},
				"null");
}
public void test016() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    java.util.List<String> list = new java.util.ArrayList<>();\n" + 
					"    list.add(\"SomeString\");\n" +
					"    for (String s : list) {\n" + 
					"      I x2 = () -> {\n" + 
					"        System.out.println(s); // OK: args is not re-assignment since declaration/first assignment\n" + 
					"      };\n" + 
					"      x2.doit();\n" +
					"    };\n" + 
					"  }\n" + 
					"\n" +
					"}\n" ,
				},
				"SomeString");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406181, [1.8][compiler][codegen] IncompatibleClassChangeError when running code with lambda method
public void test017() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void foo(int x, int y);\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    BinaryOperator<String> binOp = (x,y) -> { return x+y; }; \n" +
					"    System.out.println(binOp.apply(\"SUCC\", \"ESS\")); // when lambdas run\n" +
					"  }\n" +
					"}\n" +
					"@FunctionalInterface\n" +
					"interface BiFunction<T, U, R> { \n" +
					"    R apply(T t, U u);\n" +
					"}\n" +
					"@FunctionalInterface \n" +
					"interface BinaryOperator<T> extends BiFunction<T,T,T> { \n" +
					"}\n",
				},
				"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test018() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X [][][] copy (short x);\n" +
					"}\n" +
					"public class X  {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X[][][]::new;\n" +
					"       I j = X[][][]::new;\n" +
					"		X[][][] x = i.copy((short) 631);\n" +
					"		System.out.println(x.length);\n" +
					"       x = j.copy((short) 136);\n" +
					"		System.out.println(x.length);\n" +
					"	}\n" +
					"}\n",
				},
				"631\n" + 
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test019() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X [][][] copy (int x);\n" +
					"}\n" +
					"public class X  {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X[][][]::new;\n" +
					"       I j = X[][][]::new;\n" +
					"		X[][][] x = i.copy(631);\n" +
					"		System.out.println(x.length);\n" +
					"       x = j.copy(136);\n" +
					"		System.out.println(x.length);\n" +
					"	}\n" +
					"}\n",
				},
				"631\n" + 
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test020() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X [][][] copy (Integer x);\n" +
					"}\n" +
					"public class X  {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X[][][]::new;\n" +
					"       I j = X[][][]::new;\n" +
					"		X[][][] x = i.copy(631);\n" +
					"		System.out.println(x.length);\n" +
					"       x = j.copy(136);\n" +
					"		System.out.println(x.length);\n" +
					"	}\n" +
					"}\n",
				},
				"631\n" + 
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test021() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X [][][] copy (Integer x);\n" +
					"}\n" +
					"public class X  {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X[][][]::new;\n" +
					"       I j = X[][][]::new;\n" +
					"		X[][][] x = i.copy(new Integer(631));\n" +
					"		System.out.println(x.length);\n" +
					"       x = j.copy(new Integer((short)136));\n" +
					"		System.out.println(x.length);\n" +
					"	}\n" +
					"}\n",
				},
				"631\n" + 
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test022() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    Object copy(int [] ia);\n" +
					"}\n" +
					"interface J {\n" +
					"	int [] copy(int [] ia);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = int[]::<String>clone;\n" +
					"        int [] x = new int [] { 10, 20, 30 };\n" +
					"        int [] y = (int []) i.copy(x);\n" +
					"        if (x == y || x.length != y.length || x[0] != y[0] || x[1] != y[1] || x[2] != y[2]) {\n" +
					"        	System.out.println(\"Broken\");\n" +
					"        } else {\n" +
					"        	System.out.println(\"OK\");\n" +
					"        }\n" +
					"        J j = int []::clone;\n" +
					"        y = null;\n" +
					"        y = j.copy(x);\n" +
					"        if (x == y || x.length != y.length || x[0] != y[0] || x[1] != y[1] || x[2] != y[2]) {\n" +
					"        	System.out.println(\"Broken\");\n" +
					"        } else {\n" +
					"        	System.out.println(\"OK\");\n" +
					"        }\n" +
					"    }\n" +
					"}\n" ,
				},
				"OK\n" + 
				"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test023() {
this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    Object copy(int [] ia);\n" +
					"}\n" +
					"\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = int[]::<String>clone;\n" +
					"        int [] ia = (int []) i.copy(new int[10]);\n" +
					"        System.out.println(ia.length);\n" +
					"    }\n" +
					"}\n",
				},
				"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test024() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    YBase copy(Y ia);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = Y::<String>copy;\n" +
					"        YBase yb = i.copy(new Y());\n" +
					"        System.out.println(yb.getClass());\n" +
					"    }\n" +
					"}\n" +
					"class YBase {\n" +
					"	public YBase copy() {\n" +
					"		return this;\n" +
					"	}\n" +
					"}\n" +
					"class Y extends YBase {\n" +
					"}\n",
				},
				"class Y");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test025() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    int foo(int [] ia);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = int[]::<String>hashCode;\n" +
					"        i.foo(new int[10]);\n" +
					"    }\n" +
					"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406589, [1.8][compiler][codegen] super call misdispatched 
public void test026() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	Integer foo(int x, int y);\n" +
					"}\n" +
					"class Y {\n" +
					"	int foo(int x, int y) {\n" +
					"		System.out.println(\"Y.foo(\" + x + \",\" + y + \")\");\n" +
					"		return foo(x, y);\n" +
					"	}\n" +
					"}\n" +
					"public class X extends Y {\n" +
					"	int foo(int x, int y) {\n" +
					"		System.out.println(\"X.foo(\" + x + \",\" + y + \")\");\n" +
					"		return x + y;\n" +
					"	}\n" +
					"	void goo() {\n" +
					"		I i = super::foo;\n" +
					"		System.out.println(i.foo(1234, 4321));\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().goo();\n" +
					"	}\n" +
					"}\n",
				},
				"Y.foo(1234,4321)\n" + 
				"X.foo(1234,4321)\n" + 
				"5555");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406589, [1.8][compiler][codegen] super call misdispatched 
public void test027() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	int foo(int x, int y);\n" +
					"}\n" +
					"interface J {\n" +
					"	default int foo(int x, int y) {\n" +
					"		System.out.println(\"I.foo(\" + x + \",\" + y + \")\");\n" +
					"		return x + y;\n" +
					"	}\n" +
					"}\n" +
					"public class X implements J {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = new X().f();\n" +
					"		System.out.println(i.foo(1234, 4321));\n" +
					"		i = new X().g();\n" +
					"		try {\n" +
					"			System.out.println(i.foo(1234, 4321));\n" +
					"		} catch (Throwable e) {\n" +
					"			System.out.println(e.getMessage());\n" +
					"		}\n" +
					"	}\n" +
					"	I f() {\n" +
					"		return J.super::foo;\n" +
					"	}\n" +
					"	I g() {\n" +
					"		return new X()::foo;\n" +
					"	}\n" +
					"	public int foo(int x, int y) {\n" +
					"		throw new RuntimeException(\"Exception\");\n" +
					"	}\n" +
					"}\n",
				},
				"I.foo(1234,4321)\n" + 
				"5555\n" + 
				"Exception");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406584, Bug 406584 - [1.8][compiler][codegen] ClassFormatError: Invalid method signature 
public void test028() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    Object copy();\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"    	int [] x = new int[] { 0xdeadbeef, 0xfeedface };\n" +
					"    	I i = x::<String>clone;\n" +
					"       System.out.println(Integer.toHexString(((int []) i.copy())[0]));\n" +
					"       System.out.println(Integer.toHexString(((int []) i.copy())[1]));\n" +
					"    }\n" +
					"}\n",
				},
				"deadbeef\n" + 
				"feedface");
}
public static Class testClass() {
	return LambdaExpressionsTest.class;
}
}