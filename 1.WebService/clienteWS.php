<?php
    

    // identificar se o cliente quer comprar ou consultar

    /*
    $numeroAssento;
    $letraFileira;

    se ( comprar ) {
        $codCartao;
        $dataVencimento;
        $digitoVerificador;
    }
    */

    $numeroAssento = 10;
    $letraFileira = "A";
    $codCartao = "7000000000000000";
    $dataVencimento = "2720";
    $digitoVerificador = "130";

    comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador);




    function comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador) {
        echo "Comprando assento";

        $clienteWS = new SoapClient("http://127.0.0.1:9876/ticketnow?wsdl");

        $param = array();
        $param["numeroAssento"] = $numeroAssento;
        $param["letraFileira"] = $letraFileira;
        $param["codCartao"] = $codCartao;
        $param["dataVencimento"] = $dataVencimento;
        $param["digitoVerificador"] = $digitoVerificador;

        //$clienteWS->comprarIngresso($param);
        //echo $clienteWS->hello("Vinicius");
        $aux = ticketnow.comprar(1, 'A');
        echo $aux;
    }

    function consultarAssento($numeroAssento, $letraFileira) {
        $param = array();
        $param["numeroAssento"] = $numeroAssento;
        $param["letraFileira"] = $letraFileira;

        $resultadoConsulta = $clienteWS->consultarAssento($param);

        return $resultadoConsulta;
    }
    
 ?>

