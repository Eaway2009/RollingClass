package com.tanhd.rollingclass.db;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.QuestionData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Joe.Tan on 4/13/2017.
 */

public class Database {

    private SQLiteDatabase mDatabase;
    private final Context mContext;
    private String mOwnerId;
    private static Database mInstance;

    public static Database getInstance(Context context, String ownerId) {
        if (mInstance == null) {
            mInstance = new Database(context, ownerId);
        } else {
            mInstance.setOwnerId(ownerId);
        }

        return mInstance;
    }

    public static Database getInstance() {
        return mInstance;
    }

    public Database(Context context, String ownerId) {
        mContext = context;
        mOwnerId = ownerId;
        mDatabase = context.openOrCreateDatabase("message.db", Context.MODE_PRIVATE, null);
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                "message(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ownerId TEXT, " +
                "fromId TEXT, " +
                "toId TEXT, " +
                "content TEXT, " +
                "flag SMALLINT default 0," +   //0: 未读  1:已读
                "type SMALLINT default 0," +
                "time INTEGER default 0" +
                ");"
        );

        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                "questioning(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ownerId TEXT, " +
                "lessonSampleID TEXT, " +
                "uuid TEXT, " +
                "toId TEXT, " +
                "toName TEXT, " +
                "title TEXT, " +
                "ids TEXT, " +
                "flag SMALLINT default 0," +   //0: 未回答  1:已回答
                "time INTEGER default 0" +
                ");"
        );

        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                "exam(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ownerId TEXT, " +
                "examID TEXT, " +
                "lessonSampleID TEXT, " +
                "studentID TEXT, " +
                "toName TEXT, " +
                "questions TEXT, " +            //问题列表-JSON
                "answers TEXT, " +              //回答列表-JSON
                "flag SMALLINT default 0," +   //0: 未回答  1:已回答
                "time INTEGER default 0" +
                ");"
        );
    }

    private void setOwnerId(String ownerId) {
        mOwnerId = ownerId;
    }

    public void newMessage(String fromId, MSG_TYPE type, String content) {
        newMessage(fromId, mOwnerId, type, content, 0);
    }

    public void newMessage(String fromId, String toId, MSG_TYPE type, String content, int flag) {
        String sql = "INSERT INTO message(ownerId, fromId, toId, content, type, flag, time) values(?, ?, ?, ?, ?, ?, ?)";

        try {
            mDatabase.execSQL(sql,
                    new Object[] {mOwnerId, fromId, toId, content, type.ordinal(), flag, System.currentTimeMillis()});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> readAllMessage() {
        List<Message> messages = new ArrayList<Message>();
        HashSet<String> map = new HashSet<>();

        Cursor c = mDatabase.rawQuery("SELECT * FROM message " +
                        "WHERE ownerId=? order by time desc",
                new String[]{mOwnerId});
        while (c.moveToNext()) {
            int type = c.getInt(c.getColumnIndex("type"));
            String fromId = c.getString(c.getColumnIndex("fromId"));
            if (fromId.equals(mOwnerId) || map.contains(fromId))
                continue;

            map.add(fromId);

            Message message = new Message();
            message._id = c.getInt(c.getColumnIndex("_id"));
            message.fromId = fromId;
            message.toId = c.getString(c.getColumnIndex("toId"));
            message.content = c.getString(c.getColumnIndex("content"));
            message.flag = c.getInt(c.getColumnIndex("flag"));
            message.type = MSG_TYPE.values()[type];
            message.time = c.getLong(c.getColumnIndex("time"));
            messages.add(message);
        }
        c.close();

        return messages;
    }

    public List<Message> readNewMessage() {
        List<Message> messages = new ArrayList<Message>();

        Cursor c = mDatabase.rawQuery("SELECT * FROM message " +
                        "WHERE ownerId=? AND flag=0 order by time",
                new String[]{mOwnerId});
        while (c.moveToNext()) {
            Message message = new Message();
            message._id = c.getInt(c.getColumnIndex("_id"));
            message.fromId = c.getString(c.getColumnIndex("fromId"));
            message.toId = c.getString(c.getColumnIndex("toId"));
            message.content = c.getString(c.getColumnIndex("content"));
            message.flag = c.getInt(c.getColumnIndex("flag"));
            int type = c.getInt(c.getColumnIndex("type"));
            message.type = MSG_TYPE.values()[type];
            message.time = c.getLong(c.getColumnIndex("time"));
            messages.add(message);
        }
        c.close();

        return messages;
    }

    public Message readMessageById(int id) {
        Cursor c = mDatabase.rawQuery("SELECT * FROM message " +
                        "WHERE _id=?",
                new String[]{String.valueOf(id)});

        try {
            while (c.moveToNext()) {
                Message message = new Message();
                message._id = c.getInt(c.getColumnIndex("_id"));
                message.fromId = c.getString(c.getColumnIndex("fromId"));
                message.toId = c.getString(c.getColumnIndex("toId"));
                message.content = c.getString(c.getColumnIndex("content"));
                message.flag = c.getInt(c.getColumnIndex("flag"));
                int type = c.getInt(c.getColumnIndex("type"));
                message.type = MSG_TYPE.values()[type];
                message.time = c.getLong(c.getColumnIndex("time"));
                return message;
            }

        } finally {
            c.close();
        }

        return null;
    }

    public int newMessageCount() {
        int result = 0;
        Cursor c = mDatabase.rawQuery("SELECT count(_id) as count FROM message " +
                        "WHERE ownerId=? AND flag=0 order by time",
                new String[]{mOwnerId});
        while (c.moveToNext()) {
            result = c.getInt(c.getColumnIndex("count"));
        }
        c.close();

        return result;
    }

    public List<Message> readChatMessage(String toId) {
        List<Message> messages = new ArrayList<Message>();

        Cursor c = mDatabase.rawQuery("SELECT * FROM message " +
                        "WHERE ownerId=? AND (fromId=? OR toId=?) AND type=? order by time",
                new String[]{mOwnerId, toId, toId, String.valueOf(MSG_TYPE.TEXT.ordinal())});
        while (c.moveToNext()) {
            Message message = new Message();
            message._id = c.getInt(c.getColumnIndex("_id"));
            message.fromId = c.getString(c.getColumnIndex("fromId"));
            message.toId = c.getString(c.getColumnIndex("toId"));
            message.content = c.getString(c.getColumnIndex("content"));
            message.flag = c.getInt(c.getColumnIndex("flag"));
            int type = c.getInt(c.getColumnIndex("type"));
            message.type = MSG_TYPE.values()[type];
            message.time = c.getLong(c.getColumnIndex("time"));
            messages.add(message);
        }
        c.close();

        resetChatMessage(toId);
        return messages;
    }

    private void resetChatMessage(String toId) {
        String sql = "UPDATE message set flag=1 WHERE ownerId=? AND (fromId=? OR toId=?)";

        try {
            mDatabase.execSQL(sql,
                    new Object[] {mOwnerId, toId, toId});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetMessage(int id) {
        String sql = "UPDATE message set flag=1 WHERE _id=?";

        try {
            mDatabase.execSQL(sql,
                    new Object[] {id});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void newQuestioning(String lessonSampleID, String uuid, String toId, String toName, String title, String ids) {
        String sql = "INSERT INTO questioning(ownerId, lessonSampleID, uuid, toId, toName, title, ids, flag, time) values(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            mDatabase.execSQL(sql,
                    new Object[] {mOwnerId, lessonSampleID, uuid, toId, toName, title, ids, 0, System.currentTimeMillis()});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setQuestioning(String uuid) {
        String sql = "UPDATE questioning set flag=1 WHERE ownerId=? AND uuid=?";

        try {
            mDatabase.execSQL(sql,
                    new Object[] {mOwnerId, uuid});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Questioning> readQuestioning(String lessonSampleID) {
        List<Questioning> list = new ArrayList<Questioning>();

        Cursor c = mDatabase.rawQuery("SELECT * FROM questioning " +
                        "WHERE ownerId=? and lessonSampleID=? order by time",
                new String[]{mOwnerId, lessonSampleID});
        while (c.moveToNext()) {
            Questioning questioning = new Questioning();
            questioning.uuid = c.getString(c.getColumnIndex("uuid"));
            questioning.toId = c.getString(c.getColumnIndex("toId"));
            questioning.toName = c.getString(c.getColumnIndex("toName"));
            questioning.title = c.getString(c.getColumnIndex("title"));
            questioning.ids = c.getString(c.getColumnIndex("ids"));
            questioning.flag = c.getInt(c.getColumnIndex("flag"));
            questioning.time = c.getLong(c.getColumnIndex("time"));
            list.add(questioning);
        }
        c.close();

        return list;
    }

    public HashSet<String> getQuestionFilter(String lessonSampleID) {
        HashSet<String> filter = new HashSet<>();

        Cursor c = mDatabase.rawQuery("SELECT * FROM questioning " +
                        "WHERE ownerId=? and lessonSampleID=?",
                new String[]{mOwnerId, lessonSampleID});
        while (c.moveToNext()) {
            String toId = c.getString(c.getColumnIndex("toId"));
            filter.add(toId);
        }
        c.close();

        return filter;
    }

    public void newExam(String examID, String lessonSampleID, String studentID, String toName, List<QuestionData> questionDataList) {
        JSONArray array = new JSONArray();
        for (int i=0; i<questionDataList.size(); i++) {
            QuestionData questionData = questionDataList.get(i);
            array.put(questionData.toJSON());
        }

        String sql = "INSERT INTO exam(ownerId, examID, lessonSampleID, studentID, toName, questions, flag, time) values(?, ?, ?, ?, ?, ?, ?)";

        try {
            mDatabase.execSQL(sql,
                    new Object[] {mOwnerId, examID, lessonSampleID, studentID, toName, array.toString(), 0, System.currentTimeMillis()});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean setAnswer(String examID, String text) {
        try {
            mDatabase.execSQL("UPDATE exam set answers=?, flag=1 WHERE ownerId=? AND examID=?",
                    new Object[] {text, mOwnerId, examID});
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<ExamItem> queryExam(String lessonSampleID, String studentID) {
        List<ExamItem> list = new ArrayList<>();

        Cursor c = mDatabase.rawQuery("SELECT * FROM exam " +
                        "WHERE ownerId=? and lessonSampleID=? order by time",
                new String[]{mOwnerId, lessonSampleID});
        while (c.moveToNext()) {
            ExamItem examItem = new ExamItem();
            examItem.examID = c.getString(c.getColumnIndex("examID"));
            examItem.lessonSampleID = c.getString(c.getColumnIndex("lessonSampleID"));
            examItem.studentID = c.getString(c.getColumnIndex("studentID"));
            examItem.toName = c.getString(c.getColumnIndex("toName"));
            String questions = c.getString(c.getColumnIndex("questions"));
            String answers = c.getString(c.getColumnIndex("answers"));
            examItem.flag = c.getInt(c.getColumnIndex("flag"));
            examItem.time = c.getLong(c.getColumnIndex("time"));
            try {
                JSONArray array = new JSONArray(questions);
                examItem.questions = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.optJSONObject(i);
                    QuestionData questionData = new QuestionData();
                    questionData.parse(questionData, obj);
                    examItem.questions.add(questionData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONArray array = new JSONArray(answers);
                examItem.answers = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.optJSONObject(i);
                    AnswerData answerData = new AnswerData();
                    answerData.parse(answerData, obj);
                    examItem.answers.add(answerData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            list.add(examItem);
        }
        c.close();

        return list;
    }
}
