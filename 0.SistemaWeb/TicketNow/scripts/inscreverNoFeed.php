<?php
    namespace TicketNow;

    require_once "FilmeSubject.php";
    require_once "InscritoObserver.php";
    /*
    $nome = $_POST["txt_nome"];
    $email = $_POST["txt_email"];
    */
    $nome = "Cicera Tavares";
    $email = "cicera.tavares@gmail.com";

    $oFilmeSubject = new FilmeSubject("Jobs");
    $oInscritoObserver = new InscritoObserver($nome, $email);

    $oFilmeSubject->attach($oInscritoObserver);

    echo "Obrigado por se inscrever no Feed";

?>