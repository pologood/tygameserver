puremvc.define({name:"drawsomething.view.component.LoginPanel",constructor:function(){var n=this;this.container=document.querySelector("#loginPanel"),this.$container=$("#loginPanel");var t=new URS({product:"d21",promark:"dNvgFOK",host:"hd.tianyu.163.com",includeBox:"login",needanimation:0,placeholder:{account:"\u7f51\u6613\u901a\u884c\u8bc1"}});t.logincb=t.regcb=function(t){n.dispatchLoginSuccess(t)},this.$container.find(".confirmBtn").click(function(){n.dispatchConfirm()}),this.$container.find(".createBtn").click(function(){n.dispatchCreateRoom()}),this.$container.find(".joinBtn").click(function(){n.dispatchJoinRoom()})}},{addEventListener:function(n,t,e){drawsomething.view.event.AppEvents.addEventListener(this.container,n,t,e)},createEvent:function(n){return drawsomething.view.event.AppEvents.createEvent(n)},dispatchEvent:function(n){drawsomething.view.event.AppEvents.dispatchEvent(this.container,n)},dispatchLoginStatus:function(){var n=this.createEvent(drawsomething.view.event.AppEvents.GET_LOGIN_STATUS);n.msg={},this.dispatchEvent(n)},dispatchLoginSuccess:function(n){var t=this.createEvent(drawsomething.view.event.AppEvents.URS_LOGIN_SUCCESS);t.msg={urs:n},this.dispatchEvent(t)},dispatchCreateRoom:function(){var n=this.createEvent(drawsomething.view.event.AppEvents.CREATE_ROOM);n.msg={roomId:0},this.dispatchEvent(n)},dispatchJoinRoom:function(){var n=this.createEvent(drawsomething.view.event.AppEvents.JOIN_ROOM);return n.msg={roomId:this.$container.find(".roomIdIpt").val()},""==n.msg.roomId?void alert("\u8bf7\u8f93\u5165\u623f\u95f4\u53f7"):void this.dispatchEvent(n)},dispatchConfirm:function(){var n=this.createEvent(drawsomething.view.event.AppEvents.ROLE_CONFIRM);return n.msg={gbId:this.$container.find("input[type='radio']:checked").val(),roleName:this.$container.find("input[type='radio']:checked").attr("data-rolename")},void 0==n.msg.gbId?void alert("\u8bf7\u9009\u62e9\u89d2\u8272"):void this.dispatchEvent(n)},showRoleList:function(){this.$container.find(".loginCnt").hide(),this.$container.find(".selectRole").show(),this.$container.find(".entrance").hide()},showLogin:function(){this.$container.find(".loginCnt").show(),this.$container.find(".selectRole").hide(),this.$container.find(".entrance").hide()},showEntrance:function(){this.$container.find(".loginCnt").hide(),this.$container.find(".selectRole").hide(),this.$container.find(".entrance").show()},updateRoleList:function(n){var t=$("#roleItems-template").html(),e=Handlebars.compile(t),i={rolesList:n.rolesList},o=e(i);this.$container.find(".radioCnt").html(o)},hide:function(){this.container.hide()},show:function(){this.container.show()}},{NAME:"LoginPanel"});