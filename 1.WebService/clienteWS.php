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
    $letraFileira = "B";
    $codCartao = "7000000000000000";
    $dataVencimento = "2720";
    $digitoVerificador = "130";



    carregarConfiguracoesWebService();

    comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador);


    function carregarConfiguracoesWebService() {
        $GLOBALS["portaServidor"] = 56000;
        $GLOBALS["ipServidor"] = "localhost";
    }

    function comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador) {
        echo "Comprando assento";
        $clienteWS = new SoapClient("http://".$GLOBALS["ipServidor"].":".$GLOBALS["portaServidor"]."/ticketnowws?wsdl");

        $param = array();
        $param["numeroAssento"] = $numeroAssento;
        $param["letraFileira"] = $letraFileira;
        $param["codCartao"] = $codCartao;
        $param["dataVencimento"] = $dataVencimento;
        $param["digitoVerificador"] = $digitoVerificador;

        $aux = $clienteWS->comprarIngresso($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador);
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

