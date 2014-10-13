package com.open.chitchat.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {

	public static Data data;

	public static Data getInstance() {
		if (data == null) {
			data = new Data();
		}
		return data;
	}

	public BaseData baseData = new BaseData();
	public TempData tempData = new TempData();
	public UserInformation userInformation;
	public Relationship relationship;
	public Messages messages;
	public Event event;

	public class BaseData {
		public boolean notification;

		public float screenWidth;
		public float screenHeight;
		public float appHeight;
		public float stateBar;
		public float density;

	}

	public class TempData {

	}

	public class UserInformation {
		public User currentUser;

		public class User {
			public int id;
			public String sex = "";
			public String age;
			public String phone = "";
			public String nickName = "";
			public String mainBusiness = "";
			public String head = "Head";
			public String accessKey = "";
			public String flag = "none";

			public String lastlogintime;
			public String longitude;
			public String latitude;

		}
	}

	public class Relationship {

		public List<String> fans = new ArrayList<String>();
		public List<String> attentions = new ArrayList<String>();
		public Map<String, Friend> friendsMap = new HashMap<String, Friend>();

		public List<String> groups = new ArrayList<String>();
		public Map<String, Group> groupsMap = new HashMap<String, Group>();

		public class Friend {
			public int id;
			public int age;
			public int distance;
			public String sex = "";
			public String phone = "";
			public String nickName = "";
			public String mainBusiness = "";
			public String head = "Head";
			public String friendStatus = "";
			public String addMessage = "";
			public boolean temp;
			public int notReadMessagesCount;
			public String longitude;
			public String latitude;
			public String alias = "";
			public String lastlogintime;
			// groups
		}

		public class Group {
			public boolean notification;
			public int gid;
			public String icon = "";
			public String create = "";
			public String name = "";
			public int notReadMessagesCount;
			public int distance;
			public String longitude;
			public String latitude;
			public String description;

			public List<String> members = new ArrayList<String>();
		}
	}

	public class Messages {

		public Map<String, ArrayList<Message>> messageMap = new HashMap<String, ArrayList<Message>>();

		public List<String> messagesOrder = new ArrayList<String>();

		public class Message {
			public int type;
			public String time;
			public String sendType;
			public String gid;
			public String status;
			public String phone;
			public String nickName;
			public String contentType;
			public String content;
			public String phoneto;
		}
	}

	public class Event {
		public List<String> groupEvents = new ArrayList<String>();

		public Map<String, EventMessage> groupEventsMap = new HashMap<String, EventMessage>();

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