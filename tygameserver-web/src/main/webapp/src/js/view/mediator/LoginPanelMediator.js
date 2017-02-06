puremvc.define({
        name: 'drawsomething.view.mediator.LoginPanelMediator',
        parent: puremvc.Mediator
    },
    {
        // Notifications this mediator is interested in 
        listNotificationInterests: function() {
            return [
                drawsomething.AppConstants.URS_LOGIN_SUCCESS,
                drawsomething.AppConstants.URS_UNLOGIN
            ];
        },
        
        // Code to be executed when the Mediator instance is registered with the View
        onRegister: function() {
            this.setViewComponent( new drawsomething.view.component.LoginPanel);
            this.viewComponent.addEventListener( drawsomething.view.event.AppEvents.URS_LOGIN_SUCCESS, this );
            this.viewComponent.addEventListener( drawsomething.view.event.AppEvents.GET_LOGIN_STATUS, this );
        },
        
        // Handle events from the view component
        handleEvent: function ( event ) {            
            switch( event.type ) {
                case drawsomething.view.event.AppEvents.URS_LOGIN_SUCCESS:
                    this.sendNotification( drawsomething.AppConstants.GET_ROLELIST, event.msg );
                    this.viewComponent.showRoleList();
                break;
                case drawsomething.view.event.AppEvents.ROLE_CONFIRM:
                    // this.sendNotification(drawsomething.AppConstants.)
                break;
             }
            
        },
 
        // Handle notifications from other PureMVC actors
        handleNotification: function( note ) {
            switch ( note.getName() ) {
                case drawsomething.AppConstants.URS_LOGIN_SUCCESS:
                    this.viewComponent.showRoleList();
                break;
                case drawsomething.AppConstants.URS_UNLOGIN:
                    this.viewComponent.showLogin();
                break;
            }
        },
    },
 
    // STATIC MEMBERS
    {
        NAME: 'LoginPanelMediator'
    }    
);
