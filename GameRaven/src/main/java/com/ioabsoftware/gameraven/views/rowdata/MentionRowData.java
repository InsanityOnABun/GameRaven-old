package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class MentionRowData extends BaseRowData {

    private String topic, board, user, time, url;

    public String getTopic() {
        return topic;
    }

    public String getBoard() {
        return board;
    }

    public String getUser() {
        return user;
    }

    public String getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    public MentionRowData(String topicIn, String boardIn, String userIn, String timeIn, String urlIn) {
        topic = topicIn;
        board = boardIn;
        user = userIn;
        time = timeIn;
        url = urlIn;
    }

    @Override
    public RowType getRowType() {
        return RowType.MENTION;
    }

    @Override
    public String toString() {
        return "topic: " + topic +
                "\nboard: " + board +
                "\nuser: " + user +
                "\ntime: " + time +
                "\nurl: " + url;
    }

}
