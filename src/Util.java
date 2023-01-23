import org.json.JSONObject;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.*;

/**
 * Utility class containing useful utility methods usable in a useful way of being utilized
 */
public class Util {
    /**
     * Creates a prepared statement for the database
     * @param dbName Name of the database file
     * @param query Query to execute
     * @param args Arguments to go with the query
     * @throws SQLException Database error
     * @return A PreparedStatement that can be executed
     */
    private static PreparedStatement prepareStatement(String dbName, String query, Object... args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:db/" + dbName + ".db");
        PreparedStatement stmt = conn.prepareStatement(query);

        for (int i = 1; i <= args.length; i++) { stmt.setString(i, args[i - 1].toString()); }

        return stmt;
    }

    /**
     * Clears out the console
     */
    static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Gets the largest number
     * @param ns Numbers
     * @return Largest number
     */
    static int max(int... ns) {
        int ret = ns[0];
        for (int n : ns) if (n > ret) ret = n;
        return ret;
    }

    /**
     * Gets the smallest number
     * @param ns Numbers
     * @return Smallest number
     */
    static int min(int... ns) {
        int ret = ns[0];
        for (int n : ns) if (n < ret) ret = n;
        return ret;
    }

    /**
     * Wraps text, capping each line at the specified max
     * @param text Text to wrap
     * @param maxLen Line length cap
     * @return String broken into lines
     */
    static String wrapIt(String text, int maxLen) {
        String out = "";
        int breaks = 0;
        for (String chunk : text.split("\\s")) {
            if (out.length() - (breaks * maxLen + breaks) <= maxLen) { out += (chunk + " "); }
            else {
                out += ("\n" + chunk + " ");
                breaks++;
            }
        }

        return out.stripLeading().stripTrailing();
    }

    /**
     * Joins an array of Character (the object wrapper class, not the primitive type)
     * @param chars The sequence of Character
     * @return The joined sequence, as a String
     */
    static String joinCharacterArr(Character[] chars) {
        String out = "";
        for (char c : chars) { out += c; }
        return out;
    }

    /**
     * I don't fuckin' know, it's all black magic to me lmfao
     * @param s To compare
     * @param o To be compared with
     * @return A score out of 100, representing the similarity between the two strings
     *
     * @see https://medium.com/@ethannam/understanding-the-levenshtein-distance-equation-for-beginners-c4285a5604f0
     */
    static double levenshtein(String s, String o) {
        int sizeX = s.length() + 1;
        int sizeY = o.length() + 1;
        int[][] matrix = new int[sizeX][sizeY];

        for (int x = 0; x < sizeX; x++) { matrix[x][0] = x; }
        for (int y = 0; y < sizeY; y++) { matrix[0][y] = y; }

        for (int x = 1; x < sizeX; x++) {
            for (int y = 1; y < sizeY; y++) {
                matrix[x][y] = min(
                    matrix[x-1][y] + 1,
                    matrix[x][y-1] + 1,
                    matrix[x-1][y-1] + (s.charAt(x-1) != o.charAt(y-1) ? 1 : 0)
                );
            }
        }

        int distance = matrix[sizeX-1][sizeY-1];
        int longerLength = max(s.length(), o.length());
        return (double) (longerLength - distance) / longerLength * 100;
    }

    /**
     * Calculates words per minute using some random formula i found online lol
     * @param keysPressed The number of keys the user has hit
     * @param startedAt The time point at which typing began
     * @param acc The user's accuracy
     * @return The user's WPM
     *
     * @see https://support.sunburst.com/hc/en-us/articles/229335208-Type-to-Learn-How-are-Words-Per-Minute-and-Accuracy-Calculated-#:~:text=Calculating%20Words%20per%20Minute%20(WPM)&text=Therefore%2C%20the%20number%20of%20words,elapsed%20time%20(in%20minutes).
     */
    static double getWPM(String text, Date startedAt, double acc) {
        double rn = new Date().getTime();
        double interval = (rn - startedAt.getTime()) / 1000 / 60;
        return (double) (text.length() / 5 / interval) * (acc / 100);
    }

    /**
     * Sets up the records' database file if needed
     * @throws SQLException Database error
     */
    static void prepareDatabase() throws SQLException {
        final String SQL = """
            CREATE TABLE IF NOT EXISTS (
                timestamp REAL NOT NULL,
                accuracy REAL NOT NULL,
                wpm REAL NOT NULL,
                gamemode TEXT NOT NULL
            );
        """;

        prepareStatement("records", SQL).executeUpdate();
    }

    /**
     * Get a random set of words
     * @param max The amount of words to return
     * @return A space-spaced String of the words requested
     */
    static String getWords(int max) {
        final String SQL = "SELECT * FROM words WHERE id IN (SELECT id FROM words ORDER BY RANDOM() LIMIT ?)";

        String[] words = new String[max];
        try {
            int c = 0;
            ResultSet results = prepareStatement("words", SQL, max).executeQuery();
            while (results.next()) {
                words[c] = results.getString("word");
                c++;
            }
        }

        catch (Exception e) { e.printStackTrace(); }

        return String.join(" ", words);
    }

    /**
     * Fetches a quote from the Quotable API
     * @throws Exception Fetching the quote failed
     * @return A quote
     */
    static String fetchQuote() throws Exception {
        String text = "";
        try {
            URL url = new URL("https://api.quotable.io/random");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) { throw new Exception(CFG.QUOTE_TRY_LATER); }

            InputStreamReader respStream = new InputStreamReader(conn.getInputStream());
            BufferedReader stream = new BufferedReader(respStream);
            StringBuffer respBuf = new StringBuffer();
            while ((text = stream.readLine()) != null) { respBuf.append(text); }
            stream.close();

            JSONObject json = new JSONObject(respBuf.toString());
            text = (String) json.get("content");
        }

        catch (Exception e) { e.printStackTrace(); }
        return text;
    }

    /**
     * Saves a run to the database
     * @param acc The accuracy of the run
     * @param wpm The WPM of the run
     */
    static void saveStats(double acc, double wpm, String mode) {
        final String SQL = "INSERT INTO records VALUES (?, ?, ?, ?)";

        double rn = new Date().getTime();
        try { prepareStatement("records", SQL, rn, acc, wpm, mode).executeUpdate(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Gets the top 10 runs based on a column name (aka sort filter)
     * @param columnName
     * @return The top 10 runs split into separate lines
     */
    static String getTop10Stats(String columnName) {
        // so apparently paramaterization of the ORDER BY clause just doesn't work
        // it's often bad practice to concatenate a query, however it's okay in this case
        // since the user never has direct access to the string that's being passed to this method
        final String SQL = "SELECT * FROM records ORDER BY " + columnName + " DESC LIMIT 10";

        String out = String.format(
            "%s %-19s| %-19s| %-19s| %-19s.%s",
            Colours.GREEN + Colours.UNDERLINE,
            "Date", "Accuracy [%]", "WPM", "Mode",
            Colours.RESET
        );

        try {
            ResultSet results = prepareStatement("records", SQL).executeQuery();

            boolean hasResults = false;
            while (results.next()) {
                out += "\n";
                if (!hasResults) { hasResults = true; }

                // we have three columns of data to display (timestamp, accuracy, wpm, and gamemode)
                for (int i = 0; i < 4; i++) {
                    if (i == 0) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' @ 'HH:mm");
                        String formatted = sdf.format(new Date(results.getLong(1)));
                        out += String.format("%-20s", formatted);
                    }

                    else if (i == 3) { out += String.format("| %s", results.getString(4)); }

                    else {
                        String fmt = (i == 1) ? "| %-17.2f%% " : "| %-19.2f";
                        out += String.format(fmt, results.getDouble(i + 1));
                    }
                }
            }

            if (!hasResults) { return Colours.YELLOW + "No records..." + Colours.RESET; }
        }
        catch (Exception e) { e.printStackTrace(); }

        return out;
    }

    /**
     * Clears out all recorded runs
     */
    static void dropRecords() {
        final String SQL = "DELETE FROM records";
        try { prepareStatement("records", SQL).executeUpdate(); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
