puremvc.define({
		name:'drawsomething.view.component.LoginPanel',
		constructor:function(event){
			var _this=this;
			this.container=document.querySelector( '#loginPanel');
			this.$container=$('#loginPanel');
			var urs = new URS({
                product : 'd21', //【必须】使用的产品ID，如urs
                promark : 'dNvgFOK', //【必须】【申请】申请的组件ID，被分派ID
                host : 'hd.tianyu.163.com',
                includeBox: 'login',
                needanimation: 0,
                placeholder : { account: '网易通行证' }
            });

            urs.logincb = urs.regcb = function (urs) {
                _this.dispatchLoginSuccess(urs)
            };

            this.$container.find(".confirmBtn").click(function(){
            	_this.dispatchConfirm();
            })
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
		dispatchLoginSuccess:function(urs){
			var loginEvent = this.createEvent( drawsomething.view.event.AppEvents.URS_LOGIN_SUCCESS);
			loginEvent.msg={
				urs:urs
			};
			this.dispatchEvent(loginEvent);
		},
		dispatchConfirm:function(){
			var e = this.createEvent( drawsomething.view.event.AppEvents.ROLE_CONFIRM);
			e.msg={
				gbid:this.$container.find("input[type='radio']:checked").val()
			}
			this.dispatchEvent(e);
		},
		showRoleList:function(){
			this.$container.find(".loginCnt").hide();
			this.$container.find(".selectRole").show();
		},
		showLoginList:function(){
			this.$container.find(".loginCnt").show();
			this.$container.find(".selectRole").hide();
		},
		hide:function(){
			this.container.hide();
		},
		show:function(){
			this.container.show();
		}
	},
	{
		NAME:'LoginPanel'
	}
)