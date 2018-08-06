<?php

    include_once "FilmeSubject.php";
    include_once "InscritoObserver.php";

    $nome = $_POST["txt_nome"];
    $email = $_POST["txt_email"];

    $oFilmeSubject = new FilmeSubject();
    $oInscritoObserver = new InscritoObserver($nome, $email);

    $oFilmeSubject->attach($oInscritoObserver);

    echo "Obrigado por se inscrever no Feed";

?>