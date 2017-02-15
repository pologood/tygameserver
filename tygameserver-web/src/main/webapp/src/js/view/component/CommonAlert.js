puremvc.define({
		name:'drawsomething.view.component.CommonAlert',
		constructor:function(event){
			var _this=this;
			this.drawsomethingApp=document.querySelector( '#connectPanel');
			this.$container=$("#ds");
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
		hide:function(){
			$("#connectPanel").hide();
		},
		show:function(){
			$("#connectPanel").show();
		}
	},
	{
		NAME:'CommonAlert'
	}
)