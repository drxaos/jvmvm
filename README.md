JvmVM
=====

Fork of JauVM - JVM stack emulator in Java.

http://drxaos.github.io/jvmvm/

### A Java VM on top of JVM.
Target of this project is to create sandbox for user java code execution
with control over instructions and save/load running program.

To run VM you just need to create and compile project:
```java
Project project = new Project("Name")
    .addFiles(mapWithFileNamesAsKeysAndTheirContentsAsValues)
    .addSystemClasses(listOfSystemClassesThatYouAllowToUseInVm)
    .compile()
    .setupVM("pkg.ClassName", "methodName");
```
This will start execution of static method on given class.

JvmVM virtualizes jvm stack and instructions execution for given code.
And then you can execute instructions:
```java
while (project.isActive()) {
    project.step();
}
Object result = project.getResult();
```

or to execute until return:
```java
Object result = project.run();
```

### Breakpoints
You can manage breakpoints to stop execution at needed point:
```java
project.setBreakpoint("pkg/ClassName.java", 15);
project.setBreakpoint("pkg.ClassName", "methodName");
project.removeBreakpoint("pkg/ClassName.java", 20);
try{
    Object result = project.run();
} catch(ProjectBreakpointException e) {
    // ...
}
project.clearBreakpoints();
```

### Data manipulation
VM has ability to mainpulate values of static objects and in stack:
```java
int x = project.getLocalVariable("x");
project.setLocalVariable("x", x + 1);

Object obj = project.getStaticField("pkg.ClassName", "fieldName");
project.setStaticField("pkg.ClassName", "fieldName", obj2);
```

### Save / Load
Executed programs must use only serializable system classes for ability of VM serialization.
User classes made serializable by classloader.

```java
byte[] serializedProject = project.saveToBytes();
Project restoredProject = Project.fromBytes(serializedProject);
restoredProject.run();
```
You can continue running restored project as if it is a new separate project, stopped at same point as original.


### In Development
Project is in alpha and contains bugs, undocumented features and documentation for unwritten features.
See tests and source code.

### Authors and Contributors
Original project is JauVM - http://jauvm.blogspot.ru/.
To achieve full functionality original classes were modified
and additional utilities were added.

### Support or Contact
Contact me: vladimir.p.polyakov@gmail.com.
