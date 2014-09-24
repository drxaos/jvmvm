package net.sf.jauvm.vm;

public class VirtualMachineException extends RuntimeException {
    public VirtualMachineException() {
    }

    public VirtualMachineException(String message) {
        super(message);
    }

    public VirtualMachineException(String message, Throwable cause) {
        super(message, cause);
    }

    public VirtualMachineException(Throwable cause) {
        super(cause);
    }
}
