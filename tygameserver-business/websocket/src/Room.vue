<template>
    <div>
        <h1>天谕你画我猜({{sData.roomId}})</h1>
        <div class="container">
            <div class="cnt row">

                <div class="item col-md-3" v-for="(item,key) in sData.members">
                    <img class="img-rounded" src="./assets/header.jpg">
                    <button class="btn btn-default readyBtn">{{item.state==1?'已准备':'准备'}}</button>
                    <div class="txtCnt">
                        <label>昵称：{{item.name}}</label>
                        <!--<label>总积分：xxxx</label>-->
                    </div>
                </div>

            </div>
            <div class="btnCnt">
                <button class="btn btn-default btn-lg" v-if="sData.avatarId==sData.ownerName">游戏开启</button>
                <button class="btn btn-default btn-lg" v-if="sData.avatarId!=sData.ownerName&&sData.state==0" v-on:click="ready">准备</button>
                <button class="btn btn-default btn-lg" v-if="sData.avatarId!=sData.ownerName&&sData.state==1">已准备</button>
            </div>

            <div class="chatCnt row">
                <div class="col-md-8">
                    <div class="msgCnt">
                        <p v-for="item in sData.groupmsgs" class="line"><span>{{item.from}}：</span><span class="label label-info">{{item.msg}}</span></p>
                    </div>
                    <div class="row">
                        <div class="col-md-10">
                            <input type="text" class="form-control" placeholder="请输入文字" v-model="chatMsg">
                        </div> 
                        <div class="col-md-2">
                            <button class="btn btn-default" v-on:click="sendMsg">发送</button> 
                        </div>
                                                                      
                    </div>
                </div>        
                <div class="col-md-4">
                    <div class="playerList">
                        
                    </div>
                </div>        
            </div>

        </div>     

    </div>
</template>
<script>
    import s from './socketManager'

    export default{
        data(){
            return {
                sData:s,
                chatMsg:''
            }
        },
        created(){
            if(!s.socket){
                // s.router.push("/");
            }
        },
        methods:{
            ready(){
                s.ready();
            },
            sendMsg(){
                s.sendMsg(this.chatMsg);
                this.chatMsg='';
            }
        }
    }
</script>
<style scoped>
    .cnt{padding-top: 50px;}
    h1{text-align: center;font-weight: bold;font-size: 50px;}
    .item{text-align: center;margin-bottom: 40px;position: relative;}
    .readyBtn{position: absolute;left: 50%;bottom: 40px;margin-left: -26px;}
    .img-rounded{min-width: 180px;height: 180px;}
    .txtCnt{margin-top: 10px;}
    .btnCnt{text-align: center;}
    .chatCnt{margin-top: 40px;}
    .msgCnt{background: #eee;height: 400px;border: none;border-radius: 10px;margin-bottom: 20px;}
    .playerList{height: 400px;background: #eee;border-radius: 10px;}
    .line{padding: 10px;margin: 0;}
</style>
