import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;

import java.io.IOException;
import java.util.List;

//This class detects the user's message, and if it is in one of the right channels, it sends info to the SearchExcel class for it to update Excel
public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        //If the message was sent by a bot, nothing will happen
        if(event.getAuthor().isBot()) return;

        int rrNumber = -1; //setting this to -1 will cause an error if it isn't changed later
        int locoNumber = 0;

        Message userInput = event.getMessage();
        MessageChannel channel = event.getChannel();
        MessageChannel dateChannel = event.getGuild().getTextChannelsByName("date",true).get(0);

        String comment = "";
        String channelName = channel.toString();
        String logMessage = "Default Error Message";
        String id = dateChannel.getLatestMessageId();
        String[] railroadName = channelName.split("[:(]");

        //Only way I could find to retrieve a message by ID and then store it somewhere. Don't get it, but it works.
        dateChannel.retrieveMessageById(id).queue((message) -> {
            String possibleDate = message.getContentRaw();
            SearchExcel.tempArray(possibleDate);
        }, (failure) -> {
            if (failure instanceof ErrorResponseException) {
                ErrorResponseException ex = (ErrorResponseException) failure;
                if (ex.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                    System.out.println("Date not found, using today's date instead.");
                }
            }
            failure.printStackTrace();
        });

        //the second item in this array will be just the railroad name, without the channel ID
        switch(railroadName[1]){
            case("csx"):
                rrNumber = 0;
                break;
            case("ns"):
                rrNumber = 1;
                break;
            case("up"):
                rrNumber = 2;
                break;
            case("bnsf"):
                rrNumber = 3;
                break;
            case("kcs"):
                rrNumber = 4;
                break;
            case("cp"):
                rrNumber = 5;
                break;
            case("cn"):
                rrNumber = 6;
                break;
            case("amtrak"):
                rrNumber = 7;
                break;
            default:
                System.out.println("Something went wrong with the switch statement!");
                break;
        }

        try {
            //Takes the user's message and splits it based on the dash
            String m1Content = userInput.getContentRaw();
            String[] m1Array = m1Content.split("-");
            locoNumber = Integer.parseInt(m1Array[0]);
            comment = m1Array[1];

        }catch(NumberFormatException e){
            logMessage = "Input format incorrect! Use the format [Loco Number]-[Comments].";
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("Comments were not entered!");
        }
        try{
            logMessage = SearchExcel.searchExcel(rrNumber,locoNumber,comment, event);
        }catch (IOException e) {
            logMessage = "Backup file path does not exist, or one of the files is open!";
        }

        //If nothing went wrong, the bot reacts with a check. Otherwise, it reacts with an X and logs the error
        if(logMessage.equals("Finished!")){
            userInput.addReaction("✅").queue();
        }else{
            userInput.addReaction("❌").queue();
        }

        //Any error message is posted in the logs channel
        if(!logMessage.equals("Finished!")){
            TextChannel logs = event.getGuild().getTextChannelsByName("logs",true).get(0);
            logs.sendMessage(logMessage).queue();
        }
    }
}
