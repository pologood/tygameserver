<template>
    <div>
        <div class="cnt">
            <canvas id="board" width="900" height="600"></canvas>
        </div>
        <div class="panel panel-default">
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
</template>
<script>
    import $ from 'jquery'

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
                brushes:[,2,5,10,20,30]
            }
        },
        created(){
            
        },
        mounted(){            
            this.stage = new createjs.Stage("board");
            createjs.Ticker.setFPS(24);
            // createjs.Ticker.addEventListener("tick", this.stage);
            this.drawingCanvas=new createjs.Shape();	
            this.stage.addChild(this.drawingCanvas);
            this.stage.addEventListener("stagemousedown", this.handleMouseDown);
            this.stage.addEventListener("stagemouseup", this.handleMouseUp);
            // this.stage.autoClear=false;
            this.drawingCanvas.cache(0,0,900,600);
        },
        methods:{
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
                this.oldPt.x = this.stage.mouseX;
                this.oldPt.y = this.stage.mouseY;
                this.oldMidPt.x = midPoint.x;
                this.oldMidPt.y = midPoint.y;
                this.stage.update();
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
            }
        }
    }    
</script>
<style scoped>
    #board{width: 900px;height: 600px;}
    .cnt{border: 1px solid #000;width: 900px;height: 600px;border-radius: 10px;position: absolute;margin-left: -450px;margin-top: -400px;left: 50%;top: 50%;}
    .panel{position: absolute;width: 900px;margin-left: -450px;left: 50%;margin-top: 220px;top: 50%;}
    .color span{display: inline-block;width: 40px;height: 40px;border-radius: 40px;margin-right: 4px;cursor: pointer;}
    .color span.active{border: 4px solid #fa7243;}
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
    .brush span.active{border: 4px solid #fa7243;}
    .brush span.brush-1{width: 10px;height: 10px;border-radius: 10px;}
    .brush span.brush-2{width: 15px;height: 15px;border-radius: 15px;}
    .brush span.brush-3{width: 20px;height: 20px;border-radius: 20px;}
    .brush span.brush-4{width: 25px;height: 25px;border-radius: 25px;}
    .brush span.brush-5{width: 30px;height: 30px;border-radius: 30px;}
    .typeCnt{float: left;padding-top: 4px;}
</style>