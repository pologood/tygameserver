puremvc.define({name:"drawsomething.model.proxy.DrawSomethingProxy",parent:puremvc.Proxy},{gameId:1,rolesList:[],gbId:"",roleName:"",avatarImg:"",socket:null,connectData:null,roomId:0,roominfo:null,avatarId:0,gameInfo:null,player:{roleName:""},count:0,ownerName:"",hint:{hint1:"",hint2:""},questions:[],answerList:[],answerInfo:null,gameState:0,selfName:"",likeCount:0,unlikeCount:0,host:"http://littlegame.tianyu.163.com:8090",onRegister:function(){this.getLoginStatus()},getRoleList:function(){var o=this;$.getJSON(this.host+"/master/avatar/roles?callback=?&gameId="+this.gameId,function(t){if(1==t.code){o.rolesList=[];for(var e in t.payload){var a=e;for(var n in t.payload[e]){var s=t.payload[e][n];o.rolesList.push({serverName:a,gbId:s.gbId,playerName:s.playerName,school:s.school,level:s.level})}}}o.sendNotification(drawsomething.AppConstants.GET_ROLELIST_SUCCESS,{rolesList:o.rolesList}),0==o.rolesList.length&&o.sendNotification(drawsomething.AppConstants.SHOW_ALERT,{txt:"\u4f60\u8fd8\u6ca1\u6709\u5929\u8c15\u6e38\u620f\u89d2\u8272\uff1f\u521b\u5efa\u4e00\u4e2a\u89d2\u8272\u5c31\u53ef\u4ee5\u73a9\u4f60\u753b\u6211\u731c\u5566",code:0})})},getConnectData:function(){var o=this;$.getJSON(this.host+"/master/init?callback=?&uuid="+this.gbId+"&roleName="+this.roleName+"&avatarImg="+encodeURIComponent(this.avatarImg)+"&gameId="+this.gameId+"&roomId="+this.roomId,function(t){o.connectSocket(t.payload),o.avatarId=t.payload.avatarId})},getLoginStatus:function(){var o=this;$.getJSON(this.host+"/master/isLogin?callback=?&gameId="+this.gameId,function(t){1==t.code?o.sendNotification(drawsomething.AppConstants.URS_LOGIN_SUCCESS,{}):o.sendNotification(drawsomething.AppConstants.URS_UNLOGIN,{})})},roleConfirm:function(o){this.gbId=o.gbId,this.roleName=o.roleName},setRoomId:function(o){this.roomId=o.roomId,null==this.socket&&this.getConnectData()},connectSocket:function(o){var t=this;this.connectData=o,this.socket=new WebSocket("ws://"+this.connectData.ip+":"+this.connectData.port+"/ws"),this.socket.onopen=function(o){console.log("Client open a message",o.data),0==t.roomId?t.createRoom():t.joinRoom()},this.socket.onmessage=function(e){if(console.log("Client received a message",e.data),o=JSON.parse(e.data),1==o.content.code){if("/room/create"==o.rpcMethod.toLowerCase()&&(t.roomId=o.content.payload),"/room/join"==o.rpcMethod.toLowerCase()&&(t.roominfo=o.content.payload,t.sendNotification(drawsomething.AppConstants.BROADCAST_ROOMINFO,{info:o.content.payload,roominfo:t.roominfo,avatarId:t.avatarId})),"/room/info"==o.rpcMethod.toLowerCase(),"/room/broadcast/info"==o.rpcMethod.toLowerCase()&&(t.ownerName=o.content.payload.ownerName,t.members=o.content.payload.members,t.roominfo=o.content.payload,t.sendNotification(drawsomething.AppConstants.BROADCAST_ROOMINFO,{info:o.content.payload,roominfo:t.roominfo,avatarId:t.avatarId})),"/room/broadcast/join"==o.rpcMethod.toLowerCase()){if(console.log(o.content.payload),o.content.payload.avatar.avatarId==t.avatarId)return;t.roominfo.members.push(o.content.payload.avatar),t.sendNotification(drawsomething.AppConstants.BROADCAST_JOIN,{member:o.content.payload,roominfo:t.roominfo,avatarId:t.avatarId})}if("/room/broadcast/ready"==o.rpcMethod.toLowerCase()&&(t.sendNotification(drawsomething.AppConstants.BROADCAST_READY,{info:o.content.payload}),console.log(o.content.payload)),"/room/broadcast/guess/start"==o.rpcMethod.toLowerCase()&&(t.gameInfo=o.content.payload,t.sendNotification(drawsomething.AppConstants.GAME_STARTING,{gameInfo:t.gameInfo,avatarId:t.avatarId,roominfo:t.roominfo})),"/room/private/guess/quesion"==o.rpcMethod.toLowerCase()&&(t.answerInfo=o.content.payload,t.sendNotification(drawsomething.AppConstants.ANSWER_INFO,o.content.payload)),"/room/private/remove"==o.rpcMethod.toLowerCase()&&t.sendNotification(drawsomething.AppConstants.SHOW_ALERT,{code:1,txt:"\u4f60\u88ab\u623f\u4e3b\u8bf7\u51fa\u4e86\u623f\u95f4\uff0c\u53bb\u5176\u4ed6\u623f\u95f4\u770b\u770b\u5427"}),"/avatar/ready"==o.rpcMethod.toLowerCase()&&(self.state=1),"/room/chat"==o.rpcMethod.toLowerCase()&&(console.log(o.content.payload),self.groupmsgs.push({msg:o.content.payload.msg,from:o.content.source.avatarName})),"/guess/create"==o.rpcMethod.toLowerCase(),"/guess/answer"==o.rpcMethod.toLowerCase(),"/room/private/startgame"==o.rpcMethod.toLowerCase()&&(self.questions=o.content.payload),"/room/broadcast/remove"==o.rpcMethod.toLowerCase()){for(var a=o.content.payload,n=t.roominfo.members,s=n.length-1;s>=0;s--)a==n[s].avatarId&&n.splice(s,1);t.sendNotification(drawsomething.AppConstants.RECEIVE_REMOVE_PLAYER,{avatarId:a})}if("/room/broadcast/drawgame"==o.rpcMethod.toLowerCase()){var r=o.content.payload;t.sendNotification(drawsomething.AppConstants.DRAWING_HANDLE,r)}if("/room/broadcast/drawquestion"==o.rpcMethod.toLowerCase()&&(self.hint=o.content.payload,self.gameState=o.content.payload.gameState),"/room/broadcast/guess/exit"==o.rpcMethod.toLowerCase()){for(var a=o.content.payload,n=t.roominfo.members,s=n.length-1;s>=0;s--)a==n[s].avatarId&&n.splice(s,1);t.sendNotification(drawsomething.AppConstants.RECEIVE_REMOVE_PLAYER,{avatarId:a})}if("/room/broadcast/guess/answer"==o.rpcMethod.toLowerCase()){var i=o.content.payload.fAnswer;i.isCorrect=o.content.payload.isCorrect,t.answerList.push(i),t.sendNotification(drawsomething.AppConstants.RECEIVE_MSG,i),o.content.payload.info&&o.content.payload.info.scores&&t.sendNotification(drawsomething.AppConstants.RECEIVE_SCORES,{scores:o.content.payload.info.scores,members:t.roominfo.members})}if("/room/broadcast/guess/hint1"==o.rpcMethod.toLowerCase()&&t.sendNotification(drawsomething.AppConstants.RECEIVE_HINT,{type:1,hint:o.content.message}),"/room/broadcast/guess/hint2"==o.rpcMethod.toLowerCase()&&t.sendNotification(drawsomething.AppConstants.RECEIVE_HINT,{type:2,hint:o.content.message}),"/room/broadcast/guess/countdown"==o.rpcMethod.toLowerCase()){var d=o.content.payload;t.sendNotification(drawsomething.AppConstants.COUNTDOWN,d)}if("/room/broadcast/guess/roundover"==o.rpcMethod.toLowerCase()){var c=o.content.payload;t.sendNotification(drawsomething.AppConstants.ROUND_OVER,c)}if("/room/broadcast/guess/like"==o.rpcMethod.toLowerCase()&&(t.likeCount++,t.sendNotification(drawsomething.AppConstants.RECEIVE_LIKE_INFO,{like:t.likeCount,unlike:t.unlikeCount}),t.sendNotification(drawsomething.AppConstants.RECEIVE_SCORES,{scores:o.content.payload.scores,members:t.roominfo.members})),"/room/broadcast/guess/unlike"==o.rpcMethod.toLowerCase()&&(t.unlikeCount++,t.sendNotification(drawsomething.AppConstants.RECEIVE_LIKE_INFO,{like:t.likeCount,unlike:t.unlikeCount})),"/room/broadcast/guess/running"==o.rpcMethod.toLowerCase()){var c=o.content.payload;t.gameInfo.drawerId=c.drawerId,t.sendNotification(drawsomething.AppConstants.GAME_STARTING,{gameInfo:t.gameInfo,avatarId:t.avatarId,roominfo:t.roominfo}),t.likeCount=0,t.unlikeCount=0}if("/room/broadcast/guess/gameover"==o.rpcMethod.toLowerCase()&&(t.sendNotification(drawsomething.AppConstants.GAME_OVER),t.sendNotification(drawsomething.AppConstants.BROADCAST_ROOMINFO,{info:t.roominfo,roominfo:t.roominfo,avatarId:t.avatarId})),"/room/broadcast/changeowner"==o.rpcMethod.toLowerCase()){var a=o.content.payload;t.roominfo.ownerId=a,t.sendNotification(drawsomething.AppConstants.CHANGE_OWNER,a)}if("/room/broadcast/correct"==o.rpcMethod.toLowerCase()){var i=o.content.payload;i.name=getName(i.avatarId),i.correct=!0,self.answerList.push(i),self.border.showWinner(i.name)}}else console.log(o.content.payload)},this.socket.onerror=function(o){console.log("Client notified socket has error",o.data)},this.socket.onclose=function(o){console.log("Client notified socket has closed",o.data)}},createRoom:function(){console.log("createRoom");var o={rpcMethod:"/room/create",params:{gameId:this.gameId,maxSize:10},gameId:this.gameId,uuid:this.gbId};this.socket.send(JSON.stringify(o)),console.log(o)},joinRoom:function(){console.log("joinRoom");var o={rpcMethod:"/room/join",params:{roomId:this.roomId},gameId:this.gameId,uuid:this.gbId};this.socket.send(JSON.stringify(o))},removePlayer:function(o){var t={rpcMethod:"/room/remove",params:{avatarId:o.avatarId},gameId:this.gameId,uuid:this.gbId};this.socket.send(JSON.stringify(t))},ready:function(){console.log("ready");var o={rpcMethod:"/avatar/ready",params:{},gameId:this.gameId,uuid:this.gbId};this.socket.send(window.JSON.stringify(o))},startGame:function(){console.log("startGame");var o={rpcMethod:"/guess/start",params:{roomId:this.roomId},gameId:this.gameId,uuid:this.gbId};this.socket.send(window.JSON.stringify(o))},sendDrawingInfo:function(o){var t={rpcMethod:"/guess/draw",params:{roomId:this.roomId,content:o},gameId:this.gameId,uuid:this.gbId};this.socket.send(window.JSON.stringify(t))},sendQuestion:function(){var o={rpcMethod:"/guess/question",params:{roomId:this.roomId,answer:info.answer,hint1:info.hint1,hint2:info.hint2},gameId:this.player.gameId,uuid:this.player.uuid};this.socket.send(window.JSON.stringify(o))},sendAnswer:function(o){var t={rpcMethod:"/guess/answer",params:{roomId:this.roomId,answer:o.text},gameId:this.gameId,uuid:this.gbId};this.socket.send(window.JSON.stringify(t))},like:function(){var o={rpcMethod:"/guess/like",params:{roomId:this.roomId},gameId:this.gameId,uuid:this.gbId};this.socket.send(window.JSON.stringify(o))},unlike:function(){var o={rpcMethod:"/guess/unlike",params:{roomId:this.roomId},gameId:this.gameId,uuid:this.gbId};this.socket.send(window.JSON.stringify(o))}},{NAME:"DrawSomethingProxy"});