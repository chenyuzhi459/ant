webpackJsonp([21],{1049:function(t,e){t.exports={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"main"},[a("span",{staticStyle:{color:"#242f42","font-size":"25px"}},[t._v("Version:\n        "),a("b",[t._v(" "+t._s(t.statusData.version))])]),t._v(" "),a("br"),a("br"),t._v(" "),t._m(0),t._v(" "),a("div",{staticClass:"module",staticStyle:{"margin-left":"20px"}},[a("el-table",{staticStyle:{width:"100%"},attrs:{data:t.statusData.modules,border:"",stripe:""}},[a("el-table-column",{attrs:{prop:"name",label:"name"}}),t._v(" "),a("el-table-column",{attrs:{prop:"artifact",label:"artifact"}}),t._v(" "),a("el-table-column",{attrs:{prop:"version",label:"version",width:"180"}})],1)],1),t._v(" "),a("br"),a("br"),t._v(" "),t._m(1),t._v(" "),a("div",{staticClass:"memory",staticStyle:{"margin-left":"20px"}},[a("el-table",{staticStyle:{width:"100%"},attrs:{data:t.memeoryList,border:"",stripe:""}},[a("el-table-column",{attrs:{prop:"usedMemory",label:"usedMemory"}}),t._v(" "),a("el-table-column",{attrs:{prop:"freeMemory",label:"freeMemory"}}),t._v(" "),a("el-table-column",{attrs:{prop:"totalMemory",label:"totalMemory"}}),t._v(" "),a("el-table-column",{attrs:{prop:"totalMemory",label:"maxMemory"}})],1)],1)])},staticRenderFns:[function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticStyle:{"margin-left":"20px"}},[a("span",{staticStyle:{color:"#242f42","font-size":"20px"}},[t._v("系统模块")]),t._v(" "),a("br"),a("br")])},function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticStyle:{"margin-left":"20px"}},[a("span",{staticStyle:{color:"#242f42","font-size":"20px"}},[t._v("内存")]),t._v(" "),a("br"),a("br")])}]}},523:function(t,e,a){var r=a(198)(a(619),a(1049),null,null);t.exports=r.exports},619:function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0}),e.default={name:"status",data:function(){return{statusData:{version:"",modules:[],memory:{}},memeoryList:[]}},created:function(){this.getStatus()},methods:{getStatus:function(){var t=this;this.$http.get(this.$common.apis.status).then(function(e){t.statusData=e.data,t.memeoryList.push(t.statusData.memory)})}}}}});