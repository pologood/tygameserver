puremvc.define({
	name:'drawsomething.controller.command.PrepModelCommand',
	parent:puremvc.SimpleCommand
},{
	execute:function(note){
		this.facade.registerProxy(new drawsomething.model.proxy.DrawSomethingProxy());
	}
})