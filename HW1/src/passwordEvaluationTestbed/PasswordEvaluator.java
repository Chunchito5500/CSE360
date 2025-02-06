package passwordEvaluationTestbed;

public class PasswordEvaluator {

    // Result attributes for GUI applications to enhance user experience with detailed error feedback
    public static String passwordErrorMessage = "";     // The error message text
    public static String passwordInput = "";            // The input being processed
    public static int passwordIndexofError = -1;        // The index where the error was located
    public static boolean foundUpperCase = false;
    public static boolean foundLowerCase = false;
    public static boolean foundNumericDigit = false;
    public static boolean foundSpecialChar = false;
    public static boolean foundLongEnough = false;
    public static boolean foundInvalidChar = false;     // Flag for invalid character detection

    private static String inputLine = "";               // The input line
    private static char currentChar;                    // The current character in the line
    private static int currentCharNdx;                  // The index of the current character
    private static boolean running;                     // The flag that specifies if the FSM is running
    private static int charCounter = 0;                 // Character counter for semantic actions

    /**********
     * Displays the input state for debugging.
     * 
     * @param input             The input string
     * @param currentCharNdx    The location where an error was found
     */
    private static void displayInputState() {
        System.out.println(inputLine);
        System.out.println(inputLine.substring(0, currentCharNdx) + "?");
        System.out.println("Password size: " + inputLine.length() + 
                " | CurrentCharNdx: " + currentCharNdx + " | CurrentChar: \"" + currentChar + "\"");
    }

    /**********
     * Validates a password based on the Directed Graph FSM.
     * 
     * @param input The input string for directed graph processing
     * @return      A string with errors listed on separate lines or an empty string if valid
     */
    public static String evaluatePassword(String input) {
        // Initialize variables
        passwordErrorMessage = "";
        passwordIndexofError = 0;
        inputLine = input;
        currentCharNdx = 0;
        charCounter = 0;

        if (input.length() <= 0) return "*** Error *** The password is empty!";

        // Initialize character processing
        currentChar = input.charAt(0);
        passwordInput = input;
        foundUpperCase = false;
        foundLowerCase = false;
        foundNumericDigit = false;
        foundSpecialChar = false;
        foundLongEnough = false;
        foundInvalidChar = false;
        running = true;

        // FSM loop
        while (running) {
            displayInputState();
            // Validate character type
            if (currentChar >= 'A' && currentChar <= 'Z') {
                System.out.println("Upper case letter found");
                foundUpperCase = true;
                charCounter++;
            } else if (currentChar >= 'a' && currentChar <= 'z') {
                System.out.println("Lower case letter found");
                foundLowerCase = true;
                charCounter++;
            } else if (currentChar >= '0' && currentChar <= '9') {
                System.out.println("Digit found");
                foundNumericDigit = true;
                charCounter++;
            } else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currentChar) >= 0) {
                System.out.println("Special character found");
                foundSpecialChar = true;
                charCounter++;
            } else {
                foundInvalidChar = true;
                passwordIndexofError = currentCharNdx;
                return "*** Error *** Invalid character found!";
            }
            if (charCounter >= 8) {
                System.out.println("At least 8 characters found");
                foundLongEnough = true;
            }

            // Advance to the next character
            currentCharNdx++;
            if (currentCharNdx >= inputLine.length())
                running = false;
            else
                currentChar = input.charAt(currentCharNdx);
        }

        // Build error messages, each on a new line
        StringBuilder errMessage = new StringBuilder();
        if (!foundUpperCase)
            errMessage.append("• Missing an uppercase letter.\n");
        if (!foundLowerCase)
            errMessage.append("• Missing a lowercase letter.\n");
        if (!foundNumericDigit)
            errMessage.append("• Missing a numeric digit.\n");
        if (!foundSpecialChar)
            errMessage.append("• Missing a special character.\n");
        if (!foundLongEnough)
            errMessage.append("• Password must be at least 8 characters long.\n");

        passwordIndexofError = currentCharNdx;
        return errMessage.toString().trim(); // Return all error messages without leading/trailing spaces
    }
}