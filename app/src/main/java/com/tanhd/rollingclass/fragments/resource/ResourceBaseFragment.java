package com.tanhd.rollingclass.fragments.resource;

import android.support.v4.app.Fragment;

import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;

import java.util.List;

public abstract class ResourceBaseFragment extends Fragment {

    void setListData(List resourceList) {

    }

    void clearListData() {

    }

    abstract List getDataList();

    public interface Callback{

        void itemChecked(ResourceModel resourceModel, QuestionModel questionModel);

    }
}
