JvmVM
=====

Fork of JauVM - JVM stack emulator in Java

Target of this project is to create sandbox for user java code execution
with control over instructions and save/load running program.

VM virtualizes jvm stack and instructions execution for some code.
Loader removes statics from classes and makes them serializable.
