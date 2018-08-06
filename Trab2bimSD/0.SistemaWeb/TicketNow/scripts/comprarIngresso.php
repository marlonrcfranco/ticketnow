<?php
    include_once "ClienteWS.php";

    $oClienteWS = new ClienteWS();

    $assento = explode(",",$_POST["txt_assento"]);
    $numeroAssento = $assento[0];
    $letraFileira = $assento[1];

    $codCartao = $_POST["txt_numero_cartao"];
    $dataVencimento = $_POST["txt_validade_cartao"];
    $digitoVerificador = $_POST["txt_digito_cartao"];


    echo "<br><br><h3>".$oClienteWS->comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador)."</h3>";
?>