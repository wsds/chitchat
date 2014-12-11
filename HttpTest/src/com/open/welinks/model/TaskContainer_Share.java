package com.open.welinks.model;

import com.google.gson.Gson;
import com.open.lib.MyLog;
import com.open.welinks.utils.SHA1;

public class TaskContainer_Share {
	public String tag = "TaskContainer_Share";
	public Data data = Data.getInstance();
	public Gson gson = new Gson();
	public MyLog log = new MyLog(tag, true);
	public SHA1 sha1 = new SHA1();

}
