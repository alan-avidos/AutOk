package avidos.autok.helper;

/**
 * Created by Alan on 30/11/2016.
 */

public interface OnPageCommunication {

    void dateSelected(boolean isDateSelected);
    boolean isDateSelected();
    void changePage(int page);
    void setDateTime(Long dateTime);
    Long getDateTime();
}
