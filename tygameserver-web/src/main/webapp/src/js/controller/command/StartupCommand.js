puremvc.define({
		name:'drawsomething.controller.command.StartupCommand',
		parent:puremvc.MacroCommand
	},
	{
		initializeMacroCommand:function(){
			this.addSubCommand(drawsomething.controller.command.PrepControllerCommand);
			this.addSubCommand(drawsomething.controller.command.PrepModelCommand);
			this.addSubCommand(drawsomething.controller.command.PrepViewCommand);
			
		}
	}
)