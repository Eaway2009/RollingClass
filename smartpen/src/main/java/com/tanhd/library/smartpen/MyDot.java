package com.tanhd.library.smartpen;

import com.tqltech.tqlpencomm.Dot;

import java.io.Serializable;

public class MyDot implements Serializable {
    public int Counter = 0;
    public int SectionID = 0;
    public int OwnerID = 0;
    public int BookID = 0;
    public int PageID = 0;
    public long timelong = 0L;
    public int x = 0;
    public int y = 0;
    public int fx = 0;
    public int fy = 0;
    public int force = 0;
    public int angle = 0;
    public int type;
    public float ab_x = 0.0F;
    public float ab_y = 0.0F;
    public int color = -16777216;

    public Dot getDot() {
        Dot dot = new Dot();
        dot.type = Dot.DotType.values()[type];
        dot.x = x;
        dot.fx = fx;
        dot.y = y;
        dot.fy = fy;
        dot.SectionID = SectionID;
        dot.OwnerID = OwnerID;
        dot.BookID = BookID;
        dot.PageID = PageID;
        dot.color = color;
        dot.Counter = Counter;
        dot.timelong = timelong;
        dot.force = force;
        dot.angle = angle;
        dot.ab_x = ab_x;
        dot.ab_y = ab_y;
        return dot;
    }

    public void init(Dot dot) {
        type = dot.type.ordinal();
        x = dot.x;
        fx = dot.fx;
        y = dot.y;
        fy = dot.fy;
        SectionID = dot.SectionID;
        OwnerID = dot.OwnerID;
        BookID = dot.BookID;
        PageID = dot.PageID;
        color = dot.color;
        Counter = dot.Counter;
        timelong = dot.timelong;
        force = dot.force;
        angle = dot.angle;
        ab_x = dot.ab_x;
        ab_y = dot.ab_y;
    }
}
