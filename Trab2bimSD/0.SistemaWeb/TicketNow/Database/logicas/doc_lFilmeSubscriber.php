<?php

/**
 * ..:: Teste e documentacao da utilizacao da classe lFilmeSubscriber ::..
 * 
 * @author Marlon R C Franco
 * @author Marlon R C Franco <marlonrcfranco@gmail.com>
 * 
 * * IMPORTANTE: Executar qualquer funcao de teste a seguir ira modificar a tabela tFilmeSubscriber.xml
 * 
 * Para escolher as funcoes a serem executadas ou nao, comente ou descomente a chamada das mesmas
 * na funcao 'main' ao final deste codigo.
 * 
 */


include_once 'lFilmeSubscriber.php';


/**
 * testeAtributos
 * 
 * ..:: Teste de modificacao dos atributos do objeto $oFilmeSubscriber da classe lFilmeSubscriber ::..
 * 
 * Comportamento esperado:
 *
 * * $oFilmeSubscriber possuir os atributos passados a ele;
 * 
 */
function testeAtributos(){
    $oFilmeSubscriber = new lFilmeSubscriber();

    print_r("Antes:\n");
    print_r($oFilmeSubscriber);

    $oFilmeSubscriber->titulo = "Teste Atributos, o Filme.";
    $oFilmeSubscriber->email = "teste.atributos.subscriber@email.com";
    
    print_r("\nDepois:\n");
    print_r($oFilmeSubscriber);
}


/**
 * testeCriacao
 * 
 * ..:: Teste do metodo de criacao da tabela tFilmeSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Criacao da tabela 'tFilmeSubscriber.xml' ou a sobrescrita se já houver uma tabela criada;
 * * Conteudo da tabela tFilmeSubscriber.xml deve ser apenas o primeiro registro: Template.
 *
 */
function testeCriacao() {
    $oFilmeSubscriber = new lFilmeSubscriber();
    print_r($oFilmeSubscriber->createTableFilmeSubscriber());
    print_r("\nVer se a tabela tFilmeSubscribers.xml foi criada.");
}


/**
 * testeInsercaoPorParametro
 * 
 * ..:: Teste do metodo de insercao de um novo registro na tabela tFilmeSubscriber.xml passando os valores como parametro ::..
 * 
 * Comportamento esperado:
 *
 * * Insercao de cinco registros com os respectivos valores informados nos parametros;
 * * Conteudo da tabela tFilmeSubscriber.xml deve ser seis registros, sendo o primeiro o Template.
 *
 */
function testeInsercaoPorParametro() {
    $oFilmeSubscriber = new lFilmeSubscriber();
    $oFilmeSubscriber->createTableFilmeSubscriber();

    print_r($oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.1@email.com"));
    print_r("\n");
    print_r($oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.1@email.com"));
    print_r("\n");
    print_r($oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.2@email.com"));
    print_r("\n");
    print_r($oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.1@email.com"));
    print_r("\n");
    print_r($oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.2@email.com"));
    print_r("\n");
}


/**
 * testeInsercaoPorAtributo
 * 
 * ..:: Teste do metodo de insercao de um novo registro na tabela tFilmeSubscriber.xml utilizando os atributos do objeto $oFilmeSubscriber ::..
 * 
 * Comportamento esperado:
 *
 * * Insercao de um registro com os respectivos valores informados nos atributos do objeto $oFilmeSubscriber;
 * * Conteudo da tabela tFilmeSubscriber.xml deve ser dois registros, sendo o primeiro o Template.
 *
 */
function testeInsercaoPorAtributo() {
    $oFilmeSubscriber = new lFilmeSubscriber();
    $oFilmeSubscriber->createTableFilmeSubscriber();

    $oFilmeSubscriber->titulo = "Template, o Filme";
    $oFilmeSubscriber->email = "teste.1@email.com";

    print_r($oFilmeSubscriber);
    print_r($oFilmeSubscriber->insertFilmeSubscriber());
    print_r($oFilmeSubscriber);
}


/**
 * testeUpdatePorParametro
 * 
 * ..:: Teste do metodo 'updateFilmeSubscriberCompleto' de atualização de um registro na tabela tFilmeSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Atualização do registro que possui o codigo informado;
 * * A atualização por parametro ('updateFilmeSubscriberCompleto') utiliza os valores passados como parametro para salvar na tabela tFilmeSubscriber.xml;
 * * Eh necessario que o codigo do FilmeSubscriber seja conhecido.
 * 
 *   
 */
function testeUpdatePorParametro(){
    $oFilmeSubscriber = new lFilmeSubscriber();
    $oFilmeSubscriber->createTableFilmeSubscriber();

    /* ..:: Atualizacao por parametro utilizando o metodo "updateFilmeSubscriberCompleto" ::.. */
    print_r($oFilmeSubscriber->updateFilmeSubscriberCompleto("X0000", null, "teste.2@email.com"));
}


/**
 * testeUpdatePorAtributo
 * 
 * ..:: Teste do metodo 'updateFilmeSubscriber' de atualização de um registro na tabela tFilmeSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Atualização do registro que possui o codigo informado;
 * * A atualização por atributo ('updateFilmeSubscriber') utiliza os nos atributos do objeto $oFilmeSubscriber;
 * * Eh necessario que o codigo do FilmeSubscriber seja conhecido.
 * 
 *   
 */
function testeUpdatePorAtributo(){
    $oFilmeSubscriber = new lFilmeSubscriber();
    $oFilmeSubscriber->createTableFilmeSubscriber();
    
    /* ..:: Atualizacao por atributo ::.. */
    $oFilmeSubscriber->codigo = "X0000"; 
    // como exemplo, eh utilizado o codigo do Template ("X0000"), porem este codigo 
    // deve ser adquirido atraves do metodo "getCodigoByFilmeSubscriber", ou por qualquer um dos metodos de Select (ver: testeSelect())
    $oFilmeSubscriber->email = "teste.2@email.com";
    print_r($oFilmeSubscriber->updateFilmeSubscriber());
}


/**
 * testeExclusao
 * 
 * ..:: Teste do metodo de exclusao de um registro na tabela tFilmeSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Exclusao de um registro com o codigo igual ao informado no parametro do metodo 'excluirFilmeSubscriber';
 * * Conteudo da tabela tFilmeSubscriber.xml deve ser zero registros, pois o primeiro e unico registro (Template) foi excluido.
 * 
 * 
 */
function testeExclusao(){
    $oFilmeSubscriber = new lFilmeSubscriber();
    $oFilmeSubscriber->createTableFilmeSubscriber();

    print_r($oFilmeSubscriber->excluirFilmeSubscriber("X0000"));
}


/**
 * testeSelectPorUmParametro
 * 
 * ..:: Teste dos metodos de selecao de um (ou mais) registro(s) na tabela tFilmeSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Selecao de um ou mais registros que satisfazem as condicoes nos parametros;
 * * A selecao utilizando os metodos iniciados por 'get' retornam um array do tipo lFilmeSubscriber.
 * * A selecao utilizando o metodo 'selectFilmeSubscriber' retorna um array do tipo SimpleXMLElement.
 * 
 *  
 */
function testeSelectPorUmParametro(){
    $oFilmeSubscriber = new lFilmeSubscriber();
    $oFilmeSubscriber->createTableFilmeSubscriber();
    print_r($oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.1@email.com"));
    print_r($oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.2@email.com"));
    
    /* ..:: Consulta utilizando os metodos 'get' ::.. */
    /* Retornam um array de lFilmeSubscriber */

    print_r($oFilmeSubscriber->getFilmeSubscriberByCodigo("X0001"));
    // print_r($oFilmeSubscriber->getFilmeSubscriberByTitulo("Template, o Filme"));
    // print_r($oFilmeSubscriber->getFilmeSubscriberByEmail("teste.1@email.com"));

    /* ..:: Consultas equivalentes às acima mas utilizando o metodo 'selectFilmeSubscriber' ::.. */ 
    /* Retonam um array de SimpleXMLElement */

    print_r($oFilmeSubscriber->selectFilmeSubscriber("X0001"));                           // Busca pelo Código
    // print_r($oFilmeSubscriber->selectFilmeSubscriber(null, "Template, o Filme"));         // Busca pelo Titulo do Filme
    // print_r($oFilmeSubscriber->selectFilmeSubscriber(null,null,"teste.1@email.com"));     // Busca somente pelo Email do Subscriber
}


/**
 * testeSelectPorMaisDeUmParametro
 * 
 * ..:: Teste do metodo 'selectFilmeSubscriber' de selecao de um (ou mais) registro(s) na tabela tFilmeSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Selecao de um ou mais registros que satisfazem as condicoes nos parametros;
 * * A selecao por um ou mais parametros (metodo 'selectFilmeSubscriber') retorna um array do tipo SimpleXMLElement.
 * 
 *  
 */
function testeSelectPorMaisDeUmParametro(){
    $oFilmeSubscriber = new lFilmeSubscriber();
    $oFilmeSubscriber->createTableFilmeSubscriber();
    $oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.1@email.com");
    $oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.2@email.com");
    
    /* ..:: Selecao utilizando multiplos campos ::.. */
    print_r($oFilmeSubscriber->selectFilmeSubscriber(null, "Template, o Filme", "teste.2@email.com"));
}



/**
 * testeDescobrirCodigoDoFilmeSubscriber
 * 
 * ..:: Teste do metodo de descoberta do codigo do objeto na tabela tFilmeSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * O FilmeSubscriber informado ($oFilmeSubscriber) eh buscado na tabela tFilmeSubscriber.xml, e o seu codigo 
 * eh retornado numa string.
 * 
 *  
 */
function testeDescobrirCodigoDoFilmeSubscriber(){
    $oFilmeSubscriber = new lFilmeSubscriber();
    $oFilmeSubscriber->createTableFilmeSubscriber();
    $oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.1@email.com");
    $oFilmeSubscriber->insertFilmeSubscriberCompleto("Template, o Filme", "teste.2@email.com");
     
    $oFilmeSubscriber = $oFilmeSubscriber->traduzSimpleXMLObjectToFilmeSubscriber($oFilmeSubscriber->selectFilmeSubscriber(null, "Template, o Filme", "teste.2@email.com"))[0];

    /* ..:: Para obter o codigo de um FilmeSubscriber ::.. */
    // OBS: o $oFilmeSubscriber deve possuir um ou mais atributos preenchidos
    print_r($oFilmeSubscriber->getCodigoByFilmeSubscriber($oFilmeSubscriber));
}



function main(){

    testeAtributos();
    // testeCriacao();
    // testeInsercaoPorParametro();
    // testeInsercaoPorAtributo();
    // testeUpdatePorParametro();
    // testeUpdatePorAtributo();
    // testeExclusao();
    // testeSelectPorUmParametro();
    // testeSelectPorMaisDeUmParametro();
    // testeDescobrirCodigoDoFilmeSubscriber();
    
    // $oFilmeSubscriber = new lFilmeSubscriber();
    // ..:: selectFilmeSubscriber() sem parametros retorna o conteúdo da tabela tFilmeSubscriber.xml em forma de um array da classe SimpleXMLElement ::..
    // print_r($oFilmeSubscriber->selectFilmeSubscriber());
    // ..:: getTabela() retorna o conteúdo da tabela tFilmeSubscriber.xml em forma de um array da classe lFilmeSubscriber ::..
    // print_r($oFilmeSubscriber->getTabela());
    
}

main();
?>