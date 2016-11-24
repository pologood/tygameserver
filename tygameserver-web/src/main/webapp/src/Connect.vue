<template>
    <div>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">连接服务器</h3>
            </div>
            <div class="panel-body">

                <div class="form-horizontal">

                     <div class="form-group">
                        <label class="col-md-2 control-label">玩家名字</label>
                        <div class="col-md-10">
                            <input type="text" class="form-control" v-model="player.roleName" placeholder="请填写您的昵称">
                        </div>                      
                    </div>

                    <div class="form-group">
                        <label class="col-md-2 control-label">uuid</label>
                        <div class="col-md-10">
                            <input type="text" class="form-control" v-model="player.uuid" placeholder="请填写数字">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-md-2 control-label">头像</label>
                        <div class="col-md-10">
                            <input type="text" class="form-control" v-model="player.avatarImg" placeholder="可以不填写">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="inputPassword3" class="col-sm-2 control-label">房间号</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" v-model="roomId"/> 
                        </div>
                    </div>

                    <div class="btncnt form-inline">
                        <button class="btn btn-success" v-on:click="connectServer">连接服务器</button>
                    </div>                    
                </div>

            </div>
        </div>
    </div>
</template>
<script>
    import $ from 'jquery'
    import s from './socketManager'

    export default {
        data(){
            return{
                selected:0,
                roomId:0,
                playerList:[
                    {
                        uuid:'10001',
                        roleName:'wwt',
                        avatarImg:'http://img1.360buyimg.com/cms/s244x244_jfs/t1024/361/1267066337/54518/1274eb9a/55973d01N0c07b1af.jpg',
                        gameId:1
                    }
                ],
                player:{
                    roleName:'',
                    avatarImg:'',
                    uuid:'',
                    gameId:1
                }
            }
        },
        methods:{
            connectServer(){
                // var player=this.playerList[this.selected];
                var player = this.player;
                var self = this;
                $.getJSON("http://10.240.120.77:8090/master/init?callback=?&uuid="+player.uuid+"&roleName="+player.roleName+"&avatarImg="+encodeURIComponent(player.avatarImg)+"&gameId="+player.gameId+"&roomId="+this.roomId, function(data){
                    s.roomId = self.roomId;
                    s.player = player;
                    s.avatarId=data.avatarId;
                    s.connectSocket(data);
                })
            }
        }
    }
</script>
<style scoped>
    .panel{width: 600px;margin: 0 auto;margin-top: 300px;}
    .btncnt{text-align: center;margin-top: 10px;}
    .form-group{margin-bottom: 20px;}
</style>