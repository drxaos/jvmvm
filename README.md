JvmVM
=====

Fork of JauVM - JVM stack emulator in Java.

http://drxaos.github.io/JvmVM/

### A Java VM on top of JVM.
Target of this project is to create sandbox for user java code execution
with control over instructions and save/load running program.

To run VM you just need to create and compile project:
```java
Project project = new Project("Name")
    .addFiles(mapWithFileNamesAsKeysAndTheirContentsAsValues)
    .addSystemClasses(listOfSystemClassesThatYouAllowToUseInVm)
    .compile()
    .startVM("pkg.ClassName", "methodName");
```
This will start execution of static method on given class.

To execute methods on objects you can add synthetic class:
```java
Project project = new Project("Method call example")
    .addFiles(mapWithFileNamesAsKeysAndTheirContentsAsValues)
    .addFile("Boot.java", 
                "public class Boot {" + 
                "  public static String main() {" +
                "    return new TestClass().callMethod(\"hello\");" +
                "  }" +
                "}")
    .addSystemClasses(listOfSystemClassesThatYouAllowToUseInVm)
    .compile()
    .startVM("Boot", "main");
```

JvmVM virtualizes jvm stack and instructions execution for given code.

```java
while (project.isActive()) {
    project.step();
}
Object result = project.getResult();
```

or to execute to end
```java
Object result = project.run();
```

### Save / Load
Executed programs must use only serializable system classes for ability of VM serialization.
User classes made serializable by classloader.

```java
byte[] serializedProject = project.saveToBytes();
Project restoredProject = Project.fromBytes(serializedProject);
```
You can continue running restored project as if it is a new separate project, stopped at same point as original.


### In Development
Project is still in alpha and contains bugs and hidden features.
See tests http://git.io/4hc3tA and source code http://git.io/V6UvpA.

### Authors and Contributors
Original project is JauVM - http://jauvm.blogspot.ru/.
To achieve full functionality original classes were modified
and additional utilities were added.

### Support or Contact
Contact me: vladimir.p.polyakov@gmail.com.
