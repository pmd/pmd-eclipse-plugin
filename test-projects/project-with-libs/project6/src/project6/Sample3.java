package project6;

import sample.lib3.MyInterface;

// MyInterface comes in through a jar dependency in absolute path /tmp/sample-lib3-v1.jar.
// The jar file can be created in project sample-lib3
public class Sample3 implements MyInterface {

	// missing override
	public void bar() {
	}

}
