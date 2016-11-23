<template>
    <div>
        <div class="cnt">
            <canvas id="board" width="900" height="600"></canvas>
        </div>
        <div class="toolsCnt">
            <div class="form-inline hint" v-if="sData.painterId!=sData.avatarId">
                <label>提示一：</label>
                <label class="label label-success">{{sData.hint.hint1}}</label>
                <label>提示二:</label>
                <label class="label label-success">{{sData.hint.hint2}}</label>
            </div>
            <div class="form-inline hint" v-if="sData.painterId==sData.avatarId">

                <label>选择题目：</label>
                <select v-model="questionSelected">
                    <option v-for="(item,key) in sData.questions" v-bind:value="key">{{item.answer}}</option>
                </select>
                <button class="btn btn-default" v-on:click="sendQuestion">提交</button>

            </div>
            <div class="panel panel-default" v-if="sData.painterId==sData.avatarId">
                <div class="panel-heading">
                    <h3 class="panel-title">工具栏</h3>
                </div>
                <div class="panel-body">
                    <div class="colorCnt">
                        <span class="txt">颜色：</span>
                        <div class="color">
                            <span :class="'color-'+n+' '+(n==selectedColor?'active':'')"  v-on:click="selectColor(n)" v-for="n in 6"></span>
                        </div>
                    </div>
                    <div class="brushCnt">
                        <span class="txt">笔刷：</span>
                        <div class="brush">
                            <span :class="'brush-'+n+' '+(n==selectedBrush?'active':'')" v-on:click="selectBrush(n)" v-for="n in 5"></span>
                        </div>
                    </div>  
                    <div class="typeCnt">
                        <a class="btn btn-default" :class="type==1?'active':''" role="button" v-on:click="changeType(1)">笔刷</a>
                        <a class="btn btn-default" :class="type==2?'active':''" role="button" v-on:click="changeType(2)">橡皮</a>
                        <a class="btn btn-default" role="button" v-on:click="clearStage">清空画板</a>
                    </div>              
                </div>
            </div>

        </div>
        
        <div class="answerCnt">
            <p v-for="item in sData.answerList" class="answer">
               <span class="label label-success">{{item.name}}</span>： {{item.answer}}<span class="glyphicon glyphicon-ok" v-if="item.correct"></span>
            </p>
        </div>
        <div class="sendAnswerCnt" v-if="sData.painterId!=sData.avatarId">
            <div class="row">
                <div class="col-md-8">
                    <input type="text" class="form-control" placeholder="输入名称" v-model="answer">
                </div>                
                <button class="btn btn-default" v-on:click="sendAnswer">回答</button>
            </div> 
        </div>
        <div class="members">
            <div class="member" v-for="item in sData.members">
                <img width="40" heigth="40" src="./assets/header.jpg" class="headImg">
                <span class="label label-success">{{item.name}}</span>
                <span class="label label-danger" v-if="item.avatarId==sData.ownerName">房主</span>
            </div>
        </div>

    </div>    
</template>
<script>
    import $ from 'jquery'
    import s from './socketManager'

    export default {
        data(){
            return {
                oldPt:null,
                oldMidPt:null,
                isDrawing:false,
                stage:null,
                drawingCanvas:null,
                type:1,
                selectedColor:1,
                selectedBrush:1,
                colors:['','#000000','#b91428','#1361c1','#057e1f','#c36909','#f3de2f'],
                brushes:[,2,5,10,20,30],
                sData:s,
                answer:'',
                question:{
                    word:'',
                    hint1:'',
                    hint2:''
                },
                questionSelected:0
            }
        },
        created(){
            if(!s.socket){
                s.router.push("/");
            }
        },
        mounted(){            
            this.stage = new createjs.Stage("board");
            createjs.Ticker.setFPS(24);
            // createjs.Ticker.addEventListener("tick", this.stage);
            this.drawingCanvas=new createjs.Shape();	
            this.stage.addChild(this.drawingCanvas);
            if(s.painterId == s.avatarId){
                this.stage.addEventListener("stagemousedown", this.handleMouseDown);
                this.stage.addEventListener("stagemouseup", this.handleMouseUp);
            }            
            // this.stage.autoClear=false;
            this.drawingCanvas.cache(0,0,900,600);
            s.border = this;
        },
        methods:{
            replay(){
                this.stage.removeEventListener("stagemousedown", this.handleMouseDown);
                this.stage.removeEventListener("stagemouseup", this.handleMouseUp);
                if(s.painterId == s.avatarId){
                    this.stage.addEventListener("stagemousedown", this.handleMouseDown);
                    this.stage.addEventListener("stagemouseup", this.handleMouseUp);
                }
            },
            handleMouseDown(){
                this.oldPt = new createjs.Point(this.stage.mouseX, this.stage.mouseY);
                this.oldMidPt = this.oldPt;
                this.isDrawing = true; 
                this.stage.addEventListener("stagemousemove", this.handleMouseMove);
            },
            handleMouseUp(){
                this.isDrawing = false; 
                this.stage.removeEventListener("stagemousemove", this.handleMouseMove);
            },
            handleMouseMove(){
                if (!this.isDrawing) {
                    return;
                }
                var midPoint = new createjs.Point(this.oldPt.x + this.stage.mouseX >> 1, this.oldPt.y + this.stage.mouseY >> 1);
                var color = this.colors[this.selectedColor];
                var brush = this.brushes[this.selectedBrush];
                this.drawingCanvas.graphics.setStrokeStyle(brush, "round", "round")
                        .beginStroke(color)
                        .moveTo(midPoint.x, midPoint.y)
                        .curveTo(this.oldPt.x, this.oldPt.y, this.oldMidPt.x, this.oldMidPt.y);	
                this.drawingCanvas.updateCache(this.type==2 ? "destination-out" : "source-over");
                this.drawingCanvas.graphics.clear();               

                var drawInfo={
                    brush:brush,
                    color:color,
                    mtx:midPoint.x,
                    mty:midPoint.y,
                    ctOldx:this.oldPt.x,
                    ctOldy:this.oldPt.y,
                    ctOldMidx:this.oldMidPt.x,
                    ctOldMidy:this.oldMidPt.y,
                    type:this.type
                }

                this.oldPt.x = this.stage.mouseX;
                this.oldPt.y = this.stage.mouseY;
                this.oldMidPt.x = midPoint.x;
                this.oldMidPt.y = midPoint.y;
                this.stage.update();

                s.sendDrawingInfo({type:1,drawInfo:drawInfo});

            },
            selectColor(index){
                this.selectedColor = index;
            },
            selectBrush(index){
                this.selectedBrush = index;
            },
            changeType(index){
                this.type = index;
            },
            clearStage(){
                this.stage.clear();
                this.drawingCanvas.cache(0,0,900,600);
                if(s.painterId == s.avatarId){
                    s.sendDrawingInfo({type:2});
                }
            },
            clear(){
                if(s.painterId != s.avatarId){
                    this.stage.clear();
                    this.drawingCanvas.cache(0,0,900,600);
                }
            },
            drawing(info){
                if(s.painterId == s.avatarId) return;
                this.drawingCanvas.graphics.setStrokeStyle(info.brush, "round", "round")
                        .beginStroke(info.color)
                        .moveTo(info.mtx, info.mty)
                        .curveTo(info.ctOldx, info.ctOldy, info.ctOldMidx, info.ctOldMidy);	
                this.drawingCanvas.updateCache(info.type==2 ? "destination-out" : "source-over");
                this.drawingCanvas.graphics.clear();
                this.stage.update();
            },
            sendQuestion(){
                var question = this.sData.questions[this.questionSelected];
                s.sendQuestion(question);
            },
            sendAnswer(){
                s.sendAnswer(this.answer);
            }
        }
    }    
</script>
<style scoped>
    #board{width: 900px;height: 600px;}
    .cnt{border: 1px solid #000;width: 900px;height: 600px;border-radius: 10px;position: absolute;margin-left: -450px;left: 50%;top:50px;}
    .toolsCnt{position: absolute;width: 900px;margin-left: -450px;left: 50%;top:670px;}
    .color span{display: inline-block;width: 40px;height: 40px;border-radius: 40px;margin-right: 4px;cursor: pointer;}
    .color span.active{border: 4px solid #fff;}
    .color span.color-1{background: #000;}
    .color span.color-2{background: #b91428;}
    .color span.color-3{background: #1361c1;}
    .color span.color-4{background: #057e1f;}
    .color span.color-5{background: #c36909;}
    .color span.color-6{background: #f3de2f;}
    .colorCnt{width: 400px;float: left;}
    .colorCnt .txt{float: left;margin-right: 10px;padding-top: 10px;font-weight: bold;}
    .color{float: left;}
    .brushCnt{width: 250px;float: left;}
    .brushCnt .txt{float: left;margin-right: 10px;padding-top: 10px;font-weight: bold;}
    .brush{float: left;line-height: 40px;}
    .brush span{display: inline-block;background: #000;vertical-align: middle;margin-right: 10px;cursor: pointer;}
    .brush span.active{border: 4px solid #fff;}
    .brush span.brush-1{width: 10px;height: 10px;border-radius: 10px;}
    .brush span.brush-2{width: 15px;height: 15px;border-radius: 15px;}
    .brush span.brush-3{width: 20px;height: 20px;border-radius: 20px;}
    .brush span.brush-4{width: 25px;height: 25px;border-radius: 25px;}
    .brush span.brush-5{width: 30px;height: 30px;border-radius: 30px;}
    .typeCnt{float: left;padding-top: 4px;}
    .questionCnt{width: 200px;height: 50px;background: #eee;position: absolute;padding: 8px;position: absolute;margin-left: 460px;left: 50%;top: 80px;}
    .answerCnt{width: 200px;height: 560px;background: #eee;position: absolute;margin-left: 460px;left: 50%;top:50px;border-radius: 10px;overflow-y: auto;}
    .sendAnswerCnt{width: 200px;position: absolute;top: 618px;left: 50%;margin-left: 460px;}
    .hint{margin-bottom: 20px;}
    .members{width: 200px;height: 600px;background: #eee;position: absolute;top: 50px;margin-left: -660px;left: 50%;border-radius: 10px;}
    .member{padding: 10px;}
    .answer{padding: 10px;}
    .glyphicon-ok{color:#057e1f;}
    .cnt{background: #fff;}
    select{color: #000;}
    p{color: #333;}
</style>