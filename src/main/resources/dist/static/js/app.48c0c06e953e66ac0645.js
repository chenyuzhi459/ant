webpackJsonp([24],{367:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n=a(119),s=a.n(n),o=a(15),r=a.n(o),i=a(712),l=a.n(i),u=a(427),c=a(682),d=a.n(c),p=a(698),m=(a.n(p),a(167)),f=(a.n(m),a(715)),g=a(424),h=a(710),v=a(425),k=a(426),y=a(685),S=a.n(y),b=a(223),T=a.n(b),j=a(108),x=a.n(j),C=a(711),w=a.n(C);r.a.use(h.a),r.a.use(f.a),r.a.use(d.a),r.a.use(w.a),r.a.component("chart",w.a);var I=new h.a({locale:"zh",messages:{en:s()({},v.a,S.a),zh:s()({},k.a,T.a)}});x.a.i18n(function(e,t){return I.t(e,t)}),r.a.prototype.$t=x.a.t,r.a.prototype._i18n=I,r.a.prototype.$common=g.a,r.a.http.interceptors.push(function(e,t){e._timeout&&setTimeout(function(){t(e.respondWith(e.body,{status:408,statusText:x.a.t("message.common.requestTimeout")}))},e._timeout),t(function(e){return e})}),new r.a({router:u.a,render:function(e){return e(l.a)}}).$mount("#app")},419:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n=a(368),s=a.n(n),o=a(699),r=a.n(o),i=a(15),l=a.n(i),u=["legendselectchanged","legendselected","legendunselected","datazoom","datarangeselected","timelinechanged","timelineplaychanged","restore","dataviewchanged","magictypechanged","geoselectchanged","geoselected","geounselected","pieselectchanged","pieselected","pieunselected","mapselectchanged","mapselected","mapunselected","axisareaselected","focusnodeadjacency","unfocusnodeadjacency","brush","brushselected"],c=["click","dblclick","mouseover","mouseout","mousedown","mouseup","globalout"];t.default={props:{options:Object,theme:[String,Object],initOptions:Object,group:String,autoResize:Boolean},data:function(){return{chart:null}},computed:{width:{cache:!1,get:function(){return this.delegateGet("width","getWidth")}},height:{cache:!1,get:function(){return this.delegateGet("height","getHeight")}},isDisposed:{cache:!1,get:function(){return!!this.delegateGet("isDisposed","isDisposed")}},computedOptions:{cache:!1,get:function(){return this.delegateGet("computedOptions","getOption")}}},watch:{options:{handler:function(e){!this.chart&&e?this.init():this.chart.setOption(this.options,!0)},deep:!0},group:function(e){this.chart.group=e},theme:function(){this.destroy(),this.init()}},methods:{mergeOptions:function(e,t,a){this.delegateMethod("setOption",e,t,a)},resize:function(e){this.delegateMethod("resize",e)},dispatchAction:function(e){this.delegateMethod("dispatchAction",e)},convertToPixel:function(e,t){return this.delegateMethod("convertToPixel",e,t)},convertFromPixel:function(e,t){return this.delegateMethod("convertFromPixel",e,t)},containPixel:function(e,t){return this.delegateMethod("containPixel",e,t)},showLoading:function(e,t){this.delegateMethod("showLoading",e,t)},hideLoading:function(){this.delegateMethod("hideLoading")},getDataURL:function(e){return this.delegateMethod("getDataURL",e)},getConnectedDataURL:function(e){return this.delegateMethod("getConnectedDataURL",e)},clear:function(){this.delegateMethod("clear")},dispose:function(){this.delegateMethod("dispose")},delegateMethod:function(e){var t;if(!this.chart)return void l.a.util.warn("Cannot call ["+e+"] before the chart is initialized. Set prop [options] first.",this);for(var a=arguments.length,n=Array(a>1?a-1:0),s=1;s<a;s++)n[s-1]=arguments[s];return(t=this.chart)[e].apply(t,n)},delegateGet:function(e,t){return this.chart||l.a.util.warn("Cannot get ["+e+"] before the chart is initialized. Set prop [options] first.",this),this.chart[t]()},init:function(){var e=this;if(!this.chart){var t=s.a.init(this.$el,this.theme,this.initOptions);this.group&&(t.group=this.group),t.setOption(this.options,!0),u.forEach(function(a){t.on(a,function(t){e.$emit(a,t)})}),c.forEach(function(a){t.on(a,function(t){e.$emit(a,t),e.$emit("chart"+a,t)})}),this.autoResize&&(this.__resizeHanlder=r()(function(){t.resize()},100,{leading:!0}),window.addEventListener("resize",this.__resizeHanlder)),this.chart=t}},destroy:function(){this.autoResize&&window.removeEventListener("resize",this.__resizeHanlder),this.dispose(),this.chart=null}},mounted:function(){this.options&&this.init()},activated:function(){this.autoResize&&this.chart&&this.chart.resize()},beforeDestroy:function(){this.chart&&this.destroy()},connect:function(e){"string"!=typeof e&&(e=e.map(function(e){return e.chart})),s.a.connect(e)},disconnect:function(e){s.a.disConnect(e)},registerMap:function(){s.a.registerMap.apply(s.a,arguments)},registerTheme:function(){s.a.registerTheme.apply(s.a,arguments)}}},420:function(e,t,a){"use strict";var n="";console.log("NODE_ENV:","production"),n="api",console.log("base:",n),t.a={status:n+"/status",serversInfo:n+"/druid/coordinator/v1/servers",login:n+"/login",runningTasks:n+"/druid/indexer/v1/runningTasks/custom",completeTasks:n+"/druid/indexer/v1/completeTasks",pendingTasks:n+"/druid/indexer/v1/pendingTasks",waitingTasks:n+"/druid/indexer/v1/waitingTasks",searchCompleteTasks:n+"/druid/indexer/v1/search/completeTasks",taskChatUrl:n+"/druid/worker/v1/task",baseTaskUrl:n+"/druid/indexer/v1/task",overlordUrl:n+"/druid/indexer/v1",supervisor:n+"/druid/indexer/v1/supervisor",disableDataSource:n+"/druid/coordinator/v1/metadata/disableDatasources",intervals:n+"/druid/coordinator/v1/intervals",dataSource:n+"/druid/coordinator/v1/metadata/datasources",dataSourceDirect:n+"/druid/coordinator/v1/datasources",clientInfo:n+"/druid/v2/datasources",rules:n+"/druid/coordinator/v1/rules",lookups:n+"/druid/coordinator/v1/lookups",historicalIps:n+"/system/ip/historical",lookupsHis:n+"/druid/listen/v1/lookups",systemConfig:n+"/druid/systemConfig",clusterConfig:n+"/druid/coordinator/v1/config",loadstatus:n+"/druid/coordinator/v1/loadstatus",loadqueue:n+"/druid/coordinator/v1/loadqueue?simple",logConfig:n+"/log/logConfig",logList:n+"/log/logList",listFiles:n+"/log/listFiles",logging:n+"/log/logging",logPage:n+"/log/logPage",readLog:n+"/log/readLog",downloadLog:n+"/log/download",kafka:n+"/kafka",zk:n+"/zk",link:n+"/link",hmaster:n+"/druid/hmaster/v1",uindexDatasource:n+"/druid/hmaster/v1/datasources",uindexQueryUrl:n+"/uindex/druid/v2"}},421:function(e,t,a){"use strict";function n(e){return e.replace(/(^\s*)|(\s*$)/g,"")}function s(e){if(e<0)return e;u.a.duration(e,"hours",self);var t=u.a.duration(e).get("years");if(t>0)return u.a.duration(t,"years").humanize();var a=u.a.duration(e).get("months");if(a>0)return u.a.duration(a,"months").humanize();var n=u.a.duration(e).get("days");if(n>0)return u.a.duration(n,"days").humanize();var s=u.a.duration(e).get("hours");if(s>0)return u.a.duration(s,"hours").humanize();var o=u.a.duration(e).get("minutes");if(o>0)return u.a.duration(o,"minutes").humanize();var r=u.a.duration(e).get("seconds");return r>0?1===r?"a second":r+" seconds":u.a.duration(e,"milliseconds").humanize()}function o(){if(1==arguments.length)return arguments[0];for(var e=1;e<arguments.length;e++)arguments[0]=arguments[0].replace(new RegExp("\\{"+(e-1)+"\\}","g"),arguments[e]);return arguments[0]}var r=a(390),i=a.n(r),l=a(387),u=a.n(l),c=function(e,t){for(var a in e)t.push(e[a])},d={toString:function(e){return e?i()(e,null,2):null},toJsonObject:function(e){return JSON.parse(e)}},p=function(e,t,a){for(var n=[],s=(t-1)*a,o=s+a>=e.length?e.length-s:a,r=0;r<o;r++)n.push(e[s+r]);return n},m=function(e,t,a){return _.orderBy(e,[t],[a])},f=function(e,t,a){for(var n=[],s=0;s<e.length;s++)e[s][t].indexOf(a)>=0&&n.push(e[s]);return n},g=function(e,t,a){for(var n=[],s=0;s<e.length;s++)e[s][t].toLowerCase().indexOf(a.toLowerCase())>=0&&n.push(e[s]);return n},h=function(e,t,a){var n=new Date;n.setDate(n.getDate()+a),document.cookie=e+"="+escape(t)+(null===a?"":";expires="+n.toGMTString())},v=function(e){var t=void 0,a=new RegExp("(^| )"+e+"=([^;]*)(;|$)");return(t=document.cookie.match(a))?t[2]:null},k=function(e){var t=new Date;t.setTime(t.getTime()-1);var a=v(e);null!=a&&(document.cookie=e+"="+a+";expires="+t.toGMTString())},y=function(e,t){for(var a in e)t[a]=e[a]},S=function(e){var t=void 0;t=e<1e3?e.toFixed(2)+" B":e<1e6?(e/1024).toFixed(2)+" KB":e<1e9?(e/1e6).toFixed(2)+" MB":e<1e12?(e/1e9).toFixed(2)+" GB":(e/1e12).toFixed(2)+" TB";var a=t+"",n=a.indexOf(".");return"00"==a.substr(n+1,2)?a.substring(0,n)+a.substr(n+3,2):a};t.a={pushData:c,JSONUtils:d,fillShowTableData:p,sortArray:m,searchArray:f,searchArrayIgnoreCase:g,setCookie:h,getCookie:v,delCookie:k,fillObject:y,conver:S,trim:n,formatDuring:s,format:o}},422:function(e,t,a){"use strict";var n={datasourceName:"test-manager1",partitions:1,shardSpec:{type:"single",dimension:"distinct_id",partitionNum:-1},columnDelimiter:"",dimensions:[{type:"string",name:"distinct_id",hasMultipleValues:!1},{type:"int",name:"intValue",hasMultipleValues:!1}]},s=["i_intValue","s_stringValue"],o={type:"lucene_supervisor",ioConfig:{topic:"datasourceA",replicas:1,taskCount:1,taskDuration:"P1D",useEarliestOffset:"true",consumerProperties:{"bootstrap.servers":"192.168.0.213:9092,192.168.0.214:9092,192.168.0.216:9092"}},dataSchema:{parser:{type:"string",parseSpec:{format:"url",timestampSpec:{column:"EventDateTime",format:"millis"},dimensionsSpec:{dimensions:[{name:"IP",type:"string"},{name:"EventDateTime",type:"string"},{name:"EventLabel",type:"string"}],spatialDimensions:[],dimensionExclusions:[]}}},dataSource:"datasourceA",metricsSpec:[],granularitySpec:{type:"uniform",queryGranularity:"NONE",segmentGranularity:"DAY"}},tuningConfig:{type:"kafka",buildV9Directly:!0,maxRowsInMemory:5e5,maxRowsPerSegment:2e7,basePersistDirectory:"/data1/druid/storage/datasourceA",intermediatePersistPeriod:"PT10M"}},r={type:"lucene_index",worker:"dev1.sugo.net:8091",spec:{dataSchema:{dataSource:"index-0",parser:{type:"string",parseSpec:{format:"csv",timestampSpec:{column:"ts",format:"millis"},listDelimiter:",",columns:["ts","strCol01","strCol02","strCol03","strCol04","strCol05","floatCol1","floatCol2","floatCol3","floatCol4","floatCol5","dblCol06"],dimensionsSpec:{dynamicDimension:!1,dimensions:[{name:"strCol01",type:"string"},{name:"strCol02",type:"string"},{name:"strCol03",type:"string"},{name:"strCol04",type:"string"},{name:"strCol05",type:"string"},{name:"floatCol1",type:"float"},{name:"floatCol2",type:"float"},{name:"floatCol3",type:"float"},{name:"floatCol4",type:"float"},{name:"floatCol5",type:"float"},{name:"dblCol06",type:"double"}]}}},granularitySpec:{intervals:["2017-01-01/2017-01-02"],segmentGranularity:"DAY",type:"uniform"}},ioConfig:{firehose:{type:"local",filter:"data1kw.csv",baseDir:"/data1/csv/"},type:"lucene_index"},tuningConfig:{type:"lucene_index",maxRowsPerSegment:2e6}}};t.a={uindexDatasourceJson:n,uindexDimensionJson:s,supervisorSpecJson:o,taskJson:r}},423:function(e,t,a){"use strict";var n=a(15),s=a.n(n);t.a=new s.a},424:function(e,t,a){"use strict";var n=a(420),s=a(421),o=a(423),r=a(422);t.a={apis:n.a,methods:s.a,eventBus:o.a,demo:r.a}},425:function(e,t,a){"use strict";t.a={message:{login:{buttonContent:"login",username:"username",password:"password",ruleMessage1:"please input username",ruleMessage2:"please input password",errorTips:"username or password is wrong"},header:{title:"Tindex Backstage Manager System",logout:"Logout",changeLanuage:"Change Lanuage",lanuage1:"Chinese",lanuage2:"English",quickLinks:"Quick Links"},sideBar:{dataAccess:{title:"Data Access",supervisor:"Supervisor Manager",task:"Task Manager"},manager:{title:"System Manager",status:"System Status",serversInfo:"Servers Info",systemConfig:"System Config",zkManager:"ZooKeeper Manager",hdfsManager:"HDFS Manager"},dataSource:{title:"DataSource Manager",dataSource:"DataSources(cluster)",metadata:"DataSources(Metadata)"},uindex:{title:"Uindex Manager"},lookup:{title:"Lookup Manager"},log:{title:"Log Manager"}},status:{version:"Version:",module:"System Modules",name:"name",artifact:"artifact",_version:"version",memory:"Memory",usedMemory:"usedMemory",freeMemory:"freeMemory",totalMemory:"totalMemory",maxMemory:"maxMemory"},serversInfo:{host:"host",title:"Servers Info",server:"Server",tier:"tier",priority:"priority",currSize:"currSize",maxSize:"maxSize",segments:"segments",searchTips:"please input name"},zkManager:{treeSubTitile:"ZooKeeper Structure Tree",dataSubTitle:"Data",path:"Path",checkMore:"check more"},tasks:{runningTasksTitle:"Running Tasks",pendingTasksTitle:"Pending Tasks - Tasks waiting to be assigned to a worker",waitingTasksTitle:"Waiting Tasks - Tasks waiting on locks ",completeTasksTitle:"Complete Tasks - Tasks recently completed",workersTitile:"Remote Workers",createTask:"Create Task",taskSpec:"Task Spec",format:"format",jsonSyntaxError:"wrong json syntax",createDialogConfirm:"Create",creatSuccess:"create successfully, taskId",createFailedFront:"request failed with status",createFailedLast:"please check overlord logs",searchTips:"please input id",searchTopicTips:"please input topic name",search:"Search",refresh:"Refresh",createdTime:"createdTime",queueInsertTime:"queueInsertTime",location:"location",operation:"more",taskPayloadTitle:"Task Payload",payload:"payload",taskStatusTitle:"Task Status",status:"status",segments:"segments",allLog:"log(all)",partLog:"log(8kb)",delete:"kill",dialogConfirm:"Confirm",dialogCancel:"Cancel",killTips:"Do you really want to kill",killConfirmTitle:"Warning",killSuccess:"Kill Successfully!",locationHost:"location host",locationPort:"location port",selectStatus:"please select status",statusItem1:"ALL",statusItem2:"SUCCESS",statusItem3:"FAILED",duration:"duration",topic:"topic",offsets:"offsets",currOffset:"currOffset",taskOffsetTitle:"Offset",metrics:"metrics",period:"period",datasource:"datasource",startOffset:"startOffset",topicOffsets:"Topic Offsets",wokerHost:"host",wokerIp:"ip",workerCapacity:"capacity",workerVersion:"version",currCapacityUsed:"currCapacityUsed",availabilityGroups:"groups",workerRunningTaks:"tasks",lastCompletedTaskTime:"lastCompletedTaskTime",blacklistedUntil:"blacklistedUntil"},supervisors:{supervisorManager:"Supervisor Manager",supervisorTasks:"Supervisor Tasks",runningSuperviorTitle:"Running Supervisors",completeSupervisorsTitle:"Complete Supervisors",searchTips:"please input id",search:"Search",refresh:"Refresh",operation:"more",tasks:"tasks",spec:"spec",status:"status",reset:"reset",delete:"kill",dialogConfirm:"Confirm",dialogCancel:"Cancel",specTitle:"Supervisor Spec",statusTitle:"Supervisor Status",resetTips:"Do you really want to reset",resetSuccessMsg:"reset successfully, id",resetErrMsg:"reset failed,please check overlord logs",dialogWarningTitle:"Warning",killTips:"Do you really want to kill",killSuccess:"Kill Successfully!",killFailed:"Kill failed,please check overlord logs",version:"version",taskDuration:"taskDuration",type:"type",topic:"topic",reuse:"reuse",supervisorStatusTitle:"Supervisor Status",supervisorSpec:"Supervisor Spec",createSupervisor:"createSupervisor",format:"format",createDialogConfirm:"Create",jsonSyntaxError:"wrong json syntax",creatSuccess:"create successfully, id",createFailedFront:"request failed with status",createFailedLast:"please check overlord logs",activeTasksNum:"activeTasksNum",selectType:{tips:"please select type",item1:"ALL",item2:"lucene_supervisor",item3:"default_supervisor"}},dataSource:{dataSourceTitle:"DataSources",dataSource:"DataSource",segments:"segments",span:"time span",tiers:"tiers",intervals:"intervals",tiersInfo:"Tiers Info",warning:"Warning",createTime:"createTime",dimensions:"dimensions / metrics",metrics:"metrics",candidates:"candidates",dimensionsInfo:"Dimensions Info",metricsInfo:"Metrics Info",candidatesInfo:"Candidates Info",rules:"rules",history:"history",add:"add",rulesInfo:"Rules Info",rulesHistory:"Rules History",addRulesSuccess:"Add Rules Success!",addRulesFail:"Add Rules Fail!",addRuleWarning:"Do you really add rule?",updateRuleWarning:"Do you really update rule?",dataSourceInfo:"DataSource Info",serversInfo:"Server Info",granularity:"granularity",operate:"operate",granularityInfo:"Please select a granularity",operateInfo:"Please select an operation",periodInputInfo:"Please input a valid ISO 8601 duration",intervalInputInfo:"Please input a valid ISO 8601 interval",addRule:"Add A Rule",servers:"servers",segmentCount:"segment count",intervalCount:"interval count",clusterConfig:"Cluster Config",clusterConfigHistory:"Cluster Config History"},interval:{name:"interval",intervalTitle:"Intervals",interval:"Interval",segments:"segments",mergeTips:"Do you really want to merge all segmens in [{0}] ?",createMergeTaskSuccess:"create merge task successfully, taskId",createFailedFront:"create merge task failed with status",createFailedLast:"please check overlord logs"},segment:{segmentTitle:"Segments",segment:"Segment",refresh:"Refresh",name:"name",info:"info",more:"more",delete:"delete",segmentInfo:"Segment Info",confirm:"Confirm",cancel:"Cancel"},lookup:{lookupCoordinator:{lookupTitle:"Lookup"},lookupHistorical:{},lookups:"Lookup Tier",userGroupLookup:"Lookup",type:"type",version:"version",lookupInfo:"Lookup Info",addLookup:"Create Lookup",lookupName:"Lookup Name",lookupNameIndex:"Please input the lookup name",addLookupWarning:"Do you really want to add lookup:",updateLookupWarning:"Do you really want to update lookup:",inputLookupName:"Please input lookup name",groupId:"groupId"},common:{more:"more",info:"info",delete:"delete",size:"size",confirm:"Confirm",createConfirm:"Create",cancel:"Cancel",refresh:"Refresh",name:"name",count:"count",search:"Search",warning:"Warning",deleteWarning:"Do you really want to delete:",disableWarning:"Do you really want to disable:",enableWarning:"Do you really want to enable:",enableSuccess:"Enable Success!",enableFail:"Enable Fail!",deleteSuccess:"Delete Success!",deleteFail:"Delete Fail!",addSuccess:"Add Success!",updateSuccess:"Update Success!",updateFail:"Update Fail!",disableSuccess:"Disable Success!",disableFail:"Disable Fail!",addFail:"Add Fail!",items:"items",disable:"disable",inputName:"Please input name",update:"update",return:"Back",type:"type",operation:"operation",detail:"detail",close:"close",enable:"enable",state:"state",requestTimeout:"request time out",none:"none",reset:"reset",merge:"merge",segment:"segment",lastModifiedTime:"last modified time",fileName:"file name",download:"download",downloadAll:"download all",selectAll:"select all",showAll:"show all",showPart:"show part",jsonSyntaxError:"wrong json syntax",format:"format"},error:{chooseAction:"Action can not be null! Please choose an action",chooseGranularity:"Granularity can not be null! Please choose a granularity",canNotBeNull:" can not be null!"},systemConfig:{systemConfigTitle:"System Config",menuConfig:"Menu Config",druidConfig:"Druid Config",uindexConfig:"Uindex Config",kafkaConfig:"Kafka Config",zkConfig:"Zookeeper Config",updateConfig:"Do you really want to update config?"},log:{checkLog:"check log",all:"all log",recent:"recent log",paging:"paging log",realtime:"realtime log",startScrolling:"start scroll",stopScrolling:"stop scroll"},topicChart:{title:"Offset-Overview",legend:["pulled","pulling","not pulling"]},uindex:{uindexTitle:"Uindex Datasources",specTitle:"Spec",distributeButton:"distribution",distributeTitle:"segments distribution",datasourceSpec:"Datasource Spec",createDatasource:"create Datasource",createFailedFront:"request failed with status",createFailedLast:"please check hmaster logs",dimensions:"dimensions",enable:"Enable",disable:"Disable",addDimension:"Add Dimensions",searchTips:"please input name",dataSource:"dataSource",spec:"spec"}}}},426:function(e,t,a){"use strict";var n,s=a(392),o=a.n(s);t.a={message:{login:{buttonContent:"登录",username:"用户名",password:"密码",ruleMessage1:"请输入用户名",ruleMessage2:"请输入密码",errorTips:"用户名或密码错误"},header:{title:"Tindex后台管理系统",logout:"退出",changeLanuage:"切换语言",lanuage1:"中文",lanuage2:"英文",quickLinks:"快捷链接"},tasks:{runningTasksTitle:"运行任务",pendingTasksTitle:"队列任务 - 等待分配资源的任务",waitingTasksTitle:"阻塞任务 - 等待获取锁的任务 ",workersTitile:"Remote Workers",completeTasksTitle:"已完成任务",createTask:"创建新任务",taskSpec:"任务说明",format:"格式化",jsonSyntaxError:"json格式错误",createDialogConfirm:"创建",creatSuccess:"任务创建成功, 任务Id",createFailedFront:"任务创建失败,状态码",createFailedLast:"请查看overlord日志",searchTips:"请输入id",searchTopicTips:"请输入topic名称",search:"搜索",refresh:"刷新",createdTime:"创建时间",queueInsertTime:"入队时间",location:"位置",operation:"操作",taskPayloadTitle:"Payload",payload:"payload",taskStatusTitle:"任务状态",status:"状态",segments:"数据段",allLog:"日志",partLog:"日志(8kb)",delete:"删除",dialogConfirm:"确定",dialogCancel:"取消",killTips:"你确定要删除",killConfirmTitle:"警告",killSuccess:"删除成功!",locationHost:"主机",locationPort:"端口",selectStatus:"请选择状态",statusItem1:"全部",statusItem2:"成功",statusItem3:"失败",duration:"运行时长",topic:"topic",offsets:"offsets",currOffset:"currOffset",taskOffsetTitle:"Offset",metrics:"指标",period:"阶段",datasource:"数据源",startOffset:"startOffset",topicOffsets:"Topic Offsets",wokerHost:"host",wokerIp:"ip",workerCapacity:"容量",workerVersion:"版本",currCapacityUsed:"已用容量",availabilityGroups:"活动组",workerRunningTaks:"运行任务",lastCompletedTaskTime:"上个任务完成时间",blacklistedUntil:"blacklistedUntil"},supervisors:{supervisorManager:"Supervisor管理",supervisorTasks:"Supervisor任务",runningSuperviorTitle:"运行supervisors",completeSupervisorsTitle:"已完成Supervisors",searchTips:"请输入id",search:"搜索",refresh:"刷新",operation:"操作",tasks:"任务",spec:"说明",status:"状态",reset:"重置",delete:"删除",dialogConfirm:"确定",dialogCancel:"取消",specTitle:"Supervisor 说明",statusTitle:"Supervisor 状态",resetTips:"你确定要重置",resetSuccessMsg:"重置成功, id",resetErrMsg:"重置失败,请查看overlord日志",dialogWarningTitle:"警告",killTips:"你确定要删除",killSuccess:"删除成功!",killFailed:"删除失败,请查看overlord日志",version:"版本",taskDuration:"任务持续时间",type:"类型",topic:"topic",reuse:"重用",supervisorStatusTitle:"Supervisor 状态",supervisorSpec:"Supervisor 说明",createSupervisor:"创建Supervisor",format:"格式化",createDialogConfirm:"创建",jsonSyntaxError:"json格式错误",creatSuccess:"创建成功, id",createFailedFront:"创建失败,状态码",createFailedLast:"请查看overlord日志",activeTasksNum:"已启动任务数",selectType:{tips:"请选择类型",item1:"ALL",item2:"lucene_supervisor",item3:"default_supervisor"}},sideBar:{dataAccess:{title:"数据接入",supervisor:"supervisor管理",task:"task管理"},manager:{title:"系统管理",status:"系统状态",serversInfo:"节点信息",systemConfig:"系统配置",zkManager:"ZooKeeper管理",hdfsManager:"HDFS管理"},dataSource:{title:"数据源管理",dataSource:"数据源（集群）",metadata:"数据源（元数据）"},uindex:{title:"Uindex管理"},lookup:{title:"用户分群管理"},log:{title:"日志管理"}},status:{version:"版本:",module:"系统模块",name:"名称",artifact:"模块",_version:"版本",memory:"内存",usedMemory:"已用内存",freeMemory:"可用内存",totalMemory:"总内存",maxMemory:"最大内存"},serversInfo:{host:"host",title:"节点信息",server:"节点",tier:"tier",priority:"优先级",currSize:"当前大小",maxSize:"最大大小",segments:"数据段",searchTips:"请输入名称"},zkManager:{treeSubTitile:"ZooKeeper结构树",dataSubTitle:"数据",path:"路径",checkMore:"查看更多"},dataSource:{dataSourceTitle:"数据源",dataSource:"数据源",search:"搜索",refresh:"刷新",name:"名称",segments:"数据段",count:"数量",size:"大小",span:"时间跨度",more:"更多",tiers:"等级",intervals:"区间",delete:"删除",tiersInfo:"等级信息",createTime:"创建时间",dimensions:"维度 / 指标",metrics:"指标",candidates:"candidates",dimensionsInfo:"维度信息",metricsInfo:"指标信息",candidatesInfo:"Candidates信息",rules:"规则",history:"历史",add:"添加",rulesInfo:"规则信息",serversInfo:"节点信息",rulesHistory:"规则（历史信息）",addRulesSuccess:"添加规则成功！",addRulesFail:"添加规则失败！",addRuleWarning:"你确定要添加规则？",updateRuleWarning:"你确定要更新规则？",dataSourceInfo:"数据源信息",granularity:"操作粒度",operate:"操作",granularityInfo:"请选择操作粒度",operateInfo:"请选择操作",periodInputInfo:"请输入一个有效的 ISO 8601 period",intervalInputInfo:"请输入一个有效的 ISO 8601 interval",addRule:"添加规则",servers:"节点",segmentCount:"数据段数量",intervalCount:"区间数量",clusterConfig:"集群配置",clusterConfigHistory:"集群配置历史"},interval:{intervalTitle:"区间",interval:"区间",refresh:"刷新",name:"名称",segments:"数据段",more:"更多",mergeTips:"你真的要合并区间 [{0}] 里的所有数据段吗?",mergeTaskNotify:{title:"成功",message:"你可以根据以下id在taskManager中查看合并任务"},createMergeTaskSuccess:"成功创建合并任务,任务Id"},segment:{segmentTitle:"数据段",segment:"数据段",refresh:"刷新",name:"名称",info:"信息",more:"更多",delete:"删除",segmentInfo:"数据段信息",confirm:"确 定",cancel:"取 消"},lookup:{lookupCoordinator:{lookupTitle:"用户分群"},lookupHistorical:{},lookups:"分群组",userGroupLookup:"分群",type:"类型",version:"版本",lookupInfo:"分群信息",addLookup:"创建分群",lookupName:"分群名称",lookupNameIndex:"请输入分群名称",addLookupWarning:"你确定要添加分群：",updateLookupWarning:"你确定要更新分群：",inputLookupName:"请输入分群名称",groupId:"分组Id"},common:(n={more:"操作",info:"详情",delete:"删除",size:"大小",confirm:"确 认",createConfirm:"创 建",cancel:"取 消",refresh:"刷新",name:"名称",count:"数量",search:"搜索",warning:"警告",deleteWarning:"你确定要删除：",disableWarning:"你确定要失效:",enableWarning:"你确定要恢复:",enableSuccess:"恢复成功!",enableFail:"恢复失败!",deleteSuccess:"删除成功!",deleteFail:"删除失败!",addSuccess:"添加成功！",updateSuccess:"更新成功！",updateFail:"更新失败！",disableSuccess:"失效成功!",disableFail:"失效失败!",addFail:"添加失败！",items:"条目明细",disable:"失效",return:"返回",inputName:"请输入名称",update:"更新"},o()(n,"return","返回"),o()(n,"type","类型"),o()(n,"operation","操作"),o()(n,"detail","详情"),o()(n,"close","关闭"),o()(n,"enable","恢复"),o()(n,"state","状态"),o()(n,"requestTimeout","请求超时"),o()(n,"none","无"),o()(n,"reset","重置"),o()(n,"merge","合并"),o()(n,"segment","数据段"),o()(n,"lastModifiedTime","最后修改时间"),o()(n,"fileName","文件名称"),o()(n,"download","下载"),o()(n,"downloadAll","下载所有"),o()(n,"selectAll","全选"),o()(n,"showAll","显示所有"),o()(n,"showPart","显示局部"),o()(n,"jsonSyntaxError","json格式错误"),o()(n,"format","格式化"),n),error:{chooseAction:"操作不能为空! 请选择一种操作",chooseGranularity:"操作粒度不能为空! 请选择一种操作粒度",canNotBeNull:" 不能为空!"},systemConfig:{systemConfigTitle:"系统配置",menuConfig:"菜单配置",druidConfig:"Druid 配置",uindexConfig:"Uindex 配置",kafkaConfig:"Kafka 配置",zkConfig:"Zookeeper 配置",updateConfig:"你确认要更新配置吗？"},log:{checkLog:"日志查看",all:"全部日志",recent:"最新日志",paging:"分页日志",realtime:"实时日志",startScrolling:"开启滚动",stopScrolling:"停止滚动"},topicChart:{title:"Offset-概览",legend:["已拉取","正在拉取","未拉取"]},uindex:{uindexTitle:"Uindex数据源",specTitle:"说明",distributeButton:"分布",distributeTitle:"数据段分布",datasourceSpec:"数据源说明",createDatasource:"创建数据源",createFailedFront:"数据源创建失败,状态码",createFailedLast:"请查看hmaster日志",dimensions:"维度",enable:"有效",disable:"无效",addDimension:"添加维度",searchTips:"请输入名称",dataSource:"数据源",spec:"说明"}}}},427:function(e,t,a){"use strict";var n=a(15),s=a.n(n),o=a(716);s.a.use(o.a),t.a=new o.a({routes:[{path:"/",redirect:"/index"},{path:"/allLog",component:function(e){return a.e(19).then(function(){var t=[a(764)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/lastLines",component:function(e){return a.e(18).then(function(){var t=[a(765)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/pagingLog",component:function(e){return a.e(17).then(function(){var t=[a(766)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/realtimeLog",component:function(e){return a.e(16).then(function(){var t=[a(767)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/index",component:function(e){return a.e(7).then(function(){var t=[a(756)];e.apply(null,t)}.bind(this)).catch(a.oe)},children:[{path:"/",component:function(e){return a.e(0).then(function(){var t=[a(395)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"serversInfo",path:"/serversInfo",component:function(e){return a.e(21).then(function(){var t=[a(774)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"serversSegment",path:"/serversSegment/:serverName",component:function(e){return a.e(20).then(function(){var t=[a(775)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"zkManager",path:"/zkManager",component:function(e){return a.e(13).then(function(){var t=[a(773)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"supervisorManager",path:"/supervisorManager",component:function(e){return a.e(0).then(function(){var t=[a(395)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"supervisorTasks",path:"/:supervisorId/supervisorTasks",component:function(e){return a.e(3).then(function(){var t=[a(758)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"taskManager",path:"/taskManager",component:function(e){return a.e(1).then(function(){var t=[a(757)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"dataSource",path:"/dataSource",component:function(e){return a.e(6).then(function(){var t=[a(759)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"interval",path:"/interval",component:function(e){return a.e(2).then(function(){var t=[a(760)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"segment",path:"/segment",component:function(e){return a.e(4).then(function(){var t=[a(761)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/lookupCoordinator",component:function(e){return a.e(12).then(function(){var t=[a(769)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/lookupHistorical",component:function(e){return a.e(11).then(function(){var t=[a(770)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"systemConfig",path:"/systemConfig",component:function(e){return a.e(14).then(function(){var t=[a(772)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"uindex",path:"/uindex",component:function(e){return a.e(9).then(function(){var t=[a(777)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{name:"dimension",path:"/dimension",component:function(e){return a.e(10).then(function(){var t=[a(776)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/basecharts",component:function(e){return a.e(5).then(function(){var t=[a(771)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/logView",component:function(e){return a.e(8).then(function(){var t=[a(762)];e.apply(null,t)}.bind(this)).catch(a.oe)}},{path:"/plain",component:function(e){return a.e(22).then(function(){var t=[a(763)];e.apply(null,t)}.bind(this)).catch(a.oe)}}]},{path:"/login",component:function(e){return a.e(15).then(function(){var t=[a(768)];e.apply(null,t)}.bind(this)).catch(a.oe)}}]})},654:function(e,t,a){t=e.exports=a(84)(void 0),t.i(a(657),""),t.i(a(656),""),t.push([e.i,"",""])},655:function(e,t,a){t=e.exports=a(84)(void 0),t.push([e.i,".echarts{width:600px;height:400px}",""])},656:function(e,t,a){t=e.exports=a(84)(void 0),t.push([e.i,".header{background-color:#242f42}.login-wrap{background:#324157}.plugins-tips{background:#eef1f6}.el-upload--text em,.plugins-tips a{color:#20a0ff}.pure-button{background:#1f79b9}",""])},657:function(e,t,a){t=e.exports=a(84)(void 0),t.push([e.i,"*{margin:0;padding:0}#app,.wrapper,body,html{width:100%;height:100%;overflow:hidden}body{font-family:Helvetica Neue,Helvetica,microsoft yahei,arial,STHeiTi,sans-serif}a{text-decoration:none}.content{background:none repeat scroll 0 0 #fff;position:absolute;left:250px;right:0;top:70px;bottom:0;width:auto;padding:35px;box-sizing:border-box;overflow-y:scroll}.crumbs{margin-bottom:20px}.pagination{margin:20px 0;text-align:right}.plugins-tips{padding:20px 10px;margin-bottom:20px}.el-button+.el-tooltip{margin-left:10px}.el-table tr:hover{background:#f6faff}.mgb20{margin-bottom:20px}.move-enter-active,.move-leave-active{transition:opacity .5s}.move-enter,.move-leave{opacity:0}.form-box{width:600px}.form-box .line{text-align:center}.el-time-panel__content:after,.el-time-panel__content:before{margin-top:-7px}.ms-doc .el-checkbox__input.is-disabled+.el-checkbox__label{color:#333;cursor:pointer}.pure-button{width:150px;height:40px;line-height:40px;text-align:center;color:#fff;border-radius:3px}.g-core-image-corp-container .info-aside{height:45px}.el-upload--text{background-color:#fff;border:1px dashed #d9d9d9;border-radius:6px;box-sizing:border-box;width:360px;height:180px;cursor:pointer;position:relative;overflow:hidden}.el-upload--text .el-icon-upload{font-size:67px;color:#97a8be;margin:40px 0 16px;line-height:50px}.el-upload--text{color:#97a8be;font-size:14px;text-align:center}.el-upload--text em{font-style:normal}.ql-container{min-height:400px}.ql-snow .ql-tooltip{transform:translateX(117.5px) translateY(10px)!important}.editor-btn{margin-top:20px}",""])},698:function(e,t){},701:function(e,t,a){function n(e){return a(s(e))}function s(e){var t=o[e];if(!(t+1))throw new Error("Cannot find module '"+e+"'.");return t}var o={"./af":232,"./af.js":232,"./ar":239,"./ar-dz":233,"./ar-dz.js":233,"./ar-kw":234,"./ar-kw.js":234,"./ar-ly":235,"./ar-ly.js":235,"./ar-ma":236,"./ar-ma.js":236,"./ar-sa":237,"./ar-sa.js":237,"./ar-tn":238,"./ar-tn.js":238,"./ar.js":239,"./az":240,"./az.js":240,"./be":241,"./be.js":241,"./bg":242,"./bg.js":242,"./bm":243,"./bm.js":243,"./bn":244,"./bn.js":244,"./bo":245,"./bo.js":245,"./br":246,"./br.js":246,"./bs":247,"./bs.js":247,"./ca":248,"./ca.js":248,"./cs":249,"./cs.js":249,"./cv":250,"./cv.js":250,"./cy":251,"./cy.js":251,"./da":252,"./da.js":252,"./de":255,"./de-at":253,"./de-at.js":253,"./de-ch":254,"./de-ch.js":254,"./de.js":255,"./dv":256,"./dv.js":256,"./el":257,"./el.js":257,"./en-au":258,"./en-au.js":258,"./en-ca":259,"./en-ca.js":259,"./en-gb":260,"./en-gb.js":260,"./en-ie":261,"./en-ie.js":261,"./en-nz":262,"./en-nz.js":262,"./eo":263,"./eo.js":263,"./es":266,"./es-do":264,"./es-do.js":264,"./es-us":265,"./es-us.js":265,"./es.js":266,"./et":267,"./et.js":267,"./eu":268,"./eu.js":268,"./fa":269,"./fa.js":269,"./fi":270,"./fi.js":270,"./fo":271,"./fo.js":271,"./fr":274,"./fr-ca":272,"./fr-ca.js":272,"./fr-ch":273,"./fr-ch.js":273,"./fr.js":274,"./fy":275,"./fy.js":275,"./gd":276,"./gd.js":276,"./gl":277,"./gl.js":277,"./gom-latn":278,"./gom-latn.js":278,"./gu":279,"./gu.js":279,"./he":280,"./he.js":280,"./hi":281,"./hi.js":281,"./hr":282,"./hr.js":282,"./hu":283,"./hu.js":283,"./hy-am":284,"./hy-am.js":284,"./id":285,"./id.js":285,"./is":286,"./is.js":286,"./it":287,"./it.js":287,"./ja":288,"./ja.js":288,"./jv":289,"./jv.js":289,"./ka":290,"./ka.js":290,"./kk":291,"./kk.js":291,"./km":292,"./km.js":292,"./kn":293,"./kn.js":293,"./ko":294,"./ko.js":294,"./ky":295,"./ky.js":295,"./lb":296,"./lb.js":296,"./lo":297,"./lo.js":297,"./lt":298,"./lt.js":298,"./lv":299,"./lv.js":299,"./me":300,"./me.js":300,"./mi":301,"./mi.js":301,"./mk":302,"./mk.js":302,"./ml":303,"./ml.js":303,"./mr":304,"./mr.js":304,"./ms":306,"./ms-my":305,"./ms-my.js":305,"./ms.js":306,"./my":307,"./my.js":307,"./nb":308,"./nb.js":308,"./ne":309,"./ne.js":309,"./nl":311,"./nl-be":310,"./nl-be.js":310,"./nl.js":311,"./nn":312,"./nn.js":312,"./pa-in":313,"./pa-in.js":313,"./pl":314,"./pl.js":314,"./pt":316,"./pt-br":315,"./pt-br.js":315,"./pt.js":316,"./ro":317,"./ro.js":317,"./ru":318,"./ru.js":318,"./sd":319,"./sd.js":319,"./se":320,"./se.js":320,"./si":321,"./si.js":321,"./sk":322,"./sk.js":322,"./sl":323,"./sl.js":323,"./sq":324,"./sq.js":324,"./sr":326,"./sr-cyrl":325,"./sr-cyrl.js":325,"./sr.js":326,"./ss":327,"./ss.js":327,"./sv":328,"./sv.js":328,"./sw":329,"./sw.js":329,"./ta":330,"./ta.js":330,"./te":331,"./te.js":331,"./tet":332,"./tet.js":332,"./th":333,"./th.js":333,"./tl-ph":334,"./tl-ph.js":334,"./tlh":335,"./tlh.js":335,"./tr":336,"./tr.js":336,"./tzl":337,"./tzl.js":337,"./tzm":339,"./tzm-latn":338,"./tzm-latn.js":338,"./tzm.js":339,"./uk":340,"./uk.js":340,"./ur":341,"./ur.js":341,"./uz":343,"./uz-latn":342,"./uz-latn.js":342,"./uz.js":343,"./vi":344,"./vi.js":344,"./x-pseudo":345,"./x-pseudo.js":345,"./yo":346,"./yo.js":346,"./zh-cn":347,"./zh-cn.js":347,"./zh-hk":348,"./zh-hk.js":348,"./zh-tw":349,"./zh-tw.js":349};n.keys=function(){return Object.keys(o)},n.resolve=s,e.exports=n,n.id=701},711:function(e,t,a){a(718);var n=a(170)(a(419),a(714),null,null);e.exports=n.exports},712:function(e,t,a){a(717);var n=a(170)(null,a(713),null,null);e.exports=n.exports},713:function(e,t){e.exports={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{attrs:{id:"app"}},[a("router-view")],1)},staticRenderFns:[]}},714:function(e,t){e.exports={render:function(){var e=this,t=e.$createElement;return(e._self._c||t)("div",{staticClass:"echarts"})},staticRenderFns:[]}},717:function(e,t,a){var n=a(654);"string"==typeof n&&(n=[[e.i,n,""]]),n.locals&&(e.exports=n.locals);a(171)("42b87a16",n,!0)},718:function(e,t,a){var n=a(655);"string"==typeof n&&(n=[[e.i,n,""]]),n.locals&&(e.exports=n.locals);a(171)("3fc01954",n,!0)},754:function(e,t){},755:function(e,t,a){a(167),e.exports=a(367)}},[755]);