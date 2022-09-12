import {useEffect} from "react";

interface data{
    color:string,
    coords:coords
}
type coords = {
    row:number,
    col:number
}
export const PixelCell = (props:data) =>{
    useEffect(()=>{

    },[]);

    return (
      <td style={{
          backgroundColor:"#"+props.color,
          height:"10px",
          width:"10px"
      }}>
      </td>
    );
}