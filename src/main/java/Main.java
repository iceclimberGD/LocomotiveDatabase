import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;

import javax.security.auth.login.LoginException;
import java.io.IOException;

//This class activates the bot and sets its status
public class Main {
    public static void main (String[]args) throws LoginException, IOException {
        JDA api = JDABuilder.createDefault("[REDACTED]")
                .addEventListeners(new MessageListener())
                .build();
        api.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        api.getPresence().setActivity(Activity.watching("Trains"));
    }
}
