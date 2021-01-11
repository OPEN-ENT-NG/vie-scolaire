package fr.openent.viescolaire.utils;

import fr.openent.viescolaire.service.impl.DefaultCoursService;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCoursService.class);
    public  final SimpleDateFormat SIMPLE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    public  static final SimpleDateFormat DATE_FORMATTER= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public  final SimpleDateFormat DATE_FORMATTER_SQL= new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
    public  static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String MONGO_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public  final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");
    private static final String  START_DATE = "startDate";
    private static final String  END_DATE = "endDate";
    private static final String  DAY_OF_WEEK = "dayOfWeek";
    Calendar firstOccurrenceDate(JsonObject course){
        Calendar start = getCalendar(course.getString(START_DATE), DATE_FORMATTER);
        start.set(Calendar.DAY_OF_WEEK, course.getInteger("dayOfWeek")+1);
        if(start.before(getCalendar(course.getString(START_DATE),DATE_FORMATTER))){
            start.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return start;
    }
    Calendar lastOccurrenceDate(JsonObject course){
        Calendar end = getCalendar(course.getString(END_DATE), DATE_FORMATTER);
        end.set(Calendar.DAY_OF_WEEK, course.getInteger("dayOfWeek")+1);
        if(end.after(getCalendar(course.getString(END_DATE), DATE_FORMATTER))){
            end.add(Calendar.WEEK_OF_YEAR, -1);
        }
        return end;
    }
    Calendar longToCalendar(Long  date){
        Calendar calendarOccurrence = Calendar.getInstance();
        calendarOccurrence.setTimeInMillis(date);
        calendarOccurrence.add(Calendar.DAY_OF_WEEK, 1);
        return calendarOccurrence;
    }
    int daysBetween(Calendar startDate, Calendar endDate) {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return (int) TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }
    public Date getDate(String dateString,SimpleDateFormat dateFormat ){
        Date date= new Date();
        try{
            date =  dateFormat.parse(dateString);
        } catch (ParseException e) {
            LOGGER.error("error when casting date: ", e);
        }
        return date ;
    }
    Calendar getCalendar(String dateString, SimpleDateFormat dateFormat){
        Calendar date= Calendar.getInstance();
        try{
            date.setTime(dateFormat.parse(dateString))  ;
        } catch (ParseException e) {
            LOGGER.error("error when casting date: ", e);
        }
        return date ;
    }
    Date getCombineDate(Date part1,String part2){
        Date date= new Date();
        try{
            date =  DATE_FORMATTER.parse(
                    SIMPLE_DATE_FORMATTER.format(part1)
                            +'T'
                            + TIME_FORMATTER.format(getDate(part2,DATE_FORMATTER)));
        } catch (ParseException e) {
            LOGGER.error("error when casting date: ", e);
        }
        return date ;
    }

    /**
     * Fetching current date (now())
     *
     * @param format format date to format your type of start and end date
     * @return return current date with the wished format
     */
    public static String getCurrentDate(String format) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        return sdf.format(calendar.getTime());
    }

    /**
     * Fetching current date (now())
     * <p>
     * Possibility to modify value in this calendar method
     *
     * @param format        format date to format your type of start and end date
     * @param calendarValue Calendar.HOUR, Calendar.MINUTE etc...
     * @param value         value amount on calendarValue
     * @return return       current date with the wished format
     */
    public static String getCurrentDate(String format, int calendarValue, int value) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        calendar.add(calendarValue, value);
        return sdf.format(calendar.getTime());
    }


}
