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