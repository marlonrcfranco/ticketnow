<?php

/**
 * ..:: Teste e documentacao da utilizacao da classe lSubscriber ::..
 * 
 * @author Marlon R C Franco
 * @author Marlon R C Franco <marlonrcfranco@gmail.com>
 * 
 * * IMPORTANTE: Executar qualquer funcao de teste a seguir ira modificar a tabela tSubscriber.xml
 * 
 * Para escolher as funcoes a serem executadas ou nao, comente ou descomente a chamada das mesmas
 * na funcao 'main' ao final deste codigo.
 * 
 */


include_once 'lSubscriber.php';


/**
 * testeAtributos
 * 
 * ..:: Teste de modificacao dos atributos do objeto $oSubscriber da classe lSubscriber ::..
 * 
 * Comportamento esperado:
 *
 * * $oSubscriber possuir os atributos passados a ele;
 * 
 */
function testeAtributos(){
    $oSubscriber = new lSubscriber();

    print_r("Antes:\n");
    print_r($oSubscriber);

    $oSubscriber->nome = "Teste Atributos Jr.";
    $oSubscriber->cpf = "123.456.789-00";
    $oSubscriber->email = "teste.atributos@email.com";
    $oSubscriber->telefone = "+55 53 97777-7777";
    $oSubscriber->endereco = "Rua dos Bobos, 0";
    $oSubscriber->bairro = "Downtown";
    $oSubscriber->cep = "77777-777";
    
    print_r("\nDepois:\n");
    print_r($oSubscriber);
}


/**
 * testeCriacao
 * 
 * ..:: Teste do metodo de criacao da tabela tSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Criacao da tabela 'tSubscriber.xml' ou a sobrescrita se já houver uma tabela criada;
 * * Conteudo da tabela tSubscriber.xml deve ser apenas o primeiro registro: Template.
 *
 */
function testeCriacao() {
    $oSubscriber = new lSubscriber();
    print_r($oSubscriber->createTableSubscriber());
    print_r("\nVer se a tabela tSubscribers.xml foi criada.");
}


/**
 * testeInsercaoPorParametro
 * 
 * ..:: Teste do metodo de insercao de um novo registro na tabela tSubscriber.xml passando os valores como parametro ::..
 * 
 * Comportamento esperado:
 *
 * * Insercao de cinco registros com os respectivos valores informados nos parametros;
 * * Conteudo da tabela tSubscriber.xml deve ser seis registros, sendo o primeiro o Template.
 *
 */
function testeInsercaoPorParametro() {
    $oSubscriber = new lSubscriber();
    $oSubscriber->createTableSubscriber();

    print_r($oSubscriber->insertSubscriberCompleto("Teste I", "111.111.111-11","teste.1@email.com", "+55 53 97777-7777", "Rua I, 1", "Downtown", "22222-222"));
    print_r("\n");
    print_r($oSubscriber->insertSubscriberCompleto("Teste II", "222.222.222-22","teste.2@email.com", "+55 53 97777-7777", "Rua II, 2","Uptown", "22222-222"));
    print_r("\n");
    print_r($oSubscriber->insertSubscriberCompleto("Teste III", "333.333.333-33","teste.3@email.com", null, "Av. Três, 3", "Chinatown", "33333-33"));
    print_r("\n");
    print_r($oSubscriber->insertSubscriberCompleto("Teste IV", "444.444.444-44","teste.4@email.com", "+55 53 97777-7779", null, "Little Italy", null));
    print_r("\n");
    print_r($oSubscriber->insertSubscriberCompleto("Teste V", "555.555.555-55","teste.5@email.com", null, null, null, "55555-555"));
    print_r("\n");
}


/**
 * testeInsercaoPorAtributo
 * 
 * ..:: Teste do metodo de insercao de um novo registro na tabela tSubscriber.xml utilizando os atributos do objeto $oSubscriber ::..
 * 
 * Comportamento esperado:
 *
 * * Insercao de um registro com os respectivos valores informados nos atributos do objeto $oSubscriber;
 * * Conteudo da tabela tSubscriber.xml deve ser dois registros, sendo o primeiro o Template.
 *
 */
function testeInsercaoPorAtributo() {
    $oSubscriber = new lSubscriber();
    $oSubscriber->createTableSubscriber();

    $oSubscriber->nome = "Teste Inserção por Atributos Jr.";
    $oSubscriber->cpf = "123.456.789-00";
    $oSubscriber->email = "teste.insercao.por.atributos@email.com";
    $oSubscriber->telefone = "+55 53 97777-7777";
    $oSubscriber->endereco = "Rua dos Bobos, 0";
    $oSubscriber->bairro = "Downtown";
    $oSubscriber->cep = "77777-777";

    print_r($oSubscriber);
    print_r($oSubscriber->insertSubscriber());
    print_r($oSubscriber);
}


/**
 * testeUpdatePorParametro
 * 
 * ..:: Teste do metodo 'updateSubscriberCompleto' de atualização de um registro na tabela tSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Atualização do registro que possui o codigo informado;
 * * A atualização por parametro ('updateSubscriberCompleto') utiliza os valores passados como parametro para salvar na tabela tSubscriber.xml;
 * * Eh necessario que o codigo do Subscriber seja conhecido.
 * 
 *   
 */
function testeUpdatePorParametro(){
    $oSubscriber = new lSubscriber();
    $oSubscriber->createTableSubscriber();

    /* ..:: Atualizacao por parametro utilizando o metodo "updateSubscriberCompleto" ::.. */
    print_r($oSubscriber->updateSubscriberCompleto("S0000", "Teste Update por Parametro",null,null,null, "Alterado Nome, Endereço e CEP",null, "00000-000"));
}


/**
 * testeUpdatePorAtributo
 * 
 * ..:: Teste do metodo 'updateSubscriber' de atualização de um registro na tabela tSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Atualização do registro que possui o codigo informado;
 * * A atualização por atributo ('updateSubscriber') utiliza os nos atributos do objeto $oSubscriber;
 * * Eh necessario que o codigo do Subscriber seja conhecido.
 * 
 *   
 */
function testeUpdatePorAtributo(){
    $oSubscriber = new lSubscriber();
    $oSubscriber->createTableSubscriber();
    
    /* ..:: Atualizacao por atributo ::.. */
    $oSubscriber->codigo = "S0000"; 
    // como exemplo, eh utilizado o codigo do Template ("S0000"), porem este codigo 
    // deve ser adquirido atraves do metodo "getCodigoBySubscriber", ou por qualquer um dos metodos de Select (ver: testeSelect())
    $oSubscriber->nome = "Teste Update por Atributo";
    $oSubscriber->endereco = "Alterado apenas Nome e Endereço";
    print_r($oSubscriber->updateSubscriber());
}


/**
 * testeExclusao
 * 
 * ..:: Teste do metodo de exclusao de um registro na tabela tSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Exclusao de um registro com o codigo igual ao informado no parametro do metodo 'excluirSubscriber';
 * * Conteudo da tabela tSubscriber.xml deve ser zero registros, pois o primeiro e unico registro (Template) foi excluido.
 * 
 * 
 */
function testeExclusao(){
    $oSubscriber = new lSubscriber();
    $oSubscriber->createTableSubscriber();

    print_r($oSubscriber->excluirSubscriber("S0000"));
}


/**
 * testeSelectPorUmParametro
 * 
 * ..:: Teste dos metodos de selecao de um (ou mais) registro(s) na tabela tSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Selecao de um ou mais registros que satisfazem as condicoes nos parametros;
 * * A selecao utilizando os metodos iniciados por 'get' retornam um array do tipo lSubscriber.
 * * A selecao utilizando o metodo 'selectSubscriber' retorna um array do tipo SimpleXMLElement.
 * 
 *  
 */
function testeSelectPorUmParametro(){
    $oSubscriber = new lSubscriber();
    $oSubscriber->createTableSubscriber();
    print_r($oSubscriber->insertSubscriberCompleto("Teste I", "111.111.111-11","teste.1@email.com", "+55 53 97777-7777", "Rua I, 1", "Downtown", "22222-222"));
    print_r($oSubscriber->insertSubscriberCompleto("Teste II", "222.222.222-22","teste.2@email.com", "+55 53 97777-7777", "Rua II, 2","Uptown", "22222-222"));
    
    /* ..:: Consulta utilizando os metodos 'get' ::.. */
    /* Retornam um array de lSubscriber */

    print_r($oSubscriber->getSubscriberByCodigo("S0001"));
    // print_r($oSubscriber->getSubscriberByNome("Teste I"));
    // print_r($oSubscriber->getSubscriberByCPF("111.111.111-11"));
    // print_r($oSubscriber->getSubscriberByEmail("teste.1@email.com"));
    // print_r($oSubscriber->getSubscriberByTelefone("+55 53 97777-7777"));
    // print_r($oSubscriber->getSubscriberByEndereco("Rua I, 1"));
    // print_r($oSubscriber->getSubscriberByBairro("Downtown"));
    // print_r($oSubscriber->getSubscriberByCEP("22222-222"));


    /* ..:: Consultas equivalentes às acima mas utilizando o metodo 'selectSubscriber' ::.. */ 
    /* Retonam um array de SimpleXMLElement */

    print_r($oSubscriber->selectSubscriber("S0001"));                                               // Busca pelo Código
    // print_r($oSubscriber->selectSubscriber(null,"Teste I"));                                         // Busca pelo Nome
    // print_r($oSubscriber->selectSubscriber(null,null,"111.111.111-11"));                             // Busca somente pelo CPF
    // print_r($oSubscriber->selectSubscriber(null,null,null, "teste.1@email.com"));                    // Busca somente pelo Email  
    // print_r($oSubscriber->selectSubscriber(null,null,null,null, "+55 53 97777-7777"));               // Busca somente pelo Telefone
    // print_r($oSubscriber->selectSubscriber(null,null,null,null,null, "Rua I, 1"));                   // Busca somente pela Endereço
    // print_r($oSubscriber->selectSubscriber(null,null,null,null,null,null, "Downtown"));              // Busca somente pelo Bairro
    // print_r($oSubscriber->selectSubscriber(null,null,null,null,null,null,null, "22222-222"));        // Busca somente pelo CEP
}


/**
 * testeSelectPorMaisDeUmParametro
 * 
 * ..:: Teste do metodo 'selectSubscriber' de selecao de um (ou mais) registro(s) na tabela tSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Selecao de um ou mais registros que satisfazem as condicoes nos parametros;
 * * A selecao por um ou mais parametros (metodo 'selectSubscriber') retorna um array do tipo SimpleXMLElement.
 * 
 *  
 */
function testeSelectPorMaisDeUmParametro(){
    $oSubscriber = new lSubscriber();
    $oSubscriber->createTableSubscriber();
    $oSubscriber->insertSubscriberCompleto("Teste I", "111.111.111-11","teste.1@email.com", "+55 53 97777-7777", "Rua I, 1", "Downtown", "22222-222");
    $oSubscriber->insertSubscriberCompleto("Teste II", "222.222.222-22","teste.2@email.com", "+55 53 97777-7777", "Rua II, 2","Uptown", "22222-222");
    
    /* ..:: Selecao utilizando multiplos campos ::.. */
    print_r($oSubscriber->selectSubscriber(null, null, null, null, "+55 53 97777-7777", null, null, "22222-222"));
}



/**
 * testeDescobrirCodigoDoSubscriber
 * 
 * ..:: Teste do metodo de descoberta do codigo do objeto na tabela tSubscriber.xml ::..
 * 
 * Comportamento esperado:
 *
 * * O Subscriber informado ($oSubscriber) eh buscado na tabela tSubscriber.xml, e o seu codigo 
 * eh retornado numa string.
 * 
 *  
 */
function testeDescobrirCodigoDoSubscriber(){
    $oSubscriber = new lSubscriber();
    $oSubscriber->createTableSubscriber();
    $oSubscriber->insertSubscriberCompleto("Teste I", "111.111.111-11","teste.1@email.com", "+55 53 97777-7777", "Rua I, 1", "Downtown", "22222-222");
    $oSubscriber->insertSubscriberCompleto("Teste II", "222.222.222-22","teste.2@email.com", "+55 53 97777-7777", "Rua II, 2","Uptown", "22222-222");
    
    // Para preencher os atributos de $oSubscriber buscando na tabela tSubscriber.xml pelo CPF ou por outro valor (ver testeSelect())
    // OBS: o retorno destas funcoes de selecao eh um array, por isso foi informado o indice [0] para pegar o elemento de interesse.
    $oSubscriber = $oSubscriber->getSubscriberByCPF("111.111.111-11")[0];

    /* ..:: Para obter o codigo de um Subscriber ::.. */
    // OBS: o $oSubscriber deve possuir um ou mais atributos preenchidos
    print_r($oSubscriber->getCodigoBySubscriber($oSubscriber));
}



function main(){

    // testeAtributos();
    // testeCriacao();
    // testeInsercaoPorParametro();
    // testeInsercaoPorAtributo();
    // testeUpdatePorParametro();
    // testeUpdatePorAtributo();
    // testeExclusao();
    // testeSelectPorUmParametro();
    // testeSelectPorMaisDeUmParametro();
    // testeDescobrirCodigoDoSubscriber();
    
    // $oSubscriber = new lSubscriber();
    // ..:: selectSubscriber() sem parametros retorna o conteúdo da tabela tSubscriber.xml em forma de um array da classe SimpleXMLElement ::..
    // print_r($oSubscriber->selectSubscriber());
    // ..:: getTabela() retorna o conteúdo da tabela tSubscriber.xml em forma de um array da classe lSubscriber ::..
    // print_r($oSubscriber->getTabela());
    
}

main();
?>