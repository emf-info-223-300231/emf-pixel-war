import axios from 'axios';

//const BASE_URL = "https://pixelwarapi.ilievv01.emf-informatique.ch/public/";
const BASE_URL = "https://jpo.viliev.ch/public/";
export const getColors  = (row:number,callback:any) =>{
    axios.get(BASE_URL+"pixel/row/"+row)
        .then(value => {
            callback(value.data);
        })
        .catch(reason => {
            callback(null);
        })
}

type info = {
    column_pixel?:number,
    color_pixel?:string
}
export function getAllColors (callback:any) : any{
    axios.get(BASE_URL+"pixel/test")
        .then(value => {
            callback(value.data);
        })
        .catch(reason => {
            return null;
        })

}