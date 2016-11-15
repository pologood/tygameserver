<template>
    <div>
        <h1>天谕你画我猜({{sData.roomId}})</h1>
        <div class="container">
            <div class="cnt row">

                <div class="item col-md-3" v-for="(item,key) in sData.members">
                    <img class="img-rounded" src="./assets/header.jpg">
                    <button class="btn btn-default readyBtn" v-on:click="ready">准备</button>
                    <div class="txtCnt">
                        <label>昵称：{{item.name}}</label>
                        <label>总积分：xxxx</label>
                    </div>
                </div>

            </div>
            <div class="btnCnt">
                <button class="btn btn-default btn-lg" v-if="sData.player.roleName==sData.owner">游戏开启</button>
                <button class="btn btn-default btn-lg" v-if="sData.player.roleName!=sData.owner&&sData.state==0" v-on:click="ready">准备</button>
                <button class="btn btn-default btn-lg" v-if="sData.player.roleName!=sData.owner&&sData.state==1">已准备</button>
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
        methods:{
            ready(){
                s.ready();
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
</style>
