puremvc.define({
		name:'drawsomething.controller.command.PrepViewCommand',
		parent:puremvc.SimpleCommand
	},
	//INSTANCE MEMBERS
	{
		execute:function(note){
			this.facade.registerMediator(new drawsomething.view.mediator.DrawPanelMediator);
			this.facade.registerMediator(new drawsomething.view.mediator.ReadyPanelMediator);
			this.facade.registerMediator(new drawsomething.view.mediator.LoginPanelMediator);
			this.facade.registerMediator(new drawsomething.view.mediator.CommonAlertMediator);
		}
	}

)
