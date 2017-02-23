puremvc.define({
		name:'drawsomething.view.component.CommonAlert',
		constructor:function(event){
			var _this=this;
			this.container=document.querySelector( '#commonAlert');
			this.$container=$("#commonAlert");
			this.$container.find(".confirmBtn").click(function(){
				_this.hide();
				if(_this.code==1){
					window.location.reload();
				}
			})
			this.code=0;

			//临时背景音乐
			this.$audioPanel=$("#audioPanel");
			this.$audioPanel.find(".audioCloseBtn").click(function(){
				_this.$audioPanel.find("audio")[0].pause();
			})
			// $(window).bind('beforeunload',function(e){
			// 	var msg='你是否要退出房间，游戏中退出将扣除10积分，重新进入需要再次消耗游戏币'
			// 	e.returnValue=msg;
   //              return msg;
   //          });
		}
	},
	{
		addEventListener:function(type,listener,useCapture){
			drawsomething.view.event.AppEvents.addEventListener(this.container,type,listener,useCapture);
		},
		createEvent:function(eventName){
			return drawsomething.view.event.AppEvents.createEvent(eventName);
		},
		dispatchEvent:function(event){
			drawsomething.view.event.AppEvents.dispatchEvent(this.container,event);
		},
		hide:function(){
			this.$container.hide();
		},
		show:function(msg){
			this.code=msg.code;
			this.$container.find(".content").html(msg.txt);
			this.$container.show();
		}
	},
	{
		NAME:'CommonAlert'
	}
)