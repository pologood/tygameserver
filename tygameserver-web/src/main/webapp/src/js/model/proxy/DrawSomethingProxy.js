puremvc.define({
		name:'drawsomething.model.proxy.DrawSomethingProxy',
		parent:puremvc.Proxy
	},
	{	
		gameId:1,
		socket:null,
		connectData:null,
		roomId:0,
		avatarId:0,
		player:{
			roleName:''
		},
		count:0,
		ownerName:'',
		hint:{
			hint1:'',
			hint2:''
		},
		questions:[],
		answerList:[],
		gameState:0,
		selfName:'',
		getRoleList:function(){
			var _this=this;
			$.getJSON("http://liangchong.163.com:8090/master/avatar/roles?callback=?&gameId="+this.gameId,function(msg){
				console.log(msg)
			})
		},
		getConnectData:function(obj){
			var _this=this;
			$.getJSON("http://10.241.4.111:8090/master/init?callback=?&uuid="+obj.uuid+"&roleName="+obj.roleName+"&avatarImg="+encodeURIComponent(obj.avatarImg)+"&gameId="+obj.gameId+"&roomId="+obj.roomId, function(data){
                console.log(data)
                _this.connectSocket(data);
            })
		},
		connectSocket:function(data){
			console.log("proxy conenct socket");
			var _this=this;
			this.connectData=data;
			this.socket=new WebSocket('ws://'+this.connectData.ip+':'+this.connectData.port+'/websocket');
			this.socket.onopen=function(event){
				console.log("Client open a message",event.data);
				_this.sendNotification(drawsomething.AppConstants.CONNECT_SUCCESS,{});
			}

			this.socket.onmessage=function(event){
				console.log("Client received a message",event.data);
				data=JSON.parse(event.data);

				if(data.content.code==1){
					if(data.rpcMethodName.toLowerCase() == "/room/create"){
	                    console.log(data.content.payload);
	                    self.roomId=data.content.payload;	
	                    // self.router.push('room');			
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/join"){
	                    console.log(data.content.payload);
	                    self.roomId=data.content.payload;
	                    // self.router.push('room');	
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/info"){
	                    console.log(data.content.payload);					
	                    // self.members = data.content.payload.members;
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/roominfo"){
	                    console.log(data.content.payload);	
	                    self.ownerName = data.content.payload.ownerName;
	                    self.members = data.content.payload.members;
	                    self.selfName = getName(self.avatarId);
	                }

	                if(data.rpcMethodName.toLowerCase() == "/avatar/ready"){
	                    self.state=1;
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/chat"){
	                    console.log(data.content.payload);	
	                    self.groupmsgs.push({msg:data.content.payload.msg,from:data.content.source.avatarName});
	                }

	                if(data.rpcMethodName.toLowerCase() ==  "/guess/create"){
                    
                	}

                	if(data.rpcMethodName.toLowerCase() == "/room/private/startgame"){
	                    self.questions = data.content.payload;
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/startgame"){
	                    self.painterId = data.content.payload.avatarId;
	                    self.gameState = data.content.payload.gameState;
	                    self.router.push('draw');
	                    if(self.border){
	                        self.border.replay();
	                    }
	                    
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/drawgame"){
	                    var info=data.content.payload;
	                    if(info.type==1){
	                        self.border.drawing(info.drawInfo);
	                    }else if(info.type==2){
	                        self.border.clear();
	                    }
	                    
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/drawquestion"){
	                    self.hint=data.content.payload;
	                    self.gameState=data.content.payload.gameState;
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/answer"){
	                    data.content.payload.name = getName(data.content.payload.avatarId);
	                    self.answerList.push(data.content.payload);
	                }

	                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/correct"){
	                    var answer = data.content.payload;
	                    answer.name = getName(answer.avatarId);
	                    answer.correct = true;
	                    self.answerList.push(answer);
	                    self.border.showWinner(answer.name);
	                    // alert(answer.avatarId+"回答正确！");
	                }


				}else{
					console.log(data.content.payload);
				}	


			}

			this.socket.onerror=function(event){
				console.log("Client notified socket has error",event.data);
			}

			this.socket.onclose=function(event){
				console.log("Client notified socket has closed",event.data);
			}

		},
		createRoom:function(){
			console.log('createRoom');
			var msg={
				rpcMethodName:"/room/create",
				params:{
					gameId:this.player.gameId,
					maxSize:10
				},
				gameId:this.player.gameId,
				uuid:this.player.uuid
			}
			this.socket.send(JSON.stringify(msg));
		},
		joinRoom:function(){
			console.log("joinRoom");
			var msg={
				rpcMethodName:"room/join",
				params:{
					roomId:this.roomId
				},
				gameId:this.player.gameId,
				uuid:this.player.uuid
			}
			this.socket.send(JSON.stringify(msg));
		},
		ready:function(){
			console.log("ready");
			var msg={
				rpcMethodName:"/avatar/ready",
				params:{

				},
				gameId:this.player.gameId,
				uuid:this.player.uuid
			}
		},
		sendMsg:function(msg){
			console.log('sendMsg')
	        var msg = {
	            rpcMethod:"/room/chat", 
	            params:{
	                roomId:this.roomId,
	                msg:msg
	            },
	            gameId:this.player.gameId,
	            uuid:this.player.uuid
	        };
	        this.socket.send(window.JSON.stringify(msg));
		},
		startGame:function(){
			console.log('startGame')
	        var msg = {
	            rpcMethod:"/guess/create", 
	            params:{
	                roomId:this.roomId
	            },
	            gameId:this.player.gameId,
	            uuid:this.player.uuid
	        };
	        this.socket.send(window.JSON.stringify(msg));
		},
		sendDrawingInfo:function(){
			var msg = {
           		rpcMethod:"/guess/draw", 
	            params:{
	                roomId:this.roomId,
	                content:info
	            },
	            gameId:this.player.gameId,
	            uuid:this.player.uuid
	        };
	        this.socket.send(window.JSON.stringify(msg));
		},
		sendQuestion:function(){
			var msg = {
	            rpcMethod:"/guess/question", 
	            params:{
	                roomId:this.roomId,
	                answer:info.answer,
	                hint1:info.hint1,
	                hint2:info.hint2
	            },
	            gameId:this.player.gameId,
	            uuid:this.player.uuid
	        };
	        this.socket.send(window.JSON.stringify(msg));
		},
		sendAnswer:function(){
			var msg = {
            	rpcMethod:"/guess/answer", 
	            params:{
	                roomId:this.roomId,
	                answer:word
	            },
	            gameId:this.player.gameId,
	            uuid:this.player.uuid
	        };
	        this.socket.send(window.JSON.stringify(msg));
		}

	},
	{
		NAME:'DrawSomethingProxy'
	}
)