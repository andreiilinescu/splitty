package client.utils;

import java.io.*;

public class Config {
    private static  final String USER_FILENAME="userConfig";

    public static void writeUserConfigFile(User user){
        String projectDir = new File("").getAbsolutePath();
        projectDir=projectDir.substring(0, projectDir.indexOf("oopp-team-35"))+"oopp-team-35/";
        File file = new File(projectDir+"client/build/resources/main", USER_FILENAME);
        System.out.println(projectDir);
        try {
            if (file.createNewFile()){
                System.out.println("User config file was created");
            }
        } catch (IOException e) {
            System.out.println("Error creating user config file");
        }
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(user);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public  static User readUserConfigFile(){
        String projectDir = new File("").getAbsolutePath();
        projectDir=projectDir.substring(0, projectDir.indexOf("oopp-team-35"))+"oopp-team-35/";
        File file=new File(projectDir+"client/build/resources/main", USER_FILENAME);
        User user=null;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            user = (User) ois.readObject();
        } catch (IOException | ClassNotFoundException ignored) {}
        return  user;
    }

    public static boolean deleteUserConfigFile(){
        String projectDir = new File("").getAbsolutePath();
        projectDir=projectDir.substring(0, projectDir.indexOf("oopp-team-35"))+"oopp-team-35/";
        File file=new File(projectDir+"client/build/resources/main", USER_FILENAME);
        return file.delete();
    }
}
