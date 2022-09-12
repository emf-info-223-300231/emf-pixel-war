<?php

/** @var \Laravel\Lumen\Routing\Router $router */

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It is a breeze. Simply tell Lumen the URIs it should respond to
| and give it the Closure to call when that URI is requested.
|
*/

use Illuminate\Support\Facades\DB;

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
/*
 * Permet de définir le chemin d'accès de la méthode. Cette une route qui utilise GET et à un paramètre num.
 * num - est le numéro de la colonne dont nous voulons connaitre les couleurs
 */
$router->get('pixel/row/{num}', function ($num){

    return response()->json(
        DB::table("t_pixel")->where("column_pixel",$num)->get("color_pixel")

    );
});
$router->get('pixel/test', function (){

    return response()->json(
        DB::table("t_pixel")->get(["row_pixel","column_pixel","color_pixel"])->groupBy("row_pixel")
    );
});
