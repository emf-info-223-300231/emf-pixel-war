import React, {useEffect, useState} from "react";
import {getColors} from "../Api/Call";
import {PixelCell} from "./PixelCell";

interface data{
    row:number,
    size:number,
    coloring:colors[]
}
interface colors {
    row_pixel:number,
    column_pixel:number,
    color_pixel:string
}

export const PixelRow = (props:data)=>{
    const [pixel,setPixel] = useState<React.ReactElement[]>();
    const [colors,setColors] = useState<colors[]>();
    useEffect(()=>{
        setColors(props.coloring);
    },[props.coloring]);

    useEffect(()=>{
        makePixel(colors);
    },[colors]);


    const makePixel = (color:any) => {
        let temp:React.ReactElement[] = [];
        for (let i = 0; i < props.size; i++) {
            let tmp:string = color !== undefined ? color[i]?.color_pixel : "FFF";
            temp[i] = <PixelCell key={((i+1)*(props.row+1))} color={tmp} coords={{col:i,row:props.row}}/>;
        }
        setPixel(temp);
    }

    return (
      <tr>
          {
              pixel?.map((x)=>x)
          }
      </tr>
    );
}