puremvc.define({
	name:'drawsomething.controller.command.SocketCommand',
	parent:puremvc.SimpleCommand
},
{
	execute:function(note){
		var proxy=this.facade.retrieveProxy(drawsomething.model.proxy.DrawSomethingProxy.NAME);
		switch(note.getName()){
			case drawsomething.AppConstants.CONNECT_SOCKET:
				console.log(note.getBody());
				proxy.getConnectData(note.getBody());
			break;
			case drawsomething.AppConstants.START_GAME:
				proxy.startGame();
			break;
			case drawsomething.AppConstants.GET_ROLELIST:
				proxy.getRoleList();
			break;
			case drawsomething.AppConstants.CHECK_LOGIN:
				proxy.getLoginStatus();
			break;
			case drawsomething.AppConstants.ROLE_CONFIRM:
				proxy.roleConfirm(note.getBody());
			break;
			case drawsomething.AppConstants.JOIN_ROOM:
				proxy.setRoomId(note.getBody());
			break;
			case drawsomething.AppConstants.CREATE_ROOM:
				proxy.setRoomId(note.getBody());
			break;
			case drawsomething.AppConstants.READY:
				proxy.ready();
			break;
			case drawsomething.AppConstants.DRAWING:
				proxy.sendDrawingInfo(note.getBody());
			break;
			case drawsomething.AppConstants.SEND_MSG:
				proxy.sendAnswer(note.getBody());
			break;
		}
	}
})