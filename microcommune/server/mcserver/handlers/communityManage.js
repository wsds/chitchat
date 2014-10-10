var serverSetting = root.globaldata.serverSetting;
var communityManage = {};
var neo4j = require('neo4j');
var db = new neo4j.GraphDatabase(serverSetting.neo4jUrl);
var ajax = require('../../lbsserver/lib/ajax.js');

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *URL：/api2/community / find
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

communityManage.find = function (data, response) {
    response.asynchronous = 1;
    var radius = 10000;
    var longitude0 = data.longitude;
    var latitude0 = data.latitude;
    var longitude = 0;
    var latitude = 0;
    try {
        if (longitude0 == "" || latitude0 == "" || longitude0 == undefined || latitude0 == undefined) {
            throw "longitude and latitude of undefined";
        }
        longitude = parseFloat(longitude0);
        latitude = parseFloat(latitude0);
        nearbyGroups(longitude, latitude);
    } catch (e) {
        response.write(JSON.stringify({
            "提示信息": "获取社区失败",
            "失败原因": "参数格式错误"
        }));
        response.end();
        console.error(e);
        return;
    }

    function nearbyGroups(longitude, latitude) {
        ajax.ajax({
            type: "GET",
            url: serverSetting.LBS.NEARBY,
            data: {
                geotable_id: 50513,
                ak: serverSetting.LBS.LBS_AK,
                q: "",
                coord_type: 3,
                location: longitude + "," + latitude,//"116.25,40.25"
                radius: radius, // 默认1000 M,
                sortby: "distance:1",
                tags: "community",
                gtype: "community",
                page_index: 0,
                page_size: 10
            },
            success: function (data) {
                var poisObj = JSON.parse(data);
                if (poisObj.status == 0) {
                    var contents = poisObj.contents;
                    if (contents.length == 0) {
                        radius += 10000;
                        nearbyGroups(longitude, latitude);
                    } else {
                        var poiObj = contents[0];
                        var location = {
                            longitude: (poiObj.location)[0],
                            latitude: (poiObj.location)[1]
                        };
                        var group = {
                            gid: poiObj.gid,
                            name: poiObj.name,
                            description: poiObj.description,
                            gtype: poiObj.gtype,
                            location: location
                        };
                        ajax.ajax({
                            type: "POST",
                            url: "http://127.0.0.1:8078/api2/square/getonlinecount?",
                            data: {
                                phone: "121",
                                accessKey: "lejoying",
                                gid: poiObj.gid
                            },
                            success: function (data1) {
                                response.write(JSON.stringify({
                                    "提示信息": "获取社区成功",
                                    group: group,
                                    onlinecount: (JSON.parse(data1)).onlinecount
                                }));
                                response.end();
                            },
                            error: function () {
                                response.write(JSON.stringify({
                                    "提示信息": "获取社区失败",
                                    "失败原因": "数据异常"
                                }));
                                response.end();
                            }
                        });
                    }

                } else {
                    console.log(data);
                    response.write(JSON.stringify({
                        "提示信息": "获取社区失败",
                        "失败原因": "数据异常"
                    }));
                    response.end();
                }
            }
        });
    }
}


communityManage.add = function (data, response) {
    response.asynchronous = 1;
    var phone = data.phone;
    var accessKey = data.accessKey;
    var name = data.name;
    var description = data.description;
    var longitude = data.longitude;
    var latitude = data.latitude;
    var location = {
        longitude: longitude,
        latitude: latitude
    };
    var community = {
        "name": name,
        "description": description,
        "location": JSON.stringify(location)
    };
    var query = [
        'CREATE (community:Community{community})',
        'SET community.cid=ID(community)',
        'RETURN community'
    ].join('\n');

    var params = {
        community: community
    };

    db.query(query, params, function (error, results) {
        if (error) {
            response.write(JSON.stringify({
                "提示信息": "创建服务站失败",
                "失败原因": "数据异常"
            }));
            response.end();
            console.log(error);
            return;
        } else {
            var communityData = results.pop().community.data;
            response.write(JSON.stringify({
                "提示信息": "创建服务站成功",
                community: communityData
            }));
            response.end();
        }
    });
}
/***************************************
 *     URL：/api2/community/getall
 ***************************************//*

 communityManage.getall = function(data, response){
 response.asynchronous = 1;
 var query = [
 'MATCH (community:Community)',
 'RETURN community'
 ].join('\n');

 var params = {};

 db.query(query, params, function(error,results){
 if(error){
 console.log(error);
 return;
 }else{
 if(results.length == 0){
 response.write(JSON.stringify({
 "提示信息": "获取所有社区失败",
 "失败原因": "无社区数据"
 }));
 response.end();
 }else{
 var communities = [];
 for(var index in results){
 var it = results[index].community.data;
 communities.push(it);
 }
 response.write(JSON.stringify({
 "提示信息": "获取所有社区成功",
 "communities": communities
 }));
 response.end();
 }
 }
 });
 }
 */
/***************************************
 *     URL：/api2/community/find
 ***************************************/
/*communityManage.find = function (data, response) {
 response.asynchronous = 1;
 var phone = data.phone;
 var accessKey = data.accessKey;
 var longitude = parseFloat(data.longitude);
 var latitude = parseFloat(data.latitude);
 console.log(longitude + "---" + latitude);
 findCommunity(longitude, latitude);

 function findCommunity(longitude, latitude) {
 var query = [
 'MATCH (community:Community)',
 'RETURN community'
 ].join('\n');
 var params = {};
 db.query(query, params, function (error, results) {
 if (error) {
 response.write(JSON.stringify({
 "提示信息": "获取失败",
 "失败原因": "数据异常"
 }));
 response.end();
 console.log(error);
 return;
 } else {
 var community = {};
 var num = -1;
 for (var index in results) {
 var it = results[index].community.data;
 var location = JSON.parse(it.location);
 var longitude2 = parseFloat(location.longitude);
 var latitude2 = parseFloat(location.latitude);
 if (num == -1) {
 num = (longitude - longitude2) * (longitude - longitude2) + (latitude - latitude2 ) * (latitude - latitude2);
 community = it;
 } else {
 var num1 = (longitude - longitude2) * (longitude - longitude2) + (latitude - latitude2) * (latitude - latitude2);
 if (num1 < num) {
 num = num1;
 community = it;
 }
 }
 }
 var agent = {
 uid: 110,
 nickName: "站长",
 phone: "110",
 mainBusiness: "管理社区"
 };
 community.agent = agent;
 delete community.location;
 response.write(JSON.stringify({
 "提示信息": "获取成功",
 community: community
 }));
 response.end();
 }
 });
 }
 }*/
/***************************************
 *     URL：/api2/community/join
 ***************************************/
communityManage.join = function (data, response) {
    response.asynchronous = 1;
    var phone = data.phone;
    var accessKey = data.accessKey;
    var cid = data.cid;
    console.log(phone);
    joinCommunityNode();

    function joinCommunityNode() {
        var query = [
            'START community=node({cid})',
            'MATCH (account:Account)',
            'WHERE account.phone={phone}',
            'CREATE UNIQUE account-[r:JOIN]->community',
            'RETURN r'
        ].join('\n');

        var params = {
            cid: parseInt(cid),
            phone: phone
        };
        db.query(query, params, function (error, results) {
            if (error) {
                response.write(JSON.stringify({
                    "提示信息": "加入失败",
                    "失败原因": "数据异常"
                }));
                response.end();
                console.error(error);
                return;
            } else if (results.length > 0) {
                console.log("加入成功---" + results.length);
                response.write(JSON.stringify({
                    "提示信息": "加入成功"
                }));
                response.end();
            } else {
                response.write(JSON.stringify({
                    "提示信息": "加入失败",
                    "失败原因": "数据异常"
                }));
                response.end();
            }
        });
    }
}
/***************************************
 *     URL：/api2/community/unjoin
 ***************************************/
communityManage.unjoin = function (data, response) {
    response.asynchronous = 1;
    var cid = data.cid;
    var phone = data.phone;
    var accessKey = data.accessKey;
    unJoinCommunityNode();

    function unJoinCommunityNode() {
        var query = [
            'MATCH (account:Account)-[r:JOIN]->(community:Community)',
            'WHERE account.phone={phone} AND community.cid={cid}',
            'DELETE r'
        ].join('\n');

        var params = {
            phone: phone,
            cid: parseInt(cid)
        };
        db.query(query, params, function (error, results) {
            if (error) {
                response.write(JSON.stringify({
                    "提示信息": "退出失败",
                    "失败原因": "数据异常"
                }));
                response.end();
                console.error(error);
                return;
            } else {
                console.log("退出成功---");
                response.write(JSON.stringify({
                    "提示信息": "退出成功"
                }));
                response.end();
            }
        });
    }
}
/***************************************
 *     URL：/api2/community/getcommunities
 ***************************************/
communityManage.getcommunities = function (data, response) {
    response.asynchronous = 1;
    var phone = data.phone;
    var accessKey = data.accessKey;
    var query = [
        'MATCH (account:Account)-[r:JOIN]->(community:Community)',
        'WHERE account.phone={phone}',
        'RETURN community'
    ].join('\n');
    var params = {
        phone: phone
    };
    db.query(query, params, function (error, results) {
        if (error) {
            response.write(JSON.stringify({
                "提示信息": "获取社区失败",
                "错误原因": "数据异常"
            }));
            response.end();
            console.log(error);
            return;
        } else {
            console.log("获取社区成功---" + results.length);
            var communities = [];
            var i = 0;
            var agent = {
                uid: 110,
                nickName: "站长",
                phone: "110",
                mainBusiness: "管理社区"
            };
            for (var index in results) {
                i++;
                var it = results[index].community.data;
                it.agent = agent;
                delete it.location;
                communities.push(it);
            }
            response.write(JSON.stringify({
                "提示信息": "获取社区成功",
                "communities": communities
            }));
            response.end();
        }
    });
}
/***************************************
 *     URL：/api2/community/getcommunityfriends
 ***************************************/

communityManage.getcommunityfriends = function (data, response) {
    response.asynchronous = 1;
    var communityName = data.name;
    var phone = data.phone;
    var accessKey = data.accessKey;
    var query = [
        'MATCH (account:Account)-->(community:Community)',
        'WHERE community.name={communityName}',
        'RETURN account'
    ].join('\n');
    var params = {
        communityName: communityName
    };
    db.query(query, params, function (error, results) {
        if (error) {
            response.write(JSON.stringify({
                "提示信息": "获取社区好友失败",
                "失败原因": "数据异常"
            }));
            response.end();
            console.log(error);
            return;
        } else if (results.length > 0) {
            var accounts = [];
            var count = 0;
            for (var index in results) {
                var it = results[index].account.data;
                var community = {
                    cid: it.cid,
                    name: it.name,
                    description: it.description
                };
                accounts.push(community);
                if (count == results.length) {
                    response.write(JSON.stringify({
                        "提示信息": "获取社区好友成功",
                        "accounts": accounts
                    }));
                    response.end();
                }
            }
        } else {
            response.write(JSON.stringify({
                "提示信息": "获取社区好友成功",
                "accounts": []
            }));
            response.end();
        }
    });
}
module.exports = communityManage;