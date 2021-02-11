import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

//This class pulls the excel file from the user's file system, updates the original with the new number, and creates a backup
public class SearchExcel {
    private static String date = "default";

    public static String searchExcel(int rrNumber, int locoNumber, String comments, MessageReceivedEvent event) throws IOException {
        //Importing the excel document and the sheet based on the discord channel the number was entered in
        FileInputStream inputStream = new FileInputStream(new File("E:\\Locomotive Database\\Current Locomotive Database.xlsx"));
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = wb.getSheetAt(rrNumber);

        Format databaseDate = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = databaseDate.format(new Date());
        Format fileDate = new SimpleDateFormat("MM-dd-yyyy");
        String updatedDate = fileDate.format(new Date());
        Format monthStr = new SimpleDateFormat("MMMM");
        String currentMonth = monthStr.format(new Date());
        Format month = new SimpleDateFormat("MM");
        String monthNumber = month.format(new Date());
        Format year = new SimpleDateFormat("yyyy");
        String currentYear = year.format(new Date());

        if(!date.equals("default")){
            System.out.println(date.length());
            if(date.charAt(2) == '/' && date.charAt(5) == '/' && date.length() == 10){
                currentDate = date;
                System.out.println("The current date was overridden!");
            }else if(date.charAt(1) == '/' && date.charAt(4) == '/' && date.length() == 9){
                currentDate = date;
                System.out.println("The current date was overridden!");
            }else if(date.charAt(1) == '/' && date.charAt(3) == '/' && date.length() == 8){
                currentDate = date;
                System.out.println("The current date was overridden!");
            }else {
                TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
                logs.sendMessage("Date given is invalid! Using the current date instead...").queue();
            }
        }

        String finished = controlF(rrNumber, locoNumber, comments, currentDate, sheet);

        //A backup is made and sorted based on the current date. The master file is also updated
        FileOutputStream backup = new FileOutputStream(new File("E:\\Locomotive Database\\" + currentYear + "\\" + monthNumber + "- " + currentMonth + "\\Train Database " + updatedDate + ".xlsx"));
        FileOutputStream updatedVersion = new FileOutputStream(new File("E:\\Locomotive Database\\Current Locomotive Database.xlsx"));
        wb.write(backup);
        wb.write(updatedVersion);
        backup.close();
        updatedVersion.close();
        return finished;
    }

    public static String controlF(int rrNumber, int locoNumber, String comments, String date, XSSFSheet rr) {
        if(locoNumber == 0){
            return "Input format incorrect! Use the format [Loco Number]-[Comments].";
        }

        try {
            double cellValue = 0;
            String cellText;
            String originalComments;
            int i = 1;
            XSSFRow r1 = rr.getRow(1);
            XSSFCell c1 = r1.getCell(1);

            //This while loop searches the file until either there are no cells remaining or the number is found
            while(c1.getCellType() != CellType.BLANK){
                r1 = rr.getRow(i);
                c1 = r1.getCell(1);
                if(c1.getCellType() == CellType.NUMERIC){
                    cellValue = c1.getNumericCellValue();
                }else if(c1.getCellType() == CellType.STRING){
                    cellText = c1.getStringCellValue();
                    //This does nothing but somehow resolves an issue with the UP section, so it's gotta stay
                }
                i++;

                //If the entered locomotive number matches something in the roster, the value is updated by 1 along with the comments/date
                if (cellValue == locoNumber) {
                    XSSFCell c2 = r1.getCell(4);
                    cellValue = c2.getNumericCellValue();
                    cellValue = cellValue + 1;
                    c2.setCellValue(cellValue);

                    //If the user specified comments, this puts them in the comments section
                    if(!comments.equals("")){
                        XSSFCell c3 = r1.getCell(5);
                        originalComments = comments;
                        c3.setCellValue(comments);
                    }

                    //If there are slots available in the dates section, today's date is placed in the farthest left one
                    for(int j = 6; j < 12; j++){
                        XSSFCell c4 = r1.getCell(j);
                        if(c4.getCellType() == CellType.BLANK){
                            c4.setCellValue(date);
                            return "Finished!";
                        }
                    }
                    return "Finished!";
                }
            }

            //Detects the NullPointerException generated by reaching the end of the sheet
        } catch (NullPointerException e) {
            String rrName = "[Railroad Name Not Found]";
            if(rrNumber == 0){
                rrName = "CSX";
            }else if(rrNumber == 1){
                rrName = "NS";
            }else if(rrNumber == 2){
                rrName = "UP";
            }else if(rrNumber == 3){
                rrName = "BNSF";
            }else if(rrNumber == 4){
                rrName = "KCS";
            }else if(rrNumber == 5){
                rrName = "CP";
            }else if(rrNumber == 6){
                rrName = "CN";
            }else if(rrNumber == 7){
                rrName = "Amtrak";
            }
            return "The number " + locoNumber + " was not found in " + rrName + "'s roster!";
        }
        //If it makes it this far then something definitely went wrong and wasn't caught earlier.
        return "An unknown error occurred! <@307636720789225472>";
    }
    public static void tempArray(String message){
        date = message;
        System.out.println(date);
    }
}
