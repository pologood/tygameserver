puremvc.define({
        name: 'drawsomething.view.mediator.DrawPanelMediator',
        parent: puremvc.Mediator
    },
    {
        // Notifications this mediator is interested in 
        listNotificationInterests: function() {
            return [
                drawsomething.AppConstants.GAME_STARTING,
                drawsomething.AppConstants.DRAWING_HANDLE,
                drawsomething.AppConstants.ANSWER_INFO,
                drawsomething.AppConstants.RECEIVE_MSG,
                drawsomething.AppConstants.RECEIVE_HINT,
                drawsomething.AppConstants.ROUND_OVER,
                drawsomething.AppConstants.COUNTDOWN,
                drawsomething.AppConstants.RECEIVE_SCORES,
                drawsomething.AppConstants.RECEIVE_LIKE_INFO,
            ];
        },
        
        // Code to be executed when the Mediator instance is registered with the View
        onRegister: function() {
            this.setViewComponent( new drawsomething.view.component.DrawPanel);
            this.viewComponent.addEventListener( drawsomething.view.event.AppEvents.SEND_MSG, this );
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.DRAWING,this);
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.DELETE,this);
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.LIKE,this);
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.UNLIKE,this);
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.SEND_POS);
            this.startTimer();
            
        },
        startTimer:function(){
            var _this=this;
            var fps=10;
            function timer(){
                _this.sendPos();
                setTimeout(function(){
                    // requestAnimationFrame(timer);
                    timer();
                },1000/fps);
            }
            timer();
        },
        
        // Handle events from the view component
        handleEvent: function ( event ) {
            switch( event.type ) {
                case drawsomething.view.event.AppEvents.SEND_MSG:
                    this.sendNotification( drawsomething.AppConstants.SEND_MSG, event.msg );
                break;
                case drawsomething.view.event.AppEvents.DRAWING:
                    this.sendNotification(drawsomething.AppConstants.DRAWING,event.msg);
                break;
                case drawsomething.view.event.AppEvents.DELETE:
                    this.sendNotification(drawsomething.AppConstants.DRAWING,event.msg);
                break;
                case drawsomething.view.event.AppEvents.LIKE:
                    this.sendNotification(drawsomething.AppConstants.SEND_LIKE,event.msg);
                break;
                case drawsomething.view.event.AppEvents.UNLIKE:
                    this.sendNotification(drawsomething.AppConstants.SEND_UNLIKE,event.msg);
                break;
                case drawsomething.view.event.AppEvents.SEND_POS:
                    // this.sendNotification(drawsomething.AppConstants.DRAWING,event.msg);
                break;

             }
            
        },
        sendPos:function(){
            if(this.viewComponent.isDrawer&&this.viewComponent.sendPosArray.length>0){
                var copyArr = this.viewComponent.sendPosArray.slice(); 
                this.viewComponent.sendPosArray=[];
                this.sendNotification(drawsomething.AppConstants.DRAWING,{list:copyArr});
                
            }   
        },
        // Handle notifications from other PureMVC actors
        handleNotification: function( note ) {
            switch ( note.getName() ) {
                case drawsomething.AppConstants.GAME_STARTING:
                    this.viewComponent.show();
                    this.viewComponent.roundStart(note.getBody());
                break;
                case drawsomething.AppConstants.DRAWING_HANDLE:
                    // this.viewComponent.drawingHandle(note.getBody());
                    this.viewComponent.receivePos(note.getBody());
                break;
                case drawsomething.AppConstants.ANSWER_INFO:
                    this.viewComponent.updateAnswerInfo(note.getBody());
                break;
                case drawsomething.AppConstants.RECEIVE_MSG:
                    this.viewComponent.receiveMsg(note.getBody());
                break;
                case drawsomething.AppConstants.RECEIVE_HINT:
                    this.viewComponent.receiveHint(note.getBody());
                break;
                case drawsomething.AppConstants.ROUND_OVER:
                    this.viewComponent.roundOver(note.getBody());
                break;
                case drawsomething.AppConstants.COUNTDOWN:
                    this.viewComponent.updateCountDown(note.getBody());
                break;
                case drawsomething.AppConstants.RECEIVE_SCORES:
                    this.viewComponent.receiveScores(note.getBody())
                break;
                case drawsomething.AppConstants.RECEIVE_LIKE_INFO:
                    this.viewComponent.receiveLikeInfo(note.getBody());
                break;
            }
        },
    },
 
    // STATIC MEMBERS
    {
        NAME: 'DrawPanelMediator'
    }    
);
