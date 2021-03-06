package com.open.chitchat.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.DisplayMetrics;

import com.open.chitchat.model.Data.Relationship.Friend;
import com.open.chitchat.model.Data.Relationship.Group;
import com.open.chitchat.model.Data.TempData.ImageBean;

public class Data {

	public static Data data;

	public static Data getInstance() {
		if (data == null) {
			data = new Data();
		}
		return data;
	}

	public LocalStatus localStatus = new LocalStatus();
	public BaseData baseData = new BaseData();
	public TempData tempData = new TempData();
	public UserInformation userInformation = new UserInformation();
	public Relationship relationship = new Relationship();
	public Messages messages = new Messages();
	public Event event = new Event();

	public class BaseData {
		public boolean notification;

		public DisplayMetrics metrics;
		public float screenWidth;
		public float screenHeight;
		public float appHeight;
		public float stateBar;
		public float density;

	}

	public class TempData {
		public ArrayList<String> selectedImageList;

		public Friend tempFriend;
		public Group tempGroup;

		public ArrayList<String> friends;
		public HashMap<String, Friend> friendsMap;

		public class ImageBean {

			public String parentName;
			public String path;

			public String contentType;
			public long size = 0;

		}
	}

	public class LocalStatus {
		public boolean isModified = false;
		public String thisActivityName = "NONE";
		public String thisActivityStatus = "";

		public String debugMode = "NONE";// NONE

		public LocalData localData;

		public class LocalData {
			public boolean isModified = true;
			public ArrayList<ImageBean> prepareUploadImagesList = new ArrayList<ImageBean>();
			public ArrayList<ImageBean> prepareDownloadImagesList = new ArrayList<ImageBean>();

			public String currentSelectedGroup = "";
			public String currentSelectedSquare = "";

			public Map<String, String> notSentMessagesMap = new HashMap<String, String>();

		}
	}

	public class UserInformation {
		public boolean isModified = false;
		public User currentUser = new User();

		public class User {
			public int id = 0;
			public String sex = "";
			public String age;
			public String phone = "";
			public String nickName = "";
			public String mainBusiness = "";
			public String head = "Head";
			public String accessKey = "";
			public String flag = "none";

			public String lastLoginTime = "";
			public String longitude = "";
			public String latitude = "";
			public String createTime = "";
			public String userBackground = "";

			public List<String> faceList = new ArrayList<String>();
		}
	}

	public class Relationship {
		public boolean isModified = false;

		public List<String> fans = new ArrayList<String>();
		public List<String> attentions = new ArrayList<String>();
		public List<String> friends = new ArrayList<String>();
		public Map<String, Friend> friendsMap = new HashMap<String, Friend>();

		public List<String> groups = new ArrayList<String>();
		public List<String> createdGroups = new ArrayList<String>();
		public List<String> joinedGroups = new ArrayList<String>();
		public Map<String, Group> groupsMap = new HashMap<String, Group>();

		public class Friend {
			public int id = 0;
			public int age = 0;
			public int distance = 0;
			public String sex = "";
			public String phone = "";
			public String nickName = "";
			public String mainBusiness = "";
			public String head = "Head";
			public String friendStatus = "";
			public String addMessage = "";
			public boolean temp = true;
			public int notReadMessagesCount = 0;
			public String longitude = "";
			public String latitude = "";
			public String alias = "";
			public String lastLoginTime = "";
			public String createTime = "";
			public String userBackground = "";
			public boolean notice = true;

			public List<String> groups = new ArrayList<String>();
			// groups
		}

		public class Group {
			public boolean notification = true;
			public int gid = 0;
			public String icon = "";
			public String create = "";
			public String createTime = "";
			public String name = "";
			public int notReadMessagesCount;
			public int distance = 0;
			public String longitude = "";
			public String latitude = "";
			public String description = "";
			public long anonymityTime = 0;
			public List<String> members = new ArrayList<String>();
		}
	}

	public class Messages {
		public boolean isModified = false;
		public Map<String, ArrayList<Message>> messageMap = new HashMap<String, ArrayList<Message>>();

		public List<String> messagesOrder = new ArrayList<String>();

		public class Message {
			public int type;
			public String time = "";
			public String sendType = "";
			public String gid = "";
			public String status = "";
			public String phone = "";
			public String nickName = "";
			public String sex = "";
			public String contentType = "";
			public String content = "";
			public String phoneto = "";
			public boolean anonymity = false;
		}
	}

	public class Event {
		public boolean isModified = false;

		public boolean groupNotReadMessage = false;
		public List<String> groupEvents = new ArrayList<String>();
		public Map<String, EventMessage> groupEventsMap = new HashMap<String, EventMessage>();

		public boolean userNotReadMessage = false;
		public List<String> userEvents = new ArrayList<String>();
		public Map<String, EventMessage> userEventsMap = new HashMap<String, EventMessage>();

		public class EventMessage {
			public String eid;
			public String gid;
			public String type;
			public String phone;
			public String phoneTo;
			public String time;
			public String status;// waiting success
			public String content;
		}
	}
}