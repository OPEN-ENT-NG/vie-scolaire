package fr.openent.viescolaire.utils;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(DateHelper.class);
    public final SimpleDateFormat SIMPLE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATE_FORMATTER= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public final SimpleDateFormat DATE_FORMATTER_SQL= new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
    public static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String MONGO_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String YEAR_MONTH_DAY = "yyyy-MM-dd";
    public static final String HOUR_MINUTES_SECONDS = "HH:mm:ss";
    public static final String HOUR_MINUTES = "HH:mm";

    public  final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");
    private static final String  START_DATE = "startDate";
    private static final String  END_DATE = "endDate";
    private static final String  DAY_OF_WEEK = "dayOfWeek";

    public static Date parseDate(String dateString, String format) {
        Date date = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            LOGGER.error(String.format("[Viescolaire@DateHelper::parseDate] Error when casting date: %s", e.getMessage()));
        }

        return date;
    }
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

    public boolean isBefore(String a, String b) {
        return isBefore(a, b, DATE_FORMATTER);
    }

    public boolean isBefore(String a, String b, SimpleDateFormat dateFormat) {
        Date dateA = getDate(a, dateFormat);
        Date dateB = getDate(b, dateFormat);
        return dateA.before(dateB);
    }

    public boolean isBeforeOrEquals(String a, String b, SimpleDateFormat dateFormat) {
        Date dateA = getDate(a, dateFormat);
        Date dateB = getDate(b, dateFormat);
        return dateA.before(dateB) || dateA.equals(dateB);
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

    public static String getDateString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String addHour(String date, int number, String format) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(parseDate(date, format));
        cal.add(Calendar.HOUR, number);
        return getDateString(cal.getTime(), format);
    }

    /**
     * Same that isAfter, but from a given format and without try / catch
     *
     * @param date1 First hour
     * @param date2 Second hour
     * @param format base dates format
     * @return Boolean that match if the first date is before the second date
     */
    public static boolean isDateFormatAfter(String date1, String date2, String format) {
        Date firstHour = new Date();
        Date secondHour = new Date();
        SimpleDateFormat sdft = new SimpleDateFormat(format);
        try {
            firstHour = sdft.parse(date1);
            secondHour = sdft.parse(date2);
        } catch (ParseException e) {
            LOGGER.error("[Viescolaire@DateHelper::isHourAfter] Error when casting hour: ", e);
        }

        return firstHour.after(secondHour);
    }

    /**
     * Test if 2 hours are equals from a given format, but without try / catch
     *
     * @param date1 First hour
     * @param date2 Second hour
     * @param format base dates format
     * @return Boolean that match if the first hour is before the second hour
     */
    public static boolean isDateFormatEqual(String date1, String date2, String format) {
        Date firstDate = new Date();
        Date secondDate = new Date();
        SimpleDateFormat sdft = new SimpleDateFormat(format);

        try {
            firstDate = sdft.parse(date1);
            secondDate = sdft.parse(date2);
        } catch (ParseException e) {
            LOGGER.error("[Viescolaire@DateHelper::isHourEqual] Error when casting hour: ", e);
        }

        return firstDate.equals(secondDate);
    }

    /**
     * Same that isAfter, but from a given format and without try / catch
     *
     * @param date1 First hour
     * @param date2 Second hour
     * @param format base dates format
     * @return Boolean that match if the first date is after the second date
     */
    public static boolean isHourAfterOrEqual(String date1, String date2, String format) {
        return isDateFormatAfter(date1, date2, format) || isDateFormatEqual(date1, date2, format);
    }


}
