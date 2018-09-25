1. **执行sql语句, 支持同步/异步方式执行**  
请求方式:POST  
请求地址:`/hive/client/execute`  
响应类型:application/json 
数据类型:application/json   
请求参数:   

    参数名 | 是否必须 | 类型 | 描述  | 默认值
    ---- | ----- | --- | --- | ---- |   
    sql | 是 | string | 表示要执行的语句 |
    params | 否 | array | sql语句中要替换的参数 |
    queryId | 否 | string | 指定该查询的查询ID, 若提供此参数则表示异步执行,否则表示同步执行 | 
请求示例1:
```
type: POST
url:http://192.168.0.248:9001/hive/client/execute
body:
{
	"sql":"select count(*) from events_json"
}
```
结果示例1:
```
{
    "result": "[78369]",
    "success": true,
    "message": "ok"
}
```

请求示例2:
```
type: POST
url:http://192.168.0.248:9001/hive/client/execute
body:
{
	"sql":"select count(*) from events_json",
	"params":[],
	"queryId":"3"
}
```
结果示例2:
```
{
    "result": "",
    "success": true,
    "message": "accepted"
}
```

2. **查看执行队列**  
请求方式:GET  
请求地址:`/hive/client/task/queue`  
响应类型:application/json 
数据类型:无   
请求参数: 无   
请求示例:
```
type: GET
url:http://192.168.0.248:9001/hive/client/task/queue
```
结果示例:
```
{
    "taskCount": 1,
    "pendingQueue": [
        {
            "queryId": "3",
            "sql": "select count(*) from events_json",
            "params": []
        }
    ],
    "runningQueue": []
}
```

3. **查看异步执行的sql结果**  
请求方式:GET  
请求地址:`/hive/client/task/result`  
响应类型:application/json 
数据类型:无   
请求参数: 无   
请求示例:
```
type: GET
url:http://192.168.0.248:9001/hive/client/task/result
```
结果示例:
```
{
    "dimData": [
        {
            "queryId": "3",
            "result": "[78369]",
            "status": "success",
            "message": "ok",
            "generateTime": "2018-08-16 17:30:44.951"
        },
        {
            "queryId": "B1EednfIQ",
            "result": "",
            "status": "success",
            "message": "ok",
            "generateTime": "2018-08-16 17:25:52.103"
        }
    ]
}
```

4. **取消某个异步执行的sql**  
请求方式:POST  
请求地址:`/hive/client/task/cancel`  
响应类型:application/json 
数据类型:application/json    
请求参数:   

    参数名 | 是否必须 | 类型 | 描述  | 默认值
    ---- | ----- | --- | --- | ---- |   
    无 | 是 | array | 表示要取消的queryId |
请求示例:
```
type: POST
url:http://192.168.0.248:9001/hive/client/task/cancel
body:["3"]
```
结果示例:
```
{
    "success": [
        "3"
    ]
}
```

5. **同步执行sql语句**  
请求方式:POST  
请求地址:`/hive/client/execute/sync`  
响应类型:application/json 
数据类型:application/json   
请求参数:   

    参数名 | 是否必须 | 类型 | 描述  | 默认值
    ---- | ----- | --- | --- | ---- |   
    sql | 是 | string | 表示要执行的语句 |
    params | 否 | array | sql语句中要替换的参数 |
请求示例:
```
type: POST
url:http://192.168.0.248:9001/hive/client/execute/sync
body:
{
	"sql":"select count(*) from events_json"
}
```
结果示例:
```
{
    "result": "[78369]",
    "success": true,
    "message": "ok"
}
```
