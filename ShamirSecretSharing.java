import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ShamirSecretSharing {

    // Helper function to convert a value from the given base to base 10
    public static long convertToBase10(String value, int base) {
        return Long.parseLong(value, base);
    }

    // Function to perform Lagrange Interpolation and find the constant term
    public static double lagrangeInterpolation(List<Point> points) {
        int k = points.size();
        double constantTerm = 0;

        for (int i = 0; i < k; i++) {
            double x_i = points.get(i).x;
            double y_i = points.get(i).y;
            double basis = 1;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    double x_j = points.get(j).x;
                    basis *= (0 - x_j) / (x_i - x_j);
                }
            }
            constantTerm += y_i * basis;
        }
        return constantTerm;
    }

    // Function to parse the JSON input and find the constant term
    public static double findConstantTerm(String filename) throws IOException {
        // Parse JSON input file using Gson
        Gson gson = new Gson();
        FileReader reader = new FileReader(filename);
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        reader.close();

        List<Point> points = new ArrayList<>();

        // Extract 'n' and 'k' from the 'keys'
        JsonObject keys = jsonObject.getAsJsonObject("keys");
        int n = keys.get("n").getAsInt();
        int k = keys.get("k").getAsInt();

        // Loop over the provided points and extract the (x, y) values
        for (Map.Entry<String, ?> entry : jsonObject.entrySet()) {
            if (entry.getKey().equals("keys")) {
                continue;  // Skip 'keys' field
            }

            int x = Integer.parseInt(entry.getKey());
            JsonObject valueObject = jsonObject.getAsJsonObject(entry.getKey());
            int base = valueObject.get("base").getAsInt();
            String value = valueObject.get("value").getAsString();
            long y = convertToBase10(value, base);  // Convert y to base 10
            points.add(new Point(x, y));
        }

        // Use only the first 'k' points for interpolation
        List<Point> selectedPoints = points.subList(0, k);

        // Compute the constant term using Lagrange Interpolation
        return lagrangeInterpolation(selectedPoints);
    }

    // Function to find and print wrong points (if any)
    public static List<Point> findWrongPoints(List<Point> points) {
        List<Point> wrongPoints = new ArrayList<>();
        // Example logic for finding wrong points: values that are too large
        for (Point point : points) {
            if (point.y > Math.pow(10, 14)) {  // Example threshold for large values
                wrongPoints.add(point);
            }
        }
        return wrongPoints;
    }

    public static void main(String[] args) {
        try {
            // Find the constant term for the first test case
            double secret1 = findConstantTerm("testcase1.json");
            System.out.println("The secret for the first test case is: " + secret1);

            // Find the constant term for the second test case
            double secret2 = findConstantTerm("testcase2.json");
            System.out.println("The secret for the second test case is: " + secret2);

            // Find and print wrong points for the second test case
            List<Point> points2 = findWrongPoints(findPointsFromTestCase("testcase2.json"));
            if (!points2.isEmpty()) {
                System.out.println("Wrong points in the second test case:");
                for (Point point : points2) {
                    System.out.println("Point: (" + point.x + ", " + point.y + ")");
                }
            } else {
                System.out.println("No wrong points found in the second test case.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper function to extract points from a test case file (for wrong points analysis)
    public static List<Point> findPointsFromTestCase(String filename) throws IOException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(filename);
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        reader.close();

        List<Point> points = new ArrayList<>();

        for (Map.Entry<String, ?> entry : jsonObject.entrySet()) {
            if (entry.getKey().equals("keys")) {
                continue;
            }
            int x = Integer.parseInt(entry.getKey());
            JsonObject valueObject = jsonObject.getAsJsonObject(entry.getKey());
            int base = valueObject.get("base").getAsInt();
            String value = valueObject.get("value").getAsString();
            long y = convertToBase10(value, base);
            points.add(new Point(x, y));
        }
        return points;
    }

    // Helper class to represent a point (x, y)
    static class Point {
        int x;
        long y;

        public Point(int x, long y) {
            this.x = x;
            this.y = y;
        }
    }
}
