package myOwnLibraryBot;

import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
    private static Properties properties = new Properties();

    static {
        try(InputStream input = BotConfig.class.getClassLoader().getResourceAsStream("config.properties")){
            if(input==null){
                System.out.println("config.properties not found");
            } else {
                properties.load(input);

                System.out.println(properties.getProperty("bot.username"));
                System.out.println(properties.getProperty("bot.token"));

            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String getBotUsername(){
        return properties.getProperty("bot.username");
    }

    public static String getBotToken(){
        return properties.getProperty("bot.token");
    }
}

