<?php
    namespace TicketNow;
    include_once "../Database/logicas/LogicaFactory.php";

    $titulo = $_POST["txt_titulo"];
    $descricao = $_POST["txt_descricao"];
    $genero = $_POST["txt_genero"];
    $diretor = $_POST["txt_diretor"];
    $ano = $_POST["txt_ano"];
    $avaliacao = $_POST["txt_avaliacao"];
    $elenco = $_POST["txt_elenco"];

    $oLogicaFactory = LogicaFactory::getInstance();
    $oFilme = $oLogicaFactory->criarLogica("filme");

    $oFilme->insert($titulo, $descricao, $genero, $diretor, $ano, $avaliacao, $elenco);

    echo "Filme adicionado com sucesso<br>";
?>