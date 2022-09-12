import React, {useEffect, useState} from 'react';
import {Table} from "react-bootstrap";
import {PixelRow} from "./Pixels/PixelRow";
import {getAllColors} from "./Api/Call";

type colors = {
    row:number,
    color:string
}

function App() {
    const [row,setRow] = useState<React.ReactElement[]>();
    const [colors,setColors] = useState<any>();
    useEffect(()=>{
        setInterval(()=>{
            getAllColors((data:any)=>setColors(data));
        },500);
    },[]);

    useEffect(()=>{
        let tmp:React.ReactElement[] = [];
        if(colors !== undefined) {
            for (let i = 0; i < 64; i++) {
                tmp[i] = <PixelRow key={"col"+i} row={i} size={colors[i].length} coloring={colors[i]}/>
            }
        }
        setRow(tmp);
    },[colors]);

    return (
        <div className="center">
            <Table>
                <tbody>
                    {row?.map((x)=>x)}
                </tbody>
            </Table>
        </div>
    );
}

export default App;
