<?php

/**
 * ..:: Teste e documentacao da utilizacao da classe lFilme ::..
 * 
 * @author Marlon R C Franco
 * @author Marlon R C Franco <marlonrcfranco@gmail.com>
 * 
 * * IMPORTANTE: Executar qualquer funcao de teste a seguir ira modificar a tabela tFilme.xml
 * 
 * Para escolher as funcoes a serem executadas ou nao, comente ou descomente a chamada das mesmas
 * na funcao 'main' ao final deste codigo.
 * 
 */


include_once 'lFilme.php';


/**
 * testeAtributos
 * 
 * ..:: Teste de modificacao dos atributos do objeto $oFilme da classe lFilme ::..
 * 
 * Comportamento esperado:
 *
 * * $oFilme possuir os atributos passados a ele;
 * 
 */
function testeAtributos(){
    $oFilme = new lFilme();

    print_r("Antes:\n");
    print_r($oFilme);

    $oFilme->titulo = "Teste, o Filme";
    $oFilme->descricao = "Teste de atributos";
    $oFilme->genero = "suspense";
    $oFilme->ano = "2018";
    $oFilme->avaliacao = "5";
    $oFilme->diretor = "Eu";
    $oFilme->elenco = "Tu, Ele; Nós, Vós, Eles.";
    
    print_r("\nDepois:\n");
    print_r($oFilme);
}


/**
 * testeCriacao
 * 
 * ..:: Teste do metodo de criacao da tabela tFilme.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Criacao da tabela 'db/tFilme.xml' ou a sobrescrita se já houver uma tabela criada;
 * * Conteudo da tabela tFilme.xml deve ser apenas o primeiro registro: Template.
 *
 */
function testeCriacao() {
    $oFilme = new lFilme();
    print_r($oFilme->createTableFilme());
    print_r("\nVer se a tabela tFilmes.xml foi criada.");
}


/**
 * testeInsercaoPorParametro
 * 
 * ..:: Teste do metodo de insercao de um novo registro na tabela tFilme.xml passando os valores como parametro ::..
 * 
 * Comportamento esperado:
 *
 * * Insercao de cinco registros com os respectivos valores informados nos parametros;
 * * Conteudo da tabela tFilme.xml deve ser seis registros, sendo o primeiro o Template.
 *
 */
function testeInsercaoPorParametro() {
    $oFilme = new lFilme();
    $oFilme->createTableFilme();

    print_r($oFilme->insertFilmeCompleto("Teste, o Filme parte 1", "Teste de Inserção por Parâmetro.","Ação", "1993", "5", "Armando Torres", "Antonio Banderas e Gessica Alba"));
    print_r("\n");
    print_r($oFilme->insertFilmeCompleto("Teste, o Filme parte 2", "Teste de Inserção por Parâmetro.","Suspense", "1994", null,"Armando Barracas", null));
    print_r("\n");
    print_r($oFilme->insertFilmeCompleto("Teste, o Filme parte 3", "Teste de Inserção por Parâmetro.","Romance", "1995", "3.7", "Armando Tendas", "Marlon Brando"));
    print_r("\n");
    print_r($oFilme->insertFilmeCompleto("Teste, o Filme parte 4", "Teste de Inserção por Parâmetro.","Épico", "1996", "1.7", "Armando Pessoa", "Angelina Jolie"));
    print_r("\n");
    print_r($oFilme->insertFilmeCompleto("Teste, o Filme parte 5", "Teste de Inserção por Parâmetro.","Comédia", "1997", "5", "Arlindo", null));
    print_r("\n");
}


/**
 * testeInsercaoPorAtributo
 * 
 * ..:: Teste do metodo de insercao de um novo registro na tabela tFilme.xml utilizando os atributos do objeto $oFilme ::..
 * 
 * Comportamento esperado:
 *
 * * Insercao de um registro com os respectivos valores informados nos atributos do objeto $oFilme;
 * * Conteudo da tabela tFilme.xml deve ser dois registros, sendo o primeiro o Template.
 *
 */
function testeInsercaoPorAtributo() {
    $oFilme = new lFilme();
    $oFilme->createTableFilme();

    $oFilme->titulo = "Teste Insercao por Atributo";
    $oFilme->descricao = "Descrição do Teste Insercao por Atributo";
    $oFilme->genero = "Aventura";
    $oFilme->ano = "2999";
    $oFilme->avaliacao = "4.7";
    $oFilme->diretor = "Arlindo";
    $oFilme->elenco = "Angelina Jolie";

    print_r($oFilme);
    print_r($oFilme->insertFilme());
    print_r($oFilme);
}


/**
 * testeUpdatePorParametro
 * 
 * ..:: Teste do metodo 'updateFilmeCompleto' de atualização de um registro na tabela tFilme.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Atualização do registro que possui o codigo informado;
 * * A atualização por parametro ('updateFilmeCompleto') utiliza os valores passados como parametro para salvar na tabela tFilme.xml;
 * * Eh necessario que o codigo do Filme seja conhecido.
 * 
 *   
 */
function testeUpdatePorParametro(){
    $oFilme = new lFilme();
    $oFilme->createTableFilme();

    /* ..:: Atualizacao por parametro utilizando o metodo "updateFilmeCompleto" ::.. */
    print_r($oFilme->updateFilmeCompleto("F0000", "Teste Update por Parametro",null,null,null,null,null,"Angelina Jolie"));
}


/**
 * testeUpdatePorAtributo
 * 
 * ..:: Teste do metodo 'updateFilme' de atualização de um registro na tabela tFilme.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Atualização do registro que possui o codigo informado;
 * * A atualização por atributo ('updateFilme') utiliza os nos atributos do objeto $oFilme;
 * * Eh necessario que o codigo do Filme seja conhecido.
 * 
 *   
 */
function testeUpdatePorAtributo(){
    $oFilme = new lFilme();
    $oFilme->createTableFilme();
    
    /* ..:: Atualizacao por atributo ::.. */
    $oFilme->codigo = "F0000"; 
    // como exemplo, eh utilizado o codigo do Template ("F0000"), porem este codigo 
    // deve ser adquirido atraves do metodo "getCodigoByFilme", ou por qualquer um dos metodos de Select (ver: testeSelect())
    $oFilme->titulo = "Teste Update por Atributo";
    $oFilme->elenco = "Angelina Jolie e cia.";
    print_r($oFilme->updateFilme());
}


/**
 * testeExclusao
 * 
 * ..:: Teste do metodo de exclusao de um registro na tabela tFilme.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Exclusao de um registro com o codigo igual ao informado no parametro do metodo 'excluirFilme';
 * * Conteudo da tabela tFilme.xml deve ser zero registros, pois o primeiro e unico registro (Template) foi excluido.
 * 
 * 
 */
function testeExclusao(){
    $oFilme = new lFilme();
    $oFilme->createTableFilme();

    print_r($oFilme->excluirFilme("F0000"));
}


/**
 * testeSelectPorUmParametro
 * 
 * ..:: Teste dos metodos de selecao de um (ou mais) registro(s) na tabela tFilme.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Selecao de um ou mais registros que satisfazem as condicoes nos parametros;
 * * A selecao utilizando os metodos iniciados por 'get' retornam um array do tipo lFilme.
 * * A selecao utilizando o metodo 'selectFilme' retorna um array do tipo SimpleXMLElement.
 * 
 *  
 */
function testeSelectPorUmParametro(){
    $oFilme = new lFilme();
    $oFilme->createTableFilme();
    $oFilme->insertFilmeCompleto("Teste, o Filme parte 1", "Teste de Inserção por Parâmetro.","Ação", "1993", "5", "Armando Torres", "Antonio Banderas e Gessica Alba");
    $oFilme->insertFilmeCompleto("Teste, o Filme parte 2", "Teste de Inserção por Parâmetro.","Suspense", "1994", null,"Armando Barracas", null);

    /* ..:: Consulta utilizando os metodos 'get' ::.. */
    /* Retornam um array de lFilme */

    print_r($oFilme->getFilmeByCodigo("F0001"));
    // print_r($oFilme->getFilmeByTitulo("Teste, o Filme parte 1"));
    // print_r($oFilme->getFilmeByDescricao("Teste de Inserção por Parâmetro."));
    // print_r($oFilme->getFilmeByGenero("Ação"));
    // print_r($oFilme->getFilmeByAno("1993"));
    // print_r($oFilme->getFilmeByAvaliacao("4.7", "5"));
    // print_r($oFilme->getFilmeByDiretor("Armando Barracas"));
    // print_r($oFilme->getFilmeByElenco("Antonio Banderas e Gessica Alba"));


    /* ..:: Consultas equivalentes às acima mas utilizando o metodo 'selectFilme' ::.. */ 
    /* Retonam um array de SimpleXMLElement */

    print_r($oFilme->selectFilme("F0001"));                                                // Busca pelo Código
    // print_r($oFilme->selectFilme(null,"Teste, o Filme parte 1"));                          // Busca pelo Título
    // print_r($oFilme->selectFilme(null,null,"Teste de Inserção por Parâmetro."));           // Busca somente pela Descrição
    // print_r($oFilme->selectFilme(null,null,null, "Ação"));                                 // Busca somente pelo Genero  
    // print_r($oFilme->selectFilme(null,null,null,null, "1993"));                            // Busca somente pelo Ano de Lançamento
    // print_r($oFilme->selectFilme(null,null,null,null,null, "4.7", "5"));                   // Busca somente pela Avaliação
    // print_r($oFilme->selectFilme(null,null,null,null,null,null,null, "Armando Barracas")); // Busca somente pelo Diretor
    // print_r($oFilme->selectFilme(null,null,null,null,null,null,null,null, "Antonio Banderas e Gessica Alba"));  // Busca somente pelo Elenco
}


/**
 * testeSelectPorMaisDeUmParametro
 * 
 * ..:: Teste do metodo 'selectFilme' de selecao de um (ou mais) registro(s) na tabela tFilme.xml ::..
 * 
 * Comportamento esperado:
 *
 * * Selecao de um ou mais registros que satisfazem as condicoes nos parametros;
 * * A selecao por um ou mais parametros (metodo 'selectFilme') retorna um array do tipo SimpleXMLElement.
 * 
 *  
 */
function testeSelectPorMaisDeUmParametro(){
    $oFilme = new lFilme();
    $oFilme->createTableFilme();
    $oFilme->insertFilmeCompleto("Teste, o Filme parte 1", "Teste de Inserção por Parâmetro.","Ação", "1993", "5", "Armando Barracas", "Antonio Banderas e Gessica Alba");
    $oFilme->insertFilmeCompleto("Teste, o Filme parte 2", "Teste de Inserção por Parâmetro.","Suspense", "1994", "5","Armando Barracas", null);
    
    /* ..:: Selecao utilizando multiplos campos ::.. */
    print_r($oFilme->selectFilme(null,null, "Teste de Inserção por Parâmetro.",null,null, "5", "5", "Armando Barracas"));
}



/**
 * testeDescobrirCodigoDoFilme
 * 
 * ..:: Teste do metodo '' de selecao de um (ou mais) registro(s) na tabela tFilme.xml ::..
 * 
 * Comportamento esperado:
 *
 * * O Filme informado ($oFilme) eh buscado na tabela tFilme.xml, e o seu codigo 
 * eh retornado numa string.
 * 
 *  
 */
function testeDescobrirCodigoDoFilme(){
    $oFilme = new lFilme();
    $oFilme->createTableFilme();
    $oFilme->insertFilmeCompleto("Teste, o Filme parte 1", "Teste de Inserção por Parâmetro.","Ação", "1993", "5", "Armando Barracas", "Antonio Banderas e Gessica Alba");
    $oFilme->insertFilmeCompleto("Teste, o Filme parte 2", "Teste de Inserção por Parâmetro.","Suspense", "1994", "5","Armando Barracas", null);
    
    // Para preencher os atributos de $oFilme buscando na tabela tFilme.xml pelo Titulo ou por outro valor (ver testeSelect())
    // OBS: o retorno destas funcoes de selecao eh um array, por isso foi informado o indice [0] para pegar o elemento de interesse.
    $oFilme = $oFilme->getFilmeByTitulo("Teste, o Filme parte 1")[0];

    /* ..:: Para obter o codigo de um Filme ::.. */
    // OBS: o $oFilme deve possuir um ou mais atributos preenchidos
    print_r($oFilme->getCodigoByFilme($oFilme));
}



function main(){

    // testeAtributos();
    testeCriacao();
    // testeInsercaoPorParametro();
    // testeInsercaoPorAtributo();
    // testeUpdatePorParametro();
    // testeUpdatePorAtributo();
    // testeExclusao();
    // testeSelectPorUmParametro();
    // testeSelectPorMaisDeUmParametro();
    // testeDescobrirCodigoDoFilme();
    
    // $oFilme = new lFilme();
    // ..:: selectFilme() sem parametros retorna o conteúdo da tabela tFilme.xml em forma de um array da classe SimpleXMLElement ::..
    // print_r($oFilme->selectFilme());
    // ..:: getTabela() retorna o conteúdo da tabela tFilme.xml em forma de um array da classe lFilme ::..
    //print_r($oFilme->getTabela());
    
}

main();
?>