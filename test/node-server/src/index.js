const express = require('express');
const app = express();

app.get('/update/check', function (req, res) {
    res.json({
        versionCode: 2000,
        versionName: "v2.0.0",
        versionMessage: "1. 修复一些bug\n2. 新增一些功能",
        downloadUrl: "http://192.168.199.134:3000/apk/v2.0.apk"
    });
});

app.use(express.static('static'));

const server = app.listen(3000, function () {
    const host = server.address().address;
    const port = server.address().port;

    console.log('Example app listening at http://%s:%s', host, port);
});