package com.tanhd.rollingclass.db;

import java.io.Serializable;

public class Document implements Serializable {
    public int _id;
    public String documentName;
    public int status;
    public String statusText;
    public String editTime;

    public Document(int _id, String documentName, int status, String statusText, String editTime) {
        this._id = _id;
        this.documentName = documentName;
        this.status = status;
        this.statusText = statusText;
        this.editTime = editTime;
    }
}
