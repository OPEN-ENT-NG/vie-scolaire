package fr.openent.viescolaire.helper;

public class StringHelper {

    private StringHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert a string in CamelCase to snake_case
     */
    public static String camelToSnake(String str)
    {
        if (isNullOrEmpty(str)) {
            return str;
        }
        // Empty String
        StringBuilder result = new StringBuilder();

        // Append first character(in lower case)
        // to result string
        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));

        // Traverse the string from
        // ist index to last index
        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);

            // Check if the character is upper case
            // then append '_' and such character
            // (in lower case) to result string
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            }

            // If the character is lower case then
            // add such character into result string
            else {
                result.append(ch);
            }
        }

        // return the result
        return result.toString();
    }

    /**
     * @return true if string is empty or null
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}