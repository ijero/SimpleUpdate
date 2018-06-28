const express = require('express');
const app = express();

app.get('/update/check', function (req, res) {
    res.json({
        versionCode: 2,
        versionName: "v0.0.2",
        versionMessage: "v0.0.2版本更新内容：\n1. 修复一些bug\n2. 新增一些功能",
        downloadUrl: "http://192.168.199.134:3000/apk/v0.0.2.apk"
    });
});

app.use(express.static('static'));

const server = app.listen(3000, function () {
    const host = server.address().address;
    const port = server.address().port;

    console.log('Example app listening at http://%s:%s', host, port);
});