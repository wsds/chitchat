package com.open.chitchat.view;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.open.chitchat.BusinessActivity;
import com.open.chitchat.R;
import com.open.chitchat.controller.BusinessController;
import com.open.chitchat.controller.BusinessController.Status;
import com.open.chitchat.model.Data.Relationship.Friend;
import com.open.chitchat.model.Data.Relationship.Group;
import com.open.chitchat.model.Data.UserInformation.User;
import com.open.chitchat.utils.BaseDataUtils;
import com.open.chitchat.utils.DateUtil;
import com.open.chitchat.utils.DistanceUtils;

public class BusinessView {
	public BusinessActivity thisActivity;
	public BusinessView thisView;
	public BusinessController thisController;

	public LayoutInflater mInflater;

	public RelativeLayout rightContainer;
	public View detailsOneIn, detailsTwoIn, detailsThreeIn, detailsFourIn, descriptionIn, attention, mPopupWindowView, popLayoutOne, popLayoutTwo, popLayoutThree, popLayoutFour, backView, ageLayout, chatLayout, distanceLayout, groupLayout, detailsOneLayout, detailsTwoLayout, detailsThreelayout, detailsFourlayout, descriptionlayout, memberLayout, members, myGroup, chat;
	public TextView textOne, textTwo, textThree, textFour, titleText, age, distance, lastLoginTime, memberCounts, groupDistance, detailsOneTitle, detailsOneContent, detailsTwoTitle, detailsTwoContent, detailsThreeTitle, detailsThreeContent, detailsFourTitle, detailsFourContent, descriptionTitle, descriptionContent, memberTitle, myGroupTitle, chatText;
	public ImageView imageOne, imageTwo, imageThree, imageFour, head, chatImage, titleImage;
	public ListView myGroupList;

	public PopupWindow mPopupWindow;

	private DisplayImageOptions headOptions;

	public BusinessView(BusinessActivity thisActivity) {
		this.thisView = this;
		this.thisActivity = thisActivity;
	}

	public void onCreate() {
		mInflater = thisActivity.getLayoutInflater();
		thisActivity.setContentView(R.layout.activity_business);
		rightContainer = (RelativeLayout) thisActivity.findViewById(R.id.rightContainer);
		backView = thisActivity.findViewById(R.id.backView);
		chatLayout = thisActivity.findViewById(R.id.chatLayout);
		attention = thisActivity.findViewById(R.id.attention);
		ageLayout = thisActivity.findViewById(R.id.ageLayout);
		distanceLayout = thisActivity.findViewById(R.id.distanceLayout);
		groupLayout = thisActivity.findViewById(R.id.groupLayout);
		detailsOneLayout = thisActivity.findViewById(R.id.detailsOneLayout);
		detailsTwoLayout = thisActivity.findViewById(R.id.detailsTwoLayout);
		detailsThreelayout = thisActivity.findViewById(R.id.detailsThreelayout);
		detailsFourlayout = thisActivity.findViewById(R.id.detailsFourlayout);
		descriptionlayout = thisActivity.findViewById(R.id.descriptionlayout);
		memberLayout = thisActivity.findViewById(R.id.memberLayout);
		members = thisActivity.findViewById(R.id.members);
		myGroup = thisActivity.findViewById(R.id.myGroup);
		chat = thisActivity.findViewById(R.id.chat);
		detailsOneIn = thisActivity.findViewById(R.id.detailsOneIn);
		detailsTwoIn = thisActivity.findViewById(R.id.detailsTwoIn);
		detailsThreeIn = thisActivity.findViewById(R.id.detailsThreeIn);
		detailsFourIn = thisActivity.findViewById(R.id.detailsFourIn);
		descriptionIn = thisActivity.findViewById(R.id.descriptionIn);
		titleText = (TextView) thisActivity.findViewById(R.id.titleText);
		age = (TextView) thisActivity.findViewById(R.id.age);
		distance = (TextView) thisActivity.findViewById(R.id.distance);
		lastLoginTime = (TextView) thisActivity.findViewById(R.id.lastLoginTime);
		memberCounts = (TextView) thisActivity.findViewById(R.id.memberCounts);
		groupDistance = (TextView) thisActivity.findViewById(R.id.groupDistance);
		detailsOneTitle = (TextView) thisActivity.findViewById(R.id.detailsOnetitle);
		detailsOneContent = (TextView) thisActivity.findViewById(R.id.detailsOneContent);
		detailsTwoTitle = (TextView) thisActivity.findViewById(R.id.detailsTwoTitle);
		detailsTwoContent = (TextView) thisActivity.findViewById(R.id.detailsTwoContent);
		detailsThreeTitle = (TextView) thisActivity.findViewById(R.id.detailsThreetitle);
		detailsThreeContent = (TextView) thisActivity.findViewById(R.id.detailsThreeContent);
		detailsFourTitle = (TextView) thisActivity.findViewById(R.id.detailsFourTitle);
		detailsFourContent = (TextView) thisActivity.findViewById(R.id.detailsFourContent);
		descriptionTitle = (TextView) thisActivity.findViewById(R.id.descriptionTitle);
		descriptionContent = (TextView) thisActivity.findViewById(R.id.descriptionContent);
		memberTitle = (TextView) thisActivity.findViewById(R.id.memberTitle);
		myGroupTitle = (TextView) thisActivity.findViewById(R.id.myGroupTitle);
		chatText = (TextView) thisActivity.findViewById(R.id.chatText);
		head = (ImageView) thisActivity.findViewById(R.id.head);
		chatImage = (ImageView) thisActivity.findViewById(R.id.chatImage);
		myGroupList = (ListView) thisActivity.findViewById(R.id.myGroupList);

		titleImage = new ImageView(thisActivity);
		titleImage.setImageResource(R.drawable.title_image);
		rightContainer.addView(titleImage);

		mPopupWindowView = mInflater.inflate(R.layout.business_pop_view, null);
		popLayoutOne = mPopupWindowView.findViewById(R.id.popLayoutOne);
		popLayoutTwo = mPopupWindowView.findViewById(R.id.popLayoutTwo);
		popLayoutThree = mPopupWindowView.findViewById(R.id.popLayoutThree);
		popLayoutFour = mPopupWindowView.findViewById(R.id.popLayoutFour);
		textOne = (TextView) mPopupWindowView.findViewById(R.id.textOne);
		textTwo = (TextView) mPopupWindowView.findViewById(R.id.textTwo);
		textThree = (TextView) mPopupWindowView.findViewById(R.id.textThree);
		textFour = (TextView) mPopupWindowView.findViewById(R.id.textFour);
		imageOne = (ImageView) mPopupWindowView.findViewById(R.id.imageOne);
		imageTwo = (ImageView) mPopupWindowView.findViewById(R.id.imageTwo);
		imageThree = (ImageView) mPopupWindowView.findViewById(R.id.imageThree);
		imageFour = (ImageView) mPopupWindowView.findViewById(R.id.imageFour);
		mPopupWindowView.setFocusableInTouchMode(true);
		mPopupWindow = new PopupWindow(mPopupWindowView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setOutsideTouchable(true);

		head.getLayoutParams().height = (int) thisController.data.baseData.screenWidth;

		headOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
	}

	public void fillData() {
		if (thisController.status.equals(Status.SELF)) {
			User user = thisController.data.userInformation.currentUser;
			thisController.fileHandlers.getHeadImage(user.head, head, headOptions);
			if (BaseDataUtils.determineSex(user.sex)) {
				age.setBackgroundResource(R.drawable.personalinfo_male);
			} else {
				age.setBackgroundResource(R.drawable.personalinfo_female);
			}
			age.setText(user.age);
			titleText.setText(thisActivity.getString(R.string.userInfomatin));
			detailsOneTitle.setText(thisActivity.getString(R.string.showName));
			detailsOneTitle.setText(thisActivity.getString(R.string.registerTime));
			detailsOneContent.setText(user.nickName);
			detailsTwoContent.setText(DateUtil.formatYearMonthDay2(user.createTime));
			descriptionTitle.setText(thisActivity.getString(R.string.mainBusiness));
			descriptionContent.setText(user.mainBusiness);
			imageOne.setImageResource(R.drawable.pop_send_card);
			textOne.setText(thisActivity.getString(R.string.sendUserCard));

			detailsOneIn.setVisibility(View.VISIBLE);
			detailsTwoIn.setVisibility(View.VISIBLE);
			descriptionIn.setVisibility(View.VISIBLE);
			detailsThreelayout.setVisibility(View.GONE);
			chatLayout.setVisibility(View.GONE);
			detailsFourlayout.setVisibility(View.GONE);
			groupLayout.setVisibility(View.GONE);
			myGroup.setVisibility(View.GONE);
			memberLayout.setVisibility(View.GONE);
			popLayoutTwo.setVisibility(View.GONE);
			popLayoutThree.setVisibility(View.GONE);
			popLayoutFour.setVisibility(View.GONE);
		} else if (thisController.status.equals(Status.FRIEND)) {
			Friend friend = thisController.data.relationship.friendsMap.get(thisController.key);
			User user = thisController.data.userInformation.currentUser;
			thisController.fileHandlers.getHeadImage(friend.head, head, headOptions);
			titleText.setText(BaseDataUtils.generateUserName(friend.nickName, friend.alias));
			age.setText(String.valueOf(friend.age));
			if (BaseDataUtils.determineSex(friend.sex)) {
				age.setBackgroundResource(R.drawable.personalinfo_male);
				chat.setBackgroundResource(R.drawable.bg_round_azure);
			} else {
				age.setBackgroundResource(R.drawable.personalinfo_female);
				chat.setBackgroundResource(R.drawable.bg_round_pink);
			}
			distance.setText(DistanceUtils.getDistance(user.longitude, user.latitude, friend.longitude, friend.latitude));
			lastLoginTime.setText(DateUtil.formatYearMonthDay2(friend.lastLoginTime));
			detailsOneTitle.setText(thisActivity.getString(R.string.chitChatNum));
			detailsTwoTitle.setText(thisActivity.getString(R.string.registerTime));
			detailsThreeTitle.setText(thisActivity.getString(R.string.relation));
			detailsOneContent.setText(String.valueOf(friend.id));
			detailsTwoContent.setText(DateUtil.formatYearMonthDay2(friend.createTime));
			detailsThreeContent.setText(thisActivity.getString(R.string.friends));
			descriptionTitle.setText(thisActivity.getString(R.string.mainBusiness));
			descriptionContent.setText(friend.mainBusiness);

			textOne.setText(thisActivity.getString(R.string.cancelAttention));
			textTwo.setText(thisActivity.getString(R.string.sendUserCard));
			textThree.setText(thisActivity.getString(R.string.blackList));
			textFour.setText(thisActivity.getString(R.string.blackListAndReport));
			imageOne.setImageResource(R.drawable.pop_attention);
			imageTwo.setImageResource(R.drawable.pop_send_card);
			imageThree.setImageResource(R.drawable.pop_block);
			imageFour.setImageResource(R.drawable.pop_report);
			int count = 0;
			if (friend.groups != null) {
				count = friend.groups.size();
			}
			String groupTitle = thisActivity.getString(R.string.belongGroup) + "(" + count + ")";
			SpannableStringBuilder style = new SpannableStringBuilder(groupTitle);
			style.setSpan(new ForegroundColorSpan(Color.RED), groupTitle.indexOf("(") + 1, groupTitle.indexOf(")"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			myGroupTitle.setText(style);

			chat.setTag(R.id.tag_class, "chat");
			chatText.setText(thisActivity.getString(R.string.personalChat));
			chatImage.setImageResource(R.drawable.icon_chat);

			detailsFourlayout.setVisibility(View.GONE);
			groupLayout.setVisibility(View.GONE);
			memberLayout.setVisibility(View.GONE);
		} else if (thisController.status.equals(Status.ATTENTIONS)) {
			Friend friend = thisController.data.relationship.friendsMap.get(thisController.key);
			User user = thisController.data.userInformation.currentUser;
			thisController.fileHandlers.getHeadImage(friend.head, head, headOptions);
			titleText.setText(BaseDataUtils.generateUserName(friend.nickName, friend.alias));
			age.setText(String.valueOf(friend.age));
			if (BaseDataUtils.determineSex(friend.sex)) {
				age.setBackgroundResource(R.drawable.personalinfo_male);
				chat.setBackgroundResource(R.drawable.bg_round_azure);
			} else {
				age.setBackgroundResource(R.drawable.personalinfo_female);
				chat.setBackgroundResource(R.drawable.bg_round_pink);
			}
			distance.setText(DistanceUtils.getDistance(user.longitude, user.latitude, friend.longitude, friend.latitude));
			lastLoginTime.setText(DateUtil.formatYearMonthDay2(friend.lastLoginTime));

			detailsOneTitle.setText(thisActivity.getString(R.string.chitChatNum));
			detailsTwoTitle.setText(thisActivity.getString(R.string.registerTime));
			detailsThreeTitle.setText(thisActivity.getString(R.string.relation));
			detailsOneContent.setText(String.valueOf(friend.id));
			detailsTwoContent.setText(DateUtil.formatYearMonthDay2(friend.createTime));
			detailsThreeContent.setText(thisActivity.getString(R.string.attention));
			descriptionTitle.setText(thisActivity.getString(R.string.mainBusiness));
			descriptionContent.setText(friend.mainBusiness);

			textOne.setText(thisActivity.getString(R.string.cancelAttention));
			textTwo.setText(thisActivity.getString(R.string.sendUserCard));
			textThree.setText(thisActivity.getString(R.string.blackList));
			textFour.setText(thisActivity.getString(R.string.blackListAndReport));
			imageOne.setImageResource(R.drawable.pop_attention);
			imageTwo.setImageResource(R.drawable.pop_send_card);
			imageThree.setImageResource(R.drawable.pop_block);
			imageFour.setImageResource(R.drawable.pop_report);

			chat.setTag(R.id.tag_class, "chat");
			chatText.setText(thisActivity.getString(R.string.personalChat));
			chatImage.setImageResource(R.drawable.icon_chat);
			int count = 0;
			if (friend.groups != null) {
				count = friend.groups.size();
			}
			String groupTitle = thisActivity.getString(R.string.belongGroup) + "(" + count + ")";
			SpannableStringBuilder style = new SpannableStringBuilder(groupTitle);
			style.setSpan(new ForegroundColorSpan(Color.RED), groupTitle.indexOf("(") + 1, groupTitle.indexOf(")"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			myGroupTitle.setText(style);

			detailsFourlayout.setVisibility(View.GONE);
			groupLayout.setVisibility(View.GONE);
			memberLayout.setVisibility(View.GONE);
		} else if (thisController.status.equals(Status.FANS) || thisController.status.equals(Status.TEMPFRIEND)) {
			Friend friend = thisController.data.relationship.friendsMap.get(thisController.key);
			User user = thisController.data.userInformation.currentUser;

			thisController.fileHandlers.getHeadImage(friend.head, head, headOptions);
			titleText.setText(BaseDataUtils.generateUserName(friend.nickName, friend.alias));
			age.setText(String.valueOf(friend.age));
			if (BaseDataUtils.determineSex(friend.sex)) {
				age.setBackgroundResource(R.drawable.personalinfo_male);
				chat.setBackgroundResource(R.drawable.bg_round_azure);
			} else {
				age.setBackgroundResource(R.drawable.personalinfo_female);
				chat.setBackgroundResource(R.drawable.bg_round_pink);
			}
			distance.setText(DistanceUtils.getDistance(DistanceUtils.getDistance(user.longitude, user.latitude, friend.longitude, friend.latitude)));
			lastLoginTime.setText(DateUtil.formatYearMonthDay2(friend.lastLoginTime));

			detailsOneTitle.setText(thisActivity.getString(R.string.chitChatNum));
			detailsTwoTitle.setText(thisActivity.getString(R.string.registerTime));
			detailsThreeTitle.setText(thisActivity.getString(R.string.relation));
			detailsOneContent.setText(String.valueOf(friend.id));
			detailsTwoContent.setText(DateUtil.formatYearMonthDay2(friend.createTime));
			if (thisController.status.equals(Status.FANS)) {
				detailsThreeContent.setText(thisActivity.getString(R.string.fans));
			} else {
				detailsThreeContent.setText(thisActivity.getString(R.string.stranger));
			}
			descriptionTitle.setText(thisActivity.getString(R.string.mainBusiness));
			descriptionContent.setText(friend.mainBusiness);

			textOne.setText(thisActivity.getString(R.string.attention));
			textTwo.setText(thisActivity.getString(R.string.sendUserCard));
			textThree.setText(thisActivity.getString(R.string.blackList));
			textFour.setText(thisActivity.getString(R.string.blackListAndReport));
			imageOne.setImageResource(R.drawable.pop_attention);
			imageTwo.setImageResource(R.drawable.pop_send_card);
			imageThree.setImageResource(R.drawable.pop_block);
			imageFour.setImageResource(R.drawable.pop_report);

			chat.setTag(R.id.tag_class, "chat");
			chatText.setText(thisActivity.getString(R.string.personalChat));
			chatImage.setImageResource(R.drawable.icon_chat);

			attention.setVisibility(View.VISIBLE);
			detailsFourlayout.setVisibility(View.GONE);
			groupLayout.setVisibility(View.GONE);
			myGroup.setVisibility(View.GONE);
			memberLayout.setVisibility(View.GONE);
		}
		// else if (thisController.status.equals(Status.TEMPFRIEND)) {
		// Friend friend =
		// thisController.data.relationship.friendsMap.get(thisController.key);
		// User user = thisController.data.userInformation.currentUser;
		// businessCard.id = friend.id;
		// businessCard.head = friend.head;
		// businessCard.sex = friend.sex;
		// businessCard.age = friend.age + "";
		// businessCard.distance = DistanceUtils.getDistance(user.longitude,
		// user.latitude, friend.longitude, friend.latitude);
		// businessCard.nickName =
		// BaseDataUtils.generateUserName(friend.nickName, friend.alias);
		// businessCard.mainBusiness = friend.mainBusiness;
		// businessCard.creatTime = friend.createTime;
		// businessCard.detailsOneTitle = "";
		// businessCard.detailsTwoTitle = "";
		// businessCard.detailsThreeTitle = "";
		// businessCard.detailsOneContent = "";
		// businessCard.detailsTwoContent = "";
		// businessCard.detailsThreeContent = "";
		// businessCard.popTextOne = "";
		// businessCard.popTextTwo = "";
		// businessCard.popTextThree = "";
		// businessCard.popTextFour = "";
		// businessCard.popImageOne = 0;
		// businessCard.popImageTwo = 0;
		// businessCard.popImageThree = 0;
		// businessCard.popImageFour = 0;
		// detailsFourlayout.setVisibility(View.GONE);
		// descriptionlayout.setVisibility(View.GONE);
		// groupLayout.setVisibility(View.GONE);
		// memberLayout.setVisibility(View.GONE);
		// myGroup.setVisibility(View.GONE);
		// }
		else if (thisController.status.equals(Status.JOINEDGROUP)) {
			Group group = thisController.data.relationship.groupsMap.get(thisController.key);
			User user = thisController.data.userInformation.currentUser;

			thisController.fileHandlers.getHeadImage(group.icon, head, headOptions);
			titleText.setText(group.name);
			groupDistance.setText(DistanceUtils.getDistance(user.longitude, user.latitude, group.longitude, group.latitude));

			String description = "";
			if (group.description == null || group.description.equals("") || group.description.equals("请输入群组描述信息")) {
				description = "此群组暂无业务";
			} else {
				description = group.description;
			}

			detailsOneTitle.setText(thisActivity.getString(R.string.groupNum));
			detailsTwoTitle.setText(thisActivity.getString(R.string.groupCreator));
			detailsThreeTitle.setText(thisActivity.getString(R.string.createTime));
			detailsOneContent.setText(String.valueOf(group.gid));
			detailsTwoContent.setText(group.create);
			detailsThreeContent.setText(DateUtil.formatYearMonthDay2(group.createTime));
			descriptionTitle.setText(thisActivity.getString(R.string.groupDescription));
			descriptionContent.setText(description);

			textOne.setText(thisActivity.getString(R.string.sendGroupCard));
			textTwo.setText(thisActivity.getString(R.string.share));
			textThree.setText(thisActivity.getString(R.string.setting));
			imageOne.setImageResource(R.drawable.pop_send_card);
			imageTwo.setImageResource(R.drawable.pop_share);
			imageThree.setImageResource(R.drawable.pop_settings);

			String title = thisActivity.getString(R.string.groupMember) + "(" + group.members.size() + ")";
			SpannableStringBuilder style = new SpannableStringBuilder(title);
			style.setSpan(new ForegroundColorSpan(Color.RED), title.indexOf("(") + 1, title.indexOf(")"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			memberTitle.setText(style);

			chat.setBackgroundResource(R.drawable.bg_round_azure);
			chat.setTag(R.id.tag_class, "chat");
			chatText.setText(thisActivity.getString(R.string.chat));
			chatImage.setImageResource(R.drawable.btn_find_off);

			popLayoutFour.setVisibility(View.GONE);
			detailsFourlayout.setVisibility(View.GONE);
			distanceLayout.setVisibility(View.GONE);
			ageLayout.setVisibility(View.GONE);
			myGroup.setVisibility(View.GONE);
		} else if (thisController.status.equals(Status.NOTJOINGROUP)) {
			Group group = thisController.data.relationship.groupsMap.get(thisController.key);
			User user = thisController.data.userInformation.currentUser;

			thisController.fileHandlers.getHeadImage(group.icon, head, headOptions);
			titleText.setText(group.name);
			groupDistance.setText(DistanceUtils.getDistance(user.longitude, user.latitude, group.longitude, group.latitude));
			String description = "";
			if (group.description == null || group.description.equals("") || group.description.equals("请输入群组描述信息")) {
				description = "此群组暂无业务";
			} else {
				description = group.description;
			}

			detailsOneTitle.setText(thisActivity.getString(R.string.groupNum));
			detailsTwoTitle.setText(thisActivity.getString(R.string.groupCreator));
			detailsThreeTitle.setText(thisActivity.getString(R.string.createTime));
			detailsOneContent.setText(String.valueOf(group.gid));
			detailsTwoContent.setText(group.create);
			detailsThreeContent.setText(DateUtil.formatYearMonthDay2(group.createTime));
			descriptionTitle.setText(thisActivity.getString(R.string.groupDescription));
			descriptionContent.setText(description);

			String title = thisActivity.getString(R.string.groupMember) + "(" + group.members.size() + ")";
			SpannableStringBuilder style = new SpannableStringBuilder(title);
			style.setSpan(new ForegroundColorSpan(Color.RED), title.indexOf("(") + 1, title.indexOf(")"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			memberTitle.setText(style);

			chat.setBackgroundResource(R.drawable.bg_round_azure);
			chat.setTag(R.id.tag_class, "joinGroup");
			chatText.setText(thisActivity.getString(R.string.join));
			chatImage.setImageResource(R.drawable.chat_add_off);

			detailsFourlayout.setVisibility(View.GONE);
			distanceLayout.setVisibility(View.GONE);
			ageLayout.setVisibility(View.GONE);
			myGroup.setVisibility(View.GONE);
			titleImage.setVisibility(View.GONE);
		}
	}

	public void changePopMenuView() {
		if (mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		} else {
			if (!thisController.status.equals(Status.NOTJOINGROUP)) {
				mPopupWindow.showAsDropDown(titleImage);
			}
		}
	}

}
