<template>
    <div>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">连接服务器</h3>
            </div>
            <div class="panel-body">

                <div class="form-horizontal">

                     <div class="form-group">
                        <label for="inputPassword3" class="col-sm-2 control-label">玩家</label>
                        <div class="col-sm-10">
                            <select class="form-control" v-model="selected">
                                <option v-for="(item,key) in playerList" v-bind:value="key">{{item.roleName}}</option>
                            </select> 
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
    console.log(s.name)
    s.name="aaa"
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
                ]
            }
        },
        methods:{
            connectServer(){
                var player=this.playerList[this.selected];
                $.getJSON("http://littlegame.tianyu.163.com/master/init?callback=?&uuid="+player.uuid+"&roleName="+player.roleName+"&avatarImg="+encodeURIComponent(player.avatarImg)+"&gameId="+player.gameId+"&roomId="+this.roomId, function(data){
                    console.log(data);
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