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
		update:function(data,info){
			this.data=data;
			var html    = this.template(data);
			this.el=$(html);
			if(info.avatarId==data.avatarId){
				if(data.state==1){
					this.el.find(".btn-already").show();
				}else{
					this.el.find(".btn-ready").show();
				}
			}else{
				if(data.state==1){
					this.el.find(".btn-already").show();
				}else{
					this.el.find(".btn-unready").show();
				}
			}
		}
	},
	{
		NAME:'PlayerItem'
	}
)