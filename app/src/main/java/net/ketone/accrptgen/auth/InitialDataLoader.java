package net.ketone.accrptgen.auth;

import net.ketone.accrptgen.auth.model.User;
import net.ketone.accrptgen.auth.service.UserService;
import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.logging.Logger;

import static net.ketone.accrptgen.config.Constants.USERS_FILE;

@Configuration
public class InitialDataLoader {

    private static final Logger logger = Logger.getLogger(InitialDataLoader.class.getName());

    @Autowired
    private UserService userService;
    @Autowired
    @Qualifier("persistentStorage")
    private StorageService storageService;

//    private List<String> userStr = new ArrayList<>();

    @Bean
    public CommandLineRunner init() {
        return args -> {
        InputStream resource = storageService.loadAsInputStream(USERS_FILE);

        try ( BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource)) ) {
                    reader.lines().forEach(line -> {
                        String[] userPass = line.split(",");
                        User user = new User();
                        user.setUsername(userPass[0]);
                        user.setPassword(userPass[1]);
//                        userStr.add(line);
                        logger.info("saving user " + user.getUsername());
                        userService.save(user);
                    });
            } catch (Exception e) {
            logger.severe("Error loading " + USERS_FILE + " " + e.getMessage());
        }
    };
    }

/*    public void save(String username, String password) {
        try (OutputStream out = new FileOutputStream(new File("myData.txt"));
             PrintWriter writer = new PrintWriter(out)) {
            for(String oneUserStr : userStr) {
                writer.println(oneUserStr);
            }
            writer.println(username + "," + password);
            writer.flush();
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


}
