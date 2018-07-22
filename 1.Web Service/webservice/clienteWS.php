<?php
    $soap_cliente = new SoapClient("http://127.0.0.1:9876/ticketnow?wsdl");
    $param = array('num_fileira' => '1', 'num_corredor' => 'A');
    $result = $soap_cliente->consultar($param);
    if($result == 1)
        echo "Consultando preco";
 ?>

