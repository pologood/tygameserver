export default{
    rootVue:null,
    connectData:null,
    socket:null,
    router:null,
    roomId:0,
    avatarId:0,
    player:{
        roleName:''
    },
    count:0,
    ownerName:'',
    state:0,
    maxSize:0,
    members:{},
    groupmsgs:[],
    painterId:0,
    border:null,
    hint:{
        hint1:'',
        hint2:''
    },
    answerList:[],
    connectSocket(data){
        const self =this;
        // 创建一个Socket实例		
        this.connectData=data;
		this.socket = new WebSocket('ws://' + this.connectData.ip + ':'+this.connectData.port+'/websocket'); 

        // 打开Socket 
		this.socket.onopen = function(event) { 
            console.log('Client open a message',event.data); 
            self.router.push('create');
        };
         // 监听消息
        this.socket.onmessage = function(event) { 
            console.log('Client received a message', event.data);
            data = JSON.parse(event.data);
            if(data.content.code == 1){
                // if(data.rpcMethodName.toLowerCase() == "/player/reg") {
                //     self.sessionId=data.content.payload.sessionId.toString();
                //     self.userName=data.content.payload.roleName;
                // }

                // if(data.rpcMethodName.toLowerCase() == "/player/login") {
                //     self.sessionId=data.content.payload.sessionId;
                //     self.userName=data.content.payload.roleName;
                // }

                // if(data.rpcMethodName.toLowerCase() == "/player/chat") {
                //     self.msgList.push(data.content.source.playerName+" "+data.content.payload.msg);
                // }

                // if(data.rpcMethodName.toLowerCase() == "/player/list") {
                //     console.log(data.content.payload);
                //     self.playerList=data.content.payload;
                // }

                if(data.rpcMethodName.toLowerCase() == "/room/create"){
                    console.log(data.content.payload);
                    self.roomId=data.content.payload;	
                    self.router.push('room');							
                    // alert("创建房间" + data.content.payload);
                }

                if(data.rpcMethodName.toLowerCase() == "/room/join"){
                    console.log(data.content.payload);
                    self.roomId=data.content.payload;
                    self.router.push('room');	
                    // alert("加入房间" + data.content.payload);
                }

                if(data.rpcMethodName.toLowerCase() == "/room/info"){
                    console.log(data.content.payload);					
                    // self.members = data.content.payload.members;
                }

                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/roominfo"){
                    console.log(data.content.payload);	
                    self.ownerName = data.content.payload.ownerName;
                    self.members = data.content.payload.members;
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

                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/startgame"){
                    self.painterId = data.content.payload;
                    self.router.push('draw');
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
                }

                if(data.rpcMethodName.toLowerCase() == "/room/broadcast/answer"){
                    self.answerList.push(data.content);
                }

                // if(data.rpcMethodName.toLowerCase() == "/room/list"){
                //     console.log(data.content.payload);
                //     self.roomList = data.content.payload;
                // }

            }else{
                console.log(data.content.payload);
                // alert(data.message);
            }

        };

        // 监听Socket的关闭
        this.socket.onerror = function(event) { 
            console.log('Client notified socket has error',event.data); 
        }; 
        // 监听Socket的关闭
        this.socket.onclose = function(event) { 
            console.log('Client notified socket has closed',event.data); 
        };

    },
    createRoom(){
        console.log('createRoom')
        var msg = {
            rpcMethod:"/room/create", 
            params:{
                gameId:this.player.gameId,
                maxSize:10
            },
            gameId:this.player.gameId,
            uuid:this.player.uuid
        };
        this.socket.send(window.JSON.stringify(msg));
    },
    joinRoom(){
        console.log('joinRoom')
        var msg = {
            rpcMethod:"/room/join", 
            params:{
                roomId:this.roomId
            },
            gameId:this.player.gameId,
            uuid:this.player.uuid
        };
        this.socket.send(window.JSON.stringify(msg));
    },
    ready(){
        console.log('ready')
        var msg = {
            rpcMethod:"/avatar/ready", 
            params:{
            },
            gameId:this.player.gameId,
            uuid:this.player.uuid
        };
        this.socket.send(window.JSON.stringify(msg));
    },
    sendMsg(msg){
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
    startGame(){
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
    sendDrawingInfo(info){
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
    sendQuestion(info){
        var msg = {
            rpcMethod:"/guess/question", 
            params:{
                roomId:this.roomId,
                answer:info.word,
                hint1:info.hint1,
                hint2:info.hint2
            },
            gameId:this.player.gameId,
            uuid:this.player.uuid
        };
        this.socket.send(window.JSON.stringify(msg));
    },
    sendAnswer(word){
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

}