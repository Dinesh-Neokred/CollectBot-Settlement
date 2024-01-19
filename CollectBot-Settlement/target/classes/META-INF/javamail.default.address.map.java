import java.io.File;
import java.io.IOException;

public class CreateEmptyFileExample {
    public static void main(String[] args) {
        // Specify the path to the META-INF directory
        String metaInfPath = "path/to/your/project/META-INF";

        // Specify the file name
        String fileName = "javamail.default.address.map";

        // Create a File object for the desired file
        File file = new File(metaInfPath, fileName);

        try {
            // Create an empty file
            if (file.createNewFile()) {
                System.out.println("Empty file created successfully: " + file.getAbsolutePath());
            } else {
                System.out.println("File already exists: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("An error occurred while creating the file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
