puremvc.define(
	{
		name:'drawsomething.view.component.PlayerItem',
		constructor:function(){
			// this.$container=$('<div class="item f-fl"></div>');
			this.el=null;
			var source=$("#playerItems-template").html();
			this.template = Handlebars.compile(source);			
		}
	},
	{
		getName:function(){

		},
		getElement:function(){

		},
		update:function(data,info,avatarId){
			this.data=data;
			var html    = this.template(data);
			this.el=$(html);
			if(avatarId==data.avatarId){
				if(data.state=="READY"){
					this.el.find(".btn-already").css({"display":"block"});
				}else{
					this.el.find(".btn-ready").css({"display":"block"});
				}
			}else{
				if(data.state=="READY"){
					this.el.find(".btn-already").css({"display":"block"});
				}else{
					this.el.find(".btn-unready").css({"display":"block"});
				}				
			}
			if(avatarId!=info.ownerId){
				this.el.find(".closeBtn").hide();
			}
			this.avatarId=data.avatarId;
		},
		ready:function(){
			this.el.find(".btn-unready").css({"display":"none"});
			this.el.find(".btn-ready").css({"display":"none"});
			this.el.find(".btn-already").css({"display":"block"});
		}
	},
	{
		NAME:'PlayerItem'
	}
)