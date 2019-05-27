package com.tanhd.rollingclass.server.data;

import com.tanhd.library.mqtthttp.PushMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BaseJsonClass implements Serializable {

    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
    }

    public void parse(Object object, JSONObject json) {
        if (json == null)
            return;

        Class cls = this.getClass();
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            String key = iter.next();

            try {
                Field field = cls.getField(key);
                if (field.getType() == String.class) {
                    field.set(object, json.optString(key));
                } else if (field.getType() == boolean.class) {
                    field.setBoolean(object, json.optBoolean(key));
                } else if (field.getType() == long.class) {
                    field.setLong(object, json.optLong(key));
                } else if (field.getType() == int.class) {
                    field.setInt(object, json.optInt(key));
                } else if (field.getType() == PushMessage.COMMAND.class) {
                    String value = json.optString(key);
                    field.set(object, PushMessage.COMMAND.valueOf(value));
                } else  {
                    onDealListField(object, field, json, key);
                }

            } catch (NoSuchFieldException e) {

            } catch (IllegalAccessException e) {

            }
        }
    }

    public void parse(Object object, String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            parse(object, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        Field[] fields = getClass().getFields();
        for (Field field: fields) {
            if (field.getName().equals("serialVersionUID"))
                continue;

            if (field.getName().equals("Context")) {
                Object o = null;
            }

            if (field.getType() == ArrayList.class || field.getType() == List.class) {
                try {
                    List list = (List) field.get(this);
                    if (list == null)
                        continue;

                    JSONArray array = new JSONArray();
                    for (int i=0; i<list.size(); i++) {
                        Object object = list.get(i);
                        if (object instanceof BaseJsonClass) {
                            BaseJsonClass bjc = (BaseJsonClass) object;
                            array.put(bjc.toJSON());
                        } else {
                            array.put(object);
                        }
                    }
                    json.put(field.getName(), array);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (field.getType() == HashMap.class) {

                try {
                    HashMap map = (HashMap) field.get(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }  else {
                try {
                    Object value = field.get(this);
                    if (value instanceof BaseJsonClass) {
                        BaseJsonClass bjc = (BaseJsonClass) value;
                        json.put(field.getName(), bjc.toJSON());
                    } else {
                        json.put(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        Field[] fields = getClass().getFields();
        for (Field field: fields) {
            if (field.getName().equals("serialVersionUID"))
                continue;

            if (field.getType() == List.class) {
                try {
                    List list = (List) field.get(this);
                    if (list == null)
                        continue;

                    JSONArray array = new JSONArray();
                    for (int i=0; i<list.size(); i++) {
                        Object object = list.get(i);
                        if (object instanceof BaseJsonClass) {
                            BaseJsonClass bjc = (BaseJsonClass) object;
                            array.put(bjc.toJSON());
                        } else {
                            array.put(object);
                        }
                    }
                    map.put(field.getName(), array.toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (field.getType() != HashMap.class) {
                try {
                    String value = String.valueOf(field.get(this));
                    map.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return map;
    }
}
