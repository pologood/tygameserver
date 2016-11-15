<template>  
  <div id="app">   
    <router-view></router-view> 
    <div class="container">
      <h1>littlegame</h1>

      <div class="form-inline row">     
          <div class="form-group col-md-2">
            <label>用户名:</label>
		        <label id="name2">{{userName}}</label>
          </div>          
          <div class="form-group  col-md-2">     
              <label>sessionId:</label>
              <label id="sId">{{sessionId}}</label>
          </div>
          <div class="form-group  col-md-2">     
              <label>房号:</label>
              <label id="roomId">{{roomId}}</label>
          </div>
      </div> 
  
      <div class="form-inline"> 
        <div class="form-group   col-md-4">
          <input id="uuid" type="text" class="form-control" placeholder="UUID" v-model="uuid"/>
          <button id="loadBtn" class="btn btn-primary" v-on:click="connectSocket">UUID</button>
        </div>         
        <div class="form-group">
          <input id="name" type="text" class="form-control" v-model="signupName"/>
          <button id="registerBtn" class="btn btn-primary" v-on:click="registerBtnHandle">注册</button>
          <button id="loginBtn" class="btn btn-primary" v-on:click="loginBtnHandle">登陆</button>
          <button id="cBtn" class="btn btn-primary" v-on:click="cBtnHandle">创建房间</button>  
        </div>
      </div>           

      <div class="form-inline">
        <div class="form-group">
          <select id="roomList" v-model="roomSelected">
            <option value="-1">请选择房间</option>
            <option v-for="item in roomList" v-bind:value="item.id">{{item.id}}</option>
          </select>
          <button id="addRoomBtn" class="btn btn-primary" v-on:click="addRoomBtnHandle">加入</button>
          <button id="roomListBtn" class="btn btn-primary" v-on:click="roomListBtnHandle">刷新</button>
        </div>        
      </div>      
      
      <div class="row">
        <!--成员列表-->
        <div class="col-md-2">
          <h2>成员列表</h2>
          <table class="table">
            <thead>
              <tr>
                <th>成员列表</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in members">
                <th>{{item.name}}</th>
              </tr>
            </tbody>
          </table>
        </div>  
        <!--成员列表-->

        <!--聊天-->
        <div class="col-md-10">
          <h2>群聊</h2>
          <div class="chatCnt">
            <ul>
              <li v-for="item in groupmsgs">{{item.msg}}</li>
            </ul>
          </div>         

          <div class="row">
            <div class="col-md-10">
              <input id="groupMsg" type="text" class="form-control" placeholder="发送消息" v-model="sendChatMsg"/>
            </div>
            <div class="col-md-2 sendBtnCnt">
              <button id="gBtn" class="btn btn-primary sendBtn" v-on:click="gBtnHandle">发送消息</button>
            </div>            
          </div>
        </div>
        <!--聊天-->
      </div>  

      <!--消息列表-->
      <div>
        <h2>消息列表</h2>
        <div class="msgCnt">
          <ul>
            <li v-for="item in msgList">{{item}}</li>
          </ul>
        </div>        
      </div>
      <!--消息列表-->

      <div>
        <div class="row">

          <div class="col-md-2">
            <select id="playerList" v-model="playerSelected">
              <option v-for="item in playerList" v-bind:value="item.id">{{item.name}}</option>
            </select>
            <button id="refresh" class="btn btn-primary" v-on:click="refreshHandle">刷新</button>
          </div>         

          <div class="col-md-10">
            <div class="col-md-8">
              <input id="msg" type="text" class="form-control" v-model="sendPrivateMsg"/>
            </div>
            <div>
              <button id="sendMsg" class="btn btn-primary col-md-4" v-on:click="sendMsgHandle">发送消息</button>
            </div>         
          </div>  
        </div>

      </div>
      
    </div>
  </div>
</template>

<script>
import Hello from './components/Hello'
import $ from 'jquery'

require('bootstrap/dist/css/bootstrap.css')

export default {
  name: 'app',
  data(){
    return{
      roomList:{},//房间列表
      userName:'未知',//用户名
      signupName:'',//登录的名
      sessionId:'未知',
      msgList:[],//聊天记录
      sendChatMsg:'',
      playerList:[],//玩家列表
      playerSelected:'',//选择私聊的用户
      roomId:'未知',//房间账号
      members:[],//成员，
      groupmsgs:[],//消息列表
      sendPrivateMsg:'',//私聊
      uuid:"",
      roomSelected:"-1",//选择的房间号
    }
  },
  components: {

  },
  created(){
    
  },
  methods:{
    connectSocket(){
      const self = this;
      $.getJSON("http://littlegame.tianyu.163.com/master/app?callback=?&uuid="+self.uuid, function(data){
          // 创建一个Socket实例					
					socket = new WebSocket('ws://' + data.ip + ':'+data.port+'/websocket'); 
          // 打开Socket 
					socket.onopen = function(event) { 
						console.log('Client open a message',event.data); 
					};

          // 监听消息
					socket.onmessage = function(event) { 
						console.log('Client received a message', event.data);
						data = JSON.parse(event.data);
						if(data.content.code == 1){
							if(data.rpcMethodName.toLowerCase() == "/player/reg") {
                self.sessionId=data.content.payload.sessionId.toString();
                self.userName=data.content.payload.roleName;
							}

							if(data.rpcMethodName.toLowerCase() == "/player/login") {
                self.sessionId=data.content.payload.sessionId;
                self.userName=data.content.payload.roleName;
							}

							if(data.rpcMethodName.toLowerCase() == "/player/chat") {
                self.msgList.push(data.content.source.playerName+" "+data.content.payload.msg);
							}

							if(data.rpcMethodName.toLowerCase() == "/player/list") {
								console.log(data.content.payload);
                self.playerList=data.content.payload;
							}

							if(data.rpcMethodName.toLowerCase() == "/room/create"){
                console.log(data.content.payload);
                self.roomId=data.content.payload;								
								alert("创建房间" + data.content.payload);
							}

							if(data.rpcMethodName.toLowerCase() == "/room/join"){
								console.log(data.content.payload);
                self.roomId=data.content.payload;
								alert("加入房间" + data.content.payload);
							}

							if(data.rpcMethodName.toLowerCase() == "/room/info"){
								console.log(data.content.payload);					
                self.members = data.content.payload.members;
							}

							if(data.rpcMethodName.toLowerCase() == "/room/chat"){
								console.log(data.content.payload);	
                self.groupmsgs.push(data.content.payload);
							}

							if(data.rpcMethodName.toLowerCase() == "/room/list"){
								console.log(data.content.payload);
                self.roomList = data.content.payload;
							}
						}else{
							console.log(data.content.payload);
							alert(data.message);
						}

					};

           // 监听Socket的关闭
					socket.onerror = function(event) { 
						console.log('Client notified socket has error',event.data); 
					}; 
					// 监听Socket的关闭
					socket.onclose = function(event) { 
						console.log('Client notified socket has closed',event.data); 
					};

      });
    },
    registerBtnHandle(){
        console.log("注册用户");
        var msg = {rpcMethod:"/player/reg", 
          params:[this.signupName.trim(),this.uuid]
        };
        socket.send(window.JSON.stringify(msg));
    },
    loginBtnHandle(){
        var msg = {rpcMethod:"/player/login", 
          params:[this.signupName.trim(),],
          sessionId:$("#name").val().trim()
        };
        socket.send(window.JSON.stringify(msg));
    },
    refreshHandle(){
      var msg = {rpcMethod:"/player/list", 
        sessionId:this.sessionId
      };
      socket.send(window.JSON.stringify(msg));
    },
    sendMsgHandle(){
      var msg = {
        rpcMethod:"/player/chat", 
        params:[parseInt(this.playerSelected),this.sendPrivateMsg],
        sessionId:this.sessionId
      };
      socket.send(window.JSON.stringify(msg));
    },
    cBtnHandle(){
      console.log("创建房间");
      var msg = {
        rpcMethod:"/room/create", 
        params:[1, 10],
        sessionId:this.sessionId
      };
      socket.send(window.JSON.stringify(msg));
    },
    addRoomBtnHandle(){
      if(this.roomSelected!=-1){
        var msg = {
          rpcMethod:"/room/join", 
          params:[parseInt(this.roomSelected)],
          sessionId:this.sessionId
        };
        socket.send(window.JSON.stringify(msg));
      }
    },
    gBtnHandle(){
      if(this.roomSelected!=-1){
          var msg = {
          rpcMethod:"/room/chat", 
          params:[parseInt(this.roomSelected),this.sendChatMsg.trim()],
          sessionId:this.sessionId
        };
        socket.send(window.JSON.stringify(msg));
      }
    },
    roomListBtnHandle(){
      var msg = {
        rpcMethod:"/room/list", 
        sessionId:this.sessionId
      };
      socket.send(window.JSON.stringify(msg));
    }

  }
}

var socket;
</script>

<style scoped>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /*text-align: center;*/
  color: #2c3e50;
  margin-top: 60px;
}
h1{font-size: 50px;color: #2c3e50;}
.form-inline{margin-bottom: 10px;}
.chatCnt{width: 100%;border: 1px solid #357ebd;height: 400px;border-radius: 10px;}
.row{margin-top: 10px;}
.sendBtnCnt{text-align: right;}
.sendBtn{width: 100%;}
.msgCnt{width: 100%;height: 400px;border: 1px solid #357ebd;border-radius: 10px;}
body{padding-bottom: 200px;}
</style>
