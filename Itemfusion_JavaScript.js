"use strict";

var itemArray=[];
var fusionArray=[];
var itemToLookFor;
var cheapFast;
var boodschappenlijstItem=[];
var boodschappenlijstPrijs=[];

const fs = require('fs');

var readline, rl;
readline = require('readline');
rl = readline.createInterface(process.stdin, process.stdout);

fs.readFile('item-fusion-data.txt', 'utf8', loadData);

function loadData (err, data) {
    if (err) {
        throw err;
    }

    var linesArray = data.split('\r\n');

    saveData(linesArray);
}

function saveData(linesArray){
    var store;
    for (let i=0;i<linesArray.length;i++){
        var line=linesArray[i];
        if (line.includes("Beginning Store Items:")){
            store=1;
        } else if (line.includes("Second Store Items:")){
            store=2;
        } else if (line.includes("Third Store Items:")){
            store=3;
        } else if (line.includes("4th Store Items:")){
            store=4;
        } else if (line.includes("5th Store Items:")) {
            store = 5;
        }

        line=line.replace(" (RU)","");

        if (line.includes("~") && !line.includes("Cost") && (line.includes("*") || store===1)){
            line=line.replace("*","");
            var emptyFusionArray=[];
            var item=line.split(" ~ ");
            itemArray.push(new Item(item[0],item[1],store, emptyFusionArray));
        } else if (line.includes("+")){
            var fusion=line.split(/[ +]+[ =]+/);
            fusionArray.push(new Fusion(fusion[0], fusion[1], fusion[2]));
            var item1Present=false;
            var item2Present=false;
            var item3Present=false;
            for (let j=0;j<itemArray.length;j++){
                if (itemArray[j].item===fusion[0]){
                    item1Present=true;
                }
                if(itemArray[j].item===fusion[1]){
                    item2Present=true;
                }
                if (itemArray[j].item===fusion[2]){
                    item3Present=true;
                }
            }

            if (!item1Present){
                itemArray.push(new Item(fusion[0],9999,9999));
            } else if (!item2Present){
                itemArray.push(new Item(fusion[1],9999,9999));
            } else if (!item3Present){
                itemArray.push(new Item(fusion[2],9999,9999));
            }
        }
    }
    addFusionsToItem();
}

function addFusionsToItem(){
    for (let i=0;i<itemArray.length;i++){                               // voor ieder item
        //console.log(itemArray[i].item);
        var smallFusionArray=[];
        for (var j=0;j<fusionArray.length;j++){                         // zoek fusions die resulteren in het gewenste item
            if (itemArray[i].item===fusionArray[j].item){
                var item1fusion;
                var item2fusion;
                for (let k=0;k<itemArray.length;k++){                   // zoek naar items die de fusion vormen
                    if (itemArray[k].item===fusionArray[j].item1){
                        item1fusion=itemArray[k];
                    }
                    if (itemArray[k].item===fusionArray[j].item2){
                        item2fusion=itemArray[k];
                    }
                }
                smallFusionArray.push(new Fusion(item1fusion, item2fusion, itemArray[i]));
            }
        }
        itemArray[i].fusionArray=(smallFusionArray);
    }
    askItem();
}

function askItem(){
    rl.question("Wel item wil je? ", askCheapest);
}

function askCheapest(item){
    itemToLookFor=item;
    rl.question("Goedkoopste (ja) of Snelste (nee)? ", determineItem);
}

function determineItem (goedkoopSnel){
    cheapFast=goedkoopSnel;
    var startNode;
    let alreadyUsedArray=[];
    var levels=4;

    for (var i=0;i<itemArray.length;i++){
        if (itemArray[i].item===itemToLookFor){
            startNode=itemArray[i];
        }
    }

    let finalNode = findPath(startNode, alreadyUsedArray, levels, cheapFast);

    printUitkomst(finalNode, 0);
    console.log("");
    printBoom(finalNode, 0);
    console.log("");
    maakBoodschappenlijst(finalNode);
    console.log("");
    printBoodschappenlijst();
}

function findPath (itemToFind, alreadyUsedArray1, level, cheapest){
    var node=itemToFind;
    let alreadyUsedArray=alreadyUsedArray1;
    let usedNew=addToArray(alreadyUsedArray, node.item);
    let maxStore;
    let addedPrice;
    let treeNode;

    var price=node.price;
    var store=node.store;

    if (node.fusionArray.length>0 && level>0) {
        for (let i = 0; i < node.fusionArray.length; i++) {
            if (usedNew.indexOf(node.fusionArray[i].item1.item) === -1 && usedNew.indexOf(node.fusionArray[i].item2.item) === -1) {                       // als het item nog niet in een eerdere ronde aan de beurt is geweest

                let node1 = findPath(node.fusionArray[i].item1, usedNew, level-1);
                let node2 = findPath(node.fusionArray[i].item2, usedNew, level-1);

                addedPrice=parseInt(node1.price)+parseInt(node2.price);
                maxStore=Math.max(node1.store, node2.store);
                if ((cheapest && addedPrice<price) || (!cheapest && maxStore<store && addedPrice<price)){
                    store=maxStore;
                    price=addedPrice;
                    treeNode=new TreeNode(node.item, addedPrice, store, node1, node2);
                }
            }
        }
    }
    if (treeNode===undefined) {
        treeNode=new TreeNode(node.item, price, store);
    }
    return treeNode;
}

function printUitkomst(finalNode, counter){
    for (let i=0;i<counter;i++){
        process.stdout.write("  ");
    }
    console.log(finalNode.item+" ("+ finalNode.price+")"+" ("+ finalNode.store+")");
    if (finalNode.item1!==undefined){
        counter++;
        printUitkomst(finalNode.item1, counter);
        printUitkomst(finalNode.item2, counter);
    }
}

function printBoom(node,counter){
    let printSpaces="";
    let lengthDifference=10-node.item.length;

    for (let i=0;i<lengthDifference;i++){
        printSpaces=printSpaces+" ";
    }

    process.stdout.write(node.item+printSpaces);

    if (node.item1!== undefined){
        counter++;
        process.stdout.write(" <-+-- ");
        printBoom(node.item1,counter);
        console.log("");
        for (let l=0;l<counter;l++){
            process.stdout.write("             |   ");
        }


        console.log("");
        for (let j=0;j<counter;j++){
            process.stdout.write("          ");
            if (j<counter-1){
                process.stdout.write("   |   ");
            }
        }

        process.stdout.write("   +-- ");
        printBoom(node.item2,counter);
    }
}

function maakBoodschappenlijst(node){

    if (node.item1!==undefined){
        maakBoodschappenlijst(node.item1);
        maakBoodschappenlijst(node.item2);
    } else {
        boodschappenlijstItem.push(node.item);
        boodschappenlijstPrijs.push(node.price);
    }
}

function printBoodschappenlijst(){
    var lijstItem2=[];
    var lijstPrijs1=[];
    var lijstPrijs2=[];
    var lijstTotalePrijs=[];

    for (let i=0;i<boodschappenlijstItem.length;i++){
        if (!lijstItem2.includes(boodschappenlijstItem[i])){
            lijstItem2.push(boodschappenlijstItem[i]);
            lijstPrijs1.push(boodschappenlijstPrijs[i]);
            lijstPrijs2.push(1);
        } else {
            let variable=lijstItem2.indexOf(boodschappenlijstItem[i]);
            lijstPrijs2[variable]++;
        }
    }

    for (let i=0;i<lijstItem2.length;i++){
        var printSpaces="";
        let lengthDifference=10-lijstItem2[i].length;
        for (let j=0;j<lengthDifference;j++){
            printSpaces=printSpaces+" ";
        }

        var sum=lijstPrijs1[i]*lijstPrijs2[i];
        lijstTotalePrijs.push(sum);

        console.log(lijstPrijs2[i]+"x "+lijstItem2[i]+printSpaces+lijstPrijs2[i]+"x "+lijstPrijs1[i]+" = "+sum);
    }
    console.log("---------------------------")

    var totaleKosten=0;
    for (let i=0;i<lijstTotalePrijs.length;i++){
        totaleKosten+=lijstTotalePrijs[i];
    }

    console.log("Totale prijs:         "+totaleKosten);
}


function TreeNode (item, price, store, item1, item2){
    this.item=item;
    this.price=price;
    this.store=store;
    this.item1=item1;
    this.item2=item2;

}

function addToArray (array, item){
    let temporaryArr=[];
    for (let i=0;i<array.length;i++){
        temporaryArr.push(array[i]);
    }
    temporaryArr.push(item);
    return temporaryArr;
}

function Item (item, price, store, fusionArray){
    this.item = item;
    this.price = price;
    this.store=store;
    this.fusionArray=fusionArray;
}

function Fusion (item1, item2, resultItem) {           // met objecten als item1 en item1
    this.item = resultItem;
    this.item1 = item1;
    this.item2 = item2;
}
