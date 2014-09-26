JvmVM
=====

Fork of JauVM - JVM stack emulator in Java

### A Java VM on top of JVM.
Target of this project is to create sandbox for user java code execution
with control over instructions and save/load running program.

To run VM you just need to create and compile project:
```
Project project = new Project("Program1")
    .addFiles(mapWithFileNamesAsKeysAndTheirContentsAsValues)
    .addSystemClasses(listOfSystemClassesThatYouAllowToUseInVm)
    .compile()
    .startVM("pkg.ClassName", "methodName", null, new Class[0], new Object[0]);
```

JvmVM virtualizes jvm stack and instructions execution for given code.

```
try {
  while (true) {
    project.step();
  }
} catch (ProjectStoppedException e) {
  Object result = e.getResult();
}
```

### Save / Load
Executed programs must use only serializable system classes for ability of VM serialization.
User classes made serializable by classloader.

```
try {
  while (true) {
    project.step();
  }
} catch (ProjectStoppedException e) {
  Object result = e.getResult();
}
```

### In Development
Project is still in alpha and contains bugs and hidden features.
See tests https://github.com/drxaos/JvmVM/tree/master/src/test/java/com/googlecode/jvmvm/tests
and source code https://github.com/drxaos/JvmVM/tree/master/src/main/java/com/googlecode/jvmvm.

### Authors and Contributors
Original project is JauVM - http://jauvm.blogspot.ru/.
To achieve full functionality original classes were modified
and additional utilities were added.

### Support or Contact
Contact me: vladimir.p.polyakov@gmail.com.
