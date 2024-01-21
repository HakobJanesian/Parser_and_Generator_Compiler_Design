package SimpleCompilerPackage;

public class IllegalInstructionException extends RuntimeException {

	private static final long serialVersionUID = -886557317151576126L;

	public IllegalInstructionException() {
		super();
	}

	public IllegalInstructionException(String message) {
		super(message);
	}

	public IllegalInstructionException(Throwable cause) {
		super(cause);
	}

	public IllegalInstructionException(String message, Throwable cause) {
		super(message, cause);
	}

}
