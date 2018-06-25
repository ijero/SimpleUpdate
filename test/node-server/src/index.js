const express = require('express');
const app = express();
const os = require('os'), iptable = {}, ifaces = os.networkInterfaces();
app.get('/', function (req, res) {
    res.json({
        versionCode: 2000,
        versionName: "v2.0.0",
        versionMessage: "1. 修复一些bug\n2. 新增一些功能",
        downloadUrl: ""
    });
});


const server = app.listen(3000, function () {
    const host = server.address().address;
    const port = server.address().port;
    
    // console.log('the ip address : ' + getIPAddress());
    console.log('Example app listening at http://%s:%s', host, port);
});