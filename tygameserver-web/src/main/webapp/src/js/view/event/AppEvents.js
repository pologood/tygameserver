puremvc.define(
	{
		name:'drawsomething.view.event.AppEvents'
	},
	{

	},
	//Static MEMBERS
	{
		SEND_MSG:'send_msg',
		CONNECT_SOCKET:'connect_socket',
		URS_LOGIN_SUCCESS:'urs_login_success',
		ROLE_CONFIRM:'role_confirm',
		CREATE_ROOM:'create_room',
		JOIN_ROOM:'join_room',
		GET_LOGIN_STATUS:'get_login_status',
		createEvent:function(eventName){
			var event;
			if(document.createEvent){
				event=document.createEvent('Events');
				event.initEvent(eventName,false,false);
			}else if(document.creatEventObject){
				event=document.creatEventObject();
			}
			return event;
		},
		addEventListener:function(object,type,listener,useCapture){
			if(object.addEventListener){
				object.addEventListener(type,listener,useCapture);
			}else if(object.fireEvent){
				object.fireEvent(event.type,event);
			}
		},
		dispatchEvent:function(object,event){
			if(object.dispatchEvent){
				object.dispatchEvent(event);
			}else if(object.fireEvent){
				object.fireEvent(event.type,event);
			}
		}
	}

)