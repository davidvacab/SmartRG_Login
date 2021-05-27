package StaticControl;

import java.io.*;
import java.util.ArrayList;

public class Suggestions {
    public static ArrayList<String> readData(String fileName){
        ArrayList<String> suggestions = new ArrayList<>();
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            suggestions = (ArrayList) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException i) {
            writeData(suggestions, fileName);
            System.out.println(i.getMessage());
        }
        return suggestions;
    }

    public static void writeData(ArrayList<String> suggestions, String fileName){
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(suggestions);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            System.out.println(i.getMessage());
        }
    }
}
