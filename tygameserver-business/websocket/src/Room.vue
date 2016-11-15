<template>
    <div>
        <h1>天谕你画我猜({{sData.roomId}})</h1>
        <div class="container">
            <div class="cnt row">

                <div class="item col-md-3">
                    <img class="img-rounded" src="./assets/header.jpg">
                    <button class="btn btn-default readyBtn">准备</button>
                    <div class="txtCnt">
                        <label>昵称：xxx</label>
                        <label>总积分：xxxx</label>
                    </div>
                </div>
                <div class="item col-md-3">
                    <img class="img-rounded" src="./assets/header.jpg">
                    <button class="btn btn-default readyBtn">准备</button>
                    <div class="txtCnt">
                        <label>昵称：xxx</label>
                        <label>总积分：xxxx</label>
                    </div>
                </div>
                <div class="item col-md-3">
                    <img class="img-rounded" src="./assets/header.jpg">
                    <button class="btn btn-default readyBtn">准备</button>
                    <div class="txtCnt">
                        <label>昵称：xxx</label>
                        <label>总积分：xxxx</label>
                    </div>
                </div>
                <div class="item col-md-3">
                    <img class="img-rounded" src="./assets/header.jpg">
                    <button class="btn btn-default readyBtn">准备</button>
                    <div class="txtCnt">
                        <label>昵称：xxx</label>
                        <label>总积分：xxxx</label>
                    </div>
                </div>

            </div>
            <div class="btnCnt">
                <button class="btn btn-default btn-lg">游戏开启</button>
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
