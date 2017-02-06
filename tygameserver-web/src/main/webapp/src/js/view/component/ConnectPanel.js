puremvc.define({
		name:'drawsomething.view.component.ConnectPanel',
		constructor:function(event){
			var _this=this;
			this.drawsomethingApp=document.querySelector( '#connectPanel');
			this.connectBtn=this.drawsomethingApp.querySelector(".connectBtn");
			this.connectBtn.component=this;
			drawsomething.view.event.AppEvents.addEventListener(this.connectBtn,'click',function(event){
				this.component.dispatchSendMsg(event);
			})
		}
	},
	{
		addEventListener:function(type,listener,useCapture){
			drawsomething.view.event.AppEvents.addEventListener(this.drawsomethingApp,type,listener,useCapture);
		},
		createEvent:function(eventName){
			return drawsomething.view.event.AppEvents.createEvent(eventName);
		},
		dispatchEvent:function(event){
			drawsomething.view.event.AppEvents.dispatchEvent(this.drawsomethingApp,event);
		},
		dispatchSendMsg:function(e){						
			var sendMsgEvent = this.createEvent( drawsomething.view.event.AppEvents.CONNECT_SOCKET);
			sendMsgEvent.msg={
                roleName:$("#playerName").val(),
                avatarImg:$("#headImg").val(),
                uuid:$("#uuid").val(),
                gameId:1
			};
			this.dispatchEvent(sendMsgEvent);
		},
		hide:function(){
			$("#connectPanel").hide();
		},
		show:function(){
			$("#connectPanel").show();
		}
	},
	{
		NAME:'ConnectPanel'
	}
)