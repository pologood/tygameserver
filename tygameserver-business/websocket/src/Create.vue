<template>
    <div>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">连接服务器</h3>
            </div>
            <div class="panel-body">

                <div class="t">
                    <p v-if="sData.roomId==0">是否要创建房间</p>
                    <p v-if="sData.roomId!=0">是否要加入房间{{sData.roomId}}</p>
                </div>

                <div class="btncnt form-inline">
                    <button class="btn btn-success" v-on:click="confirmHandle">确认</button>
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
            sData:s
        }
    },
    created(){
        if(!s.socket){
            s.router.push("/");
        }
    },
    methods:{
        confirmHandle(){
            if(this.sData.roomId==0){
                s.createRoom();
            }else{
                s.joinRoom();
            }
        }
    }
}
</script>

<style scoped>
    .panel{width: 600px;margin: 0 auto;margin-top: 300px;}
    .btncnt{text-align: center;margin-top: 10px;}
    .form-group{margin-bottom: 20px;}
    .t{text-align: center;font-weight: bold;}
</style>