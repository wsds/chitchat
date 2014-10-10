var verifyEmpty = {};
verifyEmpty.verifyEmpty = function (data, arrObj, response) {
    if (JSON.stringify(data) == "{}") {
        return verifyEmpty.verifyResponse(response);
    } else {
        for (var i = 0; i < arrObj.length; i++) {
            var it = arrObj[i];
//            console.log(it + "-");
            if (it == undefined || it == "" || it == null || it == "null" || it == "undefined") {// || it.indexOf(" ") != -1
                return verifyEmpty.verifyResponse(response);
                break;
            }
        }
        return true;
    }
}
verifyEmpty.verifyResponse = function (response) {
    response.write(JSON.stringify({
        "提示信息": "请求失败",
        "失败原因": "数据不完整"
    }));
    response.end();
    return false;
}

module.exports = verifyEmpty;