package project6;

import sample.lib1.MyInterface;

// MyInterface comes in through a jar dependency. The jar file can be created in project sample-lib1
public class Sample1 implements MyInterface {
	
	// missing override should be detected here. This only works, if sample-lib1-v1.jar is resolved correctly.
	public void bar(int i) {
		System.out.println("test");
	}
}
