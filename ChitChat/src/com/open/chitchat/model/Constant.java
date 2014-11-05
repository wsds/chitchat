package com.open.chitchat.model;

import com.open.chitchat.R;

public class Constant {

	public static final double EARTH_RADIUS = 6378137.0;
	public static final double DEF_PI = 3.14159265359; // PI
	public static final double DEF_2PI = 6.28318530712; // 2*PI
	public static final double DEF_PI180 = 0.01745329252; // PI/180.0
	public static final double DEF_R = 6370693.5;

	// message sendType
	public static final int MESSAGE_TYPE_SEND = 0x01;
	public static final int MESSAGE_TYPE_RECEIVE = 0x02;
	// handler
	public static final int HANDLER_CHAT_NOTIFY = 0x11;
	// tableId
	public static final String ACCOUNTTABLEID = "53eacbe4e4b0693fbf5fd13b";
	public static final String GROUPTABLEID = "53eacbb9e4b0693fbf5fd0f6";
	public static final String SQUARETABLEID = "54101cade4b0dfd37f863ace";
	// lbs key
	public static final String LBS_KSY = "f348dbbed9beced691348793418616d9";// old
	// 32b48639b260edd1916960614151eec3
	public static final String LBS_SAVE_KSY = "0cd819a62c50d40b75a73f66cb14aa06";

	// wechat id
	public static final String WECHAT_ADDID = "wxbbf27e2f87ef9083";

	public static final String[] faceNames = { "[微笑]", "[撇嘴]", "[色]", "[发呆]", "[得意]", "[流泪]", "[害羞]", "[闭嘴]", "[睡]", "[大哭]", "[尴尬]", "[发怒]", "[调皮]", "[呲牙]", "[惊讶]", "[难过]", "[酷]", "[冷汗]", "[抓狂]", "[吐]", "[偷笑]", "[可爱]", "[白眼]", "[傲慢]", "[饥饿]", "[困]", "[惊恐]", "[流汗", "[憨笑]", "[大兵]", "[奋斗]", "[咒骂]", "[疑问]", "[嘘]", "[晕]", "折磨]", "[衰]", "[骷髅]", "[敲打]", "[再见]", "[擦汗]", "[抠鼻]", "[鼓掌]", "[糗大了]", "[坏笑]", "[左哼哼]", "[右哼哼]", "[哈欠]", "[鄙视]", "[委屈]", "[快哭了]", "[阴险]", "[亲亲]", "[吓]", "[可怜]", "[菜刀]", "[西瓜]", "[啤酒]", "[篮球]", "[乒乓]", "[咖啡]", "[饭]", "[猪头]", "[玫瑰]", "[凋谢]", "[示爱]", "[爱心]", "[心碎]", "[蛋糕]", "[闪电]", "[炸弹]", "[刀]", "[足球]", "[瓢虫]", "[便便]", "[月亮]", "[太阳]", "[礼物]", "[拥抱]", "[强", "[弱]", "[握手]", "[胜利]", "[抱拳]", "[勾引]", "[拳头]", "[差劲]", "[爱你]", "[NO]", "[OK]", "[爱情]", "[飞吻]", "[跳跳]", "[发抖]", "[怄火]",
			"[转圈]", "[磕头]", "[回头]", "[跳绳]", "[挥手]", "[激动]", "[街舞]", "[献吻]", "[左太极]", "[右太极]" };

	public static final Integer[] EMOJIS = { R.drawable.e0, R.drawable.e1, R.drawable.e2, R.drawable.e3, R.drawable.e4, R.drawable.e5, R.drawable.e6, R.drawable.e7, R.drawable.e8, R.drawable.e9, R.drawable.e10, R.drawable.e11, R.drawable.e12, R.drawable.e13, R.drawable.e14, R.drawable.e15, R.drawable.e16, R.drawable.e17, R.drawable.e18, R.drawable.e19, R.drawable.e20, R.drawable.e21, R.drawable.e22, R.drawable.e23, R.drawable.e24, R.drawable.e25, R.drawable.e26, R.drawable.e27, R.drawable.e28, R.drawable.e29, R.drawable.e30, R.drawable.e31, R.drawable.e32, R.drawable.e33, R.drawable.e34, R.drawable.e35, R.drawable.e36, R.drawable.e37, R.drawable.e38, R.drawable.e39, R.drawable.e40, R.drawable.e41, R.drawable.e42, R.drawable.e43, R.drawable.e44, R.drawable.e45, R.drawable.e46,
			R.drawable.e47, R.drawable.e48, R.drawable.e49, R.drawable.e50, R.drawable.e51, R.drawable.e52, R.drawable.e53, R.drawable.e54, R.drawable.e55, R.drawable.e56, R.drawable.e57, R.drawable.e58, R.drawable.e59, R.drawable.e60, R.drawable.e61, R.drawable.e62, R.drawable.e63, R.drawable.e64, R.drawable.e65, R.drawable.e66, R.drawable.e67, R.drawable.e68, R.drawable.e69, R.drawable.e70, R.drawable.e71, R.drawable.e72, R.drawable.e73, R.drawable.e74, R.drawable.e75 };
	public static final Integer[] BIG_EMOJIS = { R.drawable.e0_big, R.drawable.e1_big, R.drawable.e2_big, R.drawable.e3_big, R.drawable.e4_big, R.drawable.e5_big, R.drawable.e6_big, R.drawable.e7_big, R.drawable.e8_big, R.drawable.e9_big, R.drawable.e10_big, R.drawable.e11_big, R.drawable.e12_big, R.drawable.e13_big, R.drawable.e14_big, R.drawable.e15_big, R.drawable.e16_big, R.drawable.e17_big, R.drawable.e18_big, R.drawable.e19_big, R.drawable.e20_big, R.drawable.e21_big, R.drawable.e22_big, R.drawable.e23_big, R.drawable.e24_big, R.drawable.e25_big, R.drawable.e26_big, R.drawable.e27_big, R.drawable.e28_big, R.drawable.e29_big, R.drawable.e30_big, R.drawable.e31_big, R.drawable.e32_big, R.drawable.e33_big, R.drawable.e34_big, R.drawable.e35_big, R.drawable.e36_big, R.drawable.e37_big,
			R.drawable.e38_big, R.drawable.e39_big, R.drawable.e40_big, R.drawable.e41_big, R.drawable.e42_big, R.drawable.e43_big, R.drawable.e44_big, R.drawable.e45_big, R.drawable.e46_big, R.drawable.e47_big, R.drawable.e48_big, R.drawable.e49_big, R.drawable.e50_big, R.drawable.e51_big, R.drawable.e52_big, R.drawable.e53_big, R.drawable.e54_big, R.drawable.e55_big, R.drawable.e56_big, R.drawable.e57_big, R.drawable.e58_big, R.drawable.e59_big, R.drawable.e60_big, R.drawable.e61_big, R.drawable.e62_big, R.drawable.e63_big, R.drawable.e64_big, R.drawable.e65_big, R.drawable.e66_big, R.drawable.e67_big, R.drawable.e68_big, R.drawable.e69_big, R.drawable.e70_big, R.drawable.e71_big, R.drawable.e72_big, R.drawable.e73_big, R.drawable.e74_big, R.drawable.e75_big };

}
