<?php

/**
 * ..:: Logica para manipulacao da tabela tFilme.xml ::..
 * 
 * @author Marlon R C Franco
 * @author Marlon R C Franco <marlonrcfranco@gmail.com>
 * 
 */

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
date_default_timezone_set('America/Sao_Paulo');

include_once 'ILogica.php';

/**
 * Classe de lógica para manipulação da tabela tFilme.xml
 * 
 * @var string $codigo          Codigo de identificacao no padrão "F0000"
 * @var string $titulo          Titulo do filme
 * @var string $descricao       Descricao do filme
 * @var string $genero          Genero do filme (ex.: Ação, Suspense, Romance)
 * @var string $ano             Ano de lançamento do filme (ex.: 2007)
 * @var string $avaliacao       Rating do filme, numa escala de 0.0 a 5.0
 * @var string $diretor         Diretor do filme
 * @var string $elenco          Elenco do filme, apenas os atores principais
 * @var string $reg_date        Data e Hora de registro do filme do sistema
 * @var boolean $semEspaco      Flag para verificacao se ha espaco vazio no meio da tabela tFilme.xml
 * @var string $tablePathFilme  File path da tabela tFilme.xml
 * 
 */
class lFilme implements ILogica {
	public $codigo;
	public $titulo;
	public $descricao;
	public $genero;
	public $ano;
	public $avaliacao;
	public $diretor;
	public $elenco;
	public $reg_date;
	private $semEspaco;
	private $tablePathFilme;
	
/**
 * __construct
 * 
 * Construtor da classe lFilme, que inicializa os parametros como null.
 * 
 * @param void
 * @return void
 * 
 */
	function __construct() {
        $this->codigo = null;
		$this->titulo = null;
		$this->descricao = null;
		$this->genero = null;
		$this->ano = null;
		$this->avaliacao = null;
		$this->diretor = null;
		$this->elenco = null;
		$this->reg_date = null;
		$this->semEspaco = false;
		//$this->tablePathFilme = $_SERVER['DOCUMENT_ROOT']."/Database/db/tFilme.xml";
		$this->tablePathFilme = "0.SistemaWeb/Database/db/tFilme.xml";
    }
	

	/**
	 * createTable
	 *
	 * Metodo para a criacao da tabela Filme.xml, contendo o primeiro registro como Template.
	 * 
	 * @param void
	 * @return int 1|0	Retorna 1 se houve ERRO, ou 0 se a criacao da tabela foi realizada com sucesso.
	 * 
	 */
	public function createTable() {
		$filePathFilme = $this->tablePathFilme;
		$file = fopen($filePathFilme, "w+");
	$template = <<<XML
<?xml version="1.0" encoding="UTF-8"?>
<root>
	<filme>
		<codigo>F0000</codigo>
		<titulo>Template, o Filme</titulo>
		<descricao>Este é o primeiro registro, serve como Template para os demais [é gerado automaticamente ao criar uma nova tabela]</descricao>
		<genero>Aventura</genero>
		<ano>2007</ano>
		<avaliacao>(sem avaliação)</avaliacao>
		<diretor>(sem informações de diretor)</diretor>
		<elenco>(sem informações de elenco)</elenco>
		<reg_date>1993-05-31 23:59:59</reg_date>
  </filme>
</root>
XML;
		fwrite($file,$template);
		fclose($file);
		$file = fopen($filePathFilme, "r");
		if ((strcmp(fread($file, 7), "") == 0) or (fread($file, 7) == null)) {
			fclose($file);
			return 1; // ERRO ao criar a tabela
		}else {
			fclose($file);
			return 0; // Tabela criada com sucesso
		}
	}


	/**
	 * buscaEspacoVazio
	 * 
	 * Metodo para buscar o espaco vazio na tabela, retornando o nodo anterior ao espaco vazio, ou
	 * o ultimo nodo, caso nao haja espaco vazio.
	 * 
	 * @param DOMDocument $domXml                   Objeto DOMDocument contendo o XML.
	 * @return DOMDocument $domNode|$domLastNode    Retorna o Nodo anterior ao espaco vazio OU o ultimo Nodo, caso nao haja espaco vazio.
	 *
	 */
	public function buscaEspacoVazio(DOMDocument $domXml) {
		$i = 0;
		$domLastNode;
		$domLista = $domXml->getElementsByTagName('codigo');
		foreach($domLista as $domNode) {
			$codigo = $domNode->nodeValue;
			$domLastNode = $domNode;
			$codAnterior = ("F".sprintf('%04d', $i)); $i++;
			if( strcmp($codAnterior,$codigo) != 0) {
				//print_r("retornou:".$domNode->parentNode->nodeName." codigo: ".$codigo."\n");
				$this->semEspaco = false;
				return $domNode->parentNode;
			}
		}
		$this->semEspaco = true;
		return $domLastNode->parentNode; // ultimo elemento da lista
	}


	/**
	 * traduzSimpleXMLObjectToFilme
	 *
	 * Traduz um array de SimpleXMLObject em um array de lFilme
	 * 
	 * @param SimpleXMLElement[] $ListSimpleXMLObject   Array de SimpleXMLObject.
	 * @return lFilme[] $ListaFilme                     Array de lFilme.
	 * 
	 */
	public function traduzSimpleXMLObjectToFilme($ListSimpleXMLObject) {
		$ListaFilme = array();
		foreach($ListSimpleXMLObject as $SimpleXMLObject) {
			$oFilme = new lFilme();
			$oFilme->codigo    = (string)$SimpleXMLObject->codigo;
			$oFilme->titulo    = (string)$SimpleXMLObject->titulo;
			$oFilme->descricao = (string)$SimpleXMLObject->descricao;
			$oFilme->genero    = (string)$SimpleXMLObject->genero;
			$oFilme->ano       = (string)$SimpleXMLObject->ano;
			$oFilme->avaliacao = (string)$SimpleXMLObject->avaliacao;
			$oFilme->diretor   = (string)$SimpleXMLObject->diretor;
			$oFilme->elenco    = (string)$SimpleXMLObject->elenco;
			$oFilme->reg_date  = (string)$SimpleXMLObject->reg_date;
			array_push($ListaFilme, $oFilme);
		}
		return $ListaFilme;
	}


	/**
	 * validaFormatoAvaliacao
	 *
	 * Verifica se a Avaliacao informada está no formato correto (está entre 0.0 a 5.0)
	 * 
	 * @param string $avaliacao  Rating do Filme a ser verificado.
	 * @return false|true        'false' se Sucesso | 'true' se ERRO: Formato da avaliação inválido.
	 * 
	 */
	public function validaFormatoAvaliacao(string $avaliacao) {
		if ((floatval($avaliacao) < 0.0) || (floatval($avaliacao) > 5.0)) {
			return true; // ERRO: Formato da avaliação inválido.
		}
		return false; // Sucesso
	}


	/**
	 * verificaTituloFilme
	 *
	 * Verifica se o Titulo informado já está presente na tabela tFilme.xml
	 * 
	 * @param string $titulo    Titulo do Filme a ser verificado.
	 * @return false|true       'false' se Sucesso | 'true' se ERRO: Filme já cadastrado.
	 * 
	 */
	public function verificaTituloFilme(string $titulo) {
		if ($this->getFilmeByTitulo($titulo) == null) {
			return false; // Sucesso
		}
		return true; // ERRO: Filme já cadastrado.
	}


	/**
	 * insertFilmeCompleto
	 *
	 * Salva na tabela tFilme.xml o novo Filme que possui os parametros informados.
	 * 
	 * @param string $titulo          Titulo do filme
	 * @param string $descricao       Descricao do filme
	 * @param string $genero          Genero do filme (ex.: acao, suspense, romance)
	 * @param string $ano             Ano de lançamento do filme (ex.: 2007)
	 * @param string $avaliacao       [Opcional] Rating do filme, numa escala de 0.0 a 5.0
	 * @param string $diretor         [Opcional] Diretor do filme
	 * @param string $elenco          [Opcional] Elenco do filme, apenas os atores principais
	 * 
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function insert(string $titulo, string $descricao, string $genero, string $ano, string $avaliacao=null, string $diretor=null, string $elenco=null) {
		$tablePath  = $this->tablePathFilme;
		$domXML = new DOMDocument('1.0');
		$domXML->preserveWhiteSpace = false;
		$domXML->formatOutput = true;
		if ($domXML->load($tablePath)) {
			//echo "segue o baile\n";
		}else {
			echo "\n\nTabela '". $tablePath."' não encontrada.\nCriando tabela...\n\n";
			if( createTableFilme($tablePath) ) exit("ERRO ao criar a tabela");
			else echo "\nTabela '".$tablePath. "' criada com sucesso!\n";
			$domXML->load($tablePath);
		}
		$root = $domXML->getElementsByTagName('root')->item(0);
		// busca o primeiro espaco vazio
		$domPosition = lFilme::buscaEspacoVazio($domXML);
		//Extrai o codigo e transforma em int
		$strCodigo = $domPosition->firstChild->nodeValue;
		$strCodigo = substr($strCodigo, 1);
		$intCodigo = intval($strCodigo);
		$Filme = $domXML->createElement('filme');
		if ($this->semEspaco) {
			$codigo = "F".sprintf('%04d', $intCodigo + 1);
			$root->appendChild($Filme);
		}else {
			$codigo = "F".sprintf('%04d', $intCodigo - 1);
			$root->insertBefore($Filme, $domPosition);
		}
		// ******* Inserção do Código *******
		$codigoElement = $domXML->createElement("codigo", $codigo);
		$Filme->appendChild($codigoElement);
		// ******* Inserção do Titulo *******
		if($titulo == null) {
			return "ERRO: Campo 'Título' é obrigatório.";
		}
		if($this->verificaTituloFilme($titulo)){
			return "ERRO: Este Título já está cadastrado.";
		}
		$tituloElement = $domXML->createElement("titulo", $titulo);
		$Filme->appendChild($tituloElement);
		// ******* Inserção da Descricao *******
		if($descricao == null) {
			return "ERRO: Campo 'Descrição' é obrigatório.";
		}
		$descricaoElement = $domXML->createElement("descricao", $descricao);
		$Filme->appendChild($descricaoElement);
		// ******* Inserção do Genero *******
		if($genero == null) {
			return "ERRO: Campo 'Gênero' é obrigatório.";
		}
		$generoElement = $domXML->createElement("genero", $genero);
		$Filme->appendChild($generoElement);
		// ******* Inserção do Ano *******
		if($ano == null) {
			return "ERRO: Campo 'Ano' é obrigatório.";
		}
		$anoElement = $domXML->createElement("ano", $ano);
		$Filme->appendChild($anoElement);
		// ******* Inserção da Avaliacao *******
		if($avaliacao == null) {
			$avaliacao = "(sem avaliação)"; // Avaliacao default
		}
		if($this->validaFormatoAvaliacao($avaliacao)){
			return "ERRO: Campo 'Avaliação' deve ser preenchido com valores entre 0.0 e 5.0 (podendo ser 0.0 ou 5.0 também)";
		}
		$avaliacaoElement = $domXML->createElement("avaliacao", $avaliacao);
		$Filme->appendChild($avaliacaoElement);
		// ******* Inserção do Diretor *******
		if($diretor == null) {
			$diretor = "(sem informações de diretor)";
		}
		$diretorElement = $domXML->createElement("diretor", $diretor);
		$Filme->appendChild($diretorElement);
		// ******* Inserção do Elenco *******
		if($elenco == null) {
			$elenco = "(sem informações de elenco)";
		}
		$elencoElement = $domXML->createElement("elenco", $elenco);
		$Filme->appendChild($elencoElement);
		// ******* Inserção da Data de Registro no Sitema *******
		$reg_dateElement = $domXML->createElement("reg_date", date("Y-m-d H:i:s",time()));
		$Filme->appendChild($reg_dateElement);
		if($domXML->save($tablePath)) {
			return "Inserção realizada com sucesso!";
		}else {
			return "Erro ao inserir registro de Filme.";
		}
	}


	/**
	 * selectFilme
	 *
	 * Seleciona na tabela tFilme.xml todos os Filmes que possuem os campos informados, retornando um array do tipo SimpleXMLElement.
	 *
	 * @param string $codigo          [Opcional] Codigo de identificacao no padrão "F0000"
	 * @param string $titulo          [Opcional] Titulo do filme
	 * @param string $descricao       [Opcional] Descricao do filme
	 * @param string $genero          [Opcional] Genero do filme (ex.: acao, suspense, romance)
	 * @param string $ano             [Opcional] Ano de lançamento do filme (ex.: 2007)
	 * @param string $avaliacaoMinima [Opcional] Rating do filme mínino, numa escala de 0.0 a 5.0
	 * @param string $avaliacaoMaxima [Opcional] Rating do filme máximo, numa escala de 0.0 a 5.0
	 * @param string $diretor         [Opcional] Diretor do filme
	 * @param string $elenco          [Opcional] Elenco do filme, apenas os atores principais
	 * @param string $reg_date        [Opcional] Data e Hora de registro do filme do sistema
	 * 
	 * @return SimpleXMLElement[] $xml	Retorna um array de SimpleXMLObject contendo o resultado da consulta.
	 * 
	 */
	public function selectFilme(string $codigo = null, string $titulo = null, string $descricao = null, string $genero = null, string $ano = null, string $avaliacaoMinima = null, string $avaliacaoMaxima = null, string $diretor=null, string $elenco=null, string $reg_date = null) {
		$tablePath = $this->tablePathFilme;
		$xml=simplexml_load_file($tablePath) or die("Error: Cannot create object");
		$maisDeUmParametro = false;
		if (($codigo == null) && 
			($titulo == null) &&
			($descricao == null) &&
			($genero == null) &&
			($ano == null) && 
			($avaliacaoMinima == null) &&
			($avaliacaoMaxima == null) &&
			($diretor == null) && 
			($elenco == null) &&
			($reg_date == null))
		{
			$xPathQuery = "filme";
			return $xml->xpath($xPathQuery); // Retorna um Array de SimpleXML Object, contendo os resultados 
		}
		$xPathQuery = "filme[";
		if ($codigo != null) {
			$xPathQuery = $xPathQuery."codigo/text()='".$codigo."'";
			$maisDeUmParametro = true;
		} 
		if ($titulo != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."titulo/text()='".$titulo."'";
			$maisDeUmParametro = true;
		}
		if ($descricao != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."descricao/text()='".$descricao."'";
			$maisDeUmParametro = true;
		}
		if ($genero != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."genero/text()='".$genero."'";
			$maisDeUmParametro = true;
		}
		if ($ano != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."ano/text()='".$ano."'";
			$maisDeUmParametro = true;
		}
		if (($avaliacaoMinima != null) && ($avaliacaoMaxima != null)) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			// $xPathQuery = $xPathQuery."avaliacao/text()='".$avaliacao."'";
			$xPathQuery = $xPathQuery."avaliacao>=".$avaliacaoMinima." and avaliacao<=".$avaliacaoMaxima."";
			$maisDeUmParametro = true;
		}
		if ($diretor != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."diretor/text()='".$diretor."'";
			$maisDeUmParametro = true;
		}
		if ($elenco != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."elenco/text()='".$elenco."'";
			$maisDeUmParametro = true;
		}
		if ($reg_date != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."reg_date/text()='".$reg_date."'";
			$maisDeUmParametro = true;
		}
		$xPathQuery = $xPathQuery."]";
		$xml = $xml->xpath($xPathQuery);
		return $xml; // Retorna um Array de SimpleXML Object, contendo os resultados
	}


	/**
	 * excluirFilme
	 *
	 * Exclui da tabela tFilme.xml o Filme que possui o codigo informado.
	 * 
	 * @param string $codigo             Codigo do Filme a ser excluido da tabela.
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function excluirFilme(string $codigo) {
		$tablePath = $this->tablePathFilme;
		$Filme = $this->selectFilme($codigo);
		if($Filme == null) {
			return "ERRO: Não há Filme com o código ".$codigo.".";
		}
		$domFilme = dom_import_simplexml($Filme[0]);
		$domXML = $domFilme->parentNode->parentNode; //  documento XML
		$domFilme->parentNode->removeChild($domFilme);
		if($domXML->save($tablePath)) {
			return "Exclusão efetuada com sucesso!";
		}else {
			return "ERRO: Ao salvar modificações na tabela ".$tablePath.".";
		}
	}


	/**
	 * updateFilmeCompleto
	 *
	 * Salva na tabela tFilme.xml as alteracoes no Filme que possui o codigo informado.
	 * 
	 * @param string $codigo          Codigo de identificacao no padrão "F0000"
	 * @param string $titulo          [Opcional] Titulo do filme
	 * @param string $descricao       [Opcional] Descricao do filme
	 * @param string $genero          [Opcional] Genero do filme (ex.: acao, suspense, romance)
	 * @param string $ano             [Opcional] Ano de lançamento do filme (ex.: 2007)
	 * @param string $avaliacao       [Opcional] Rating do filme, numa escala de 0.0 a 5.0
	 * @param string $diretor         [Opcional] Diretor do filme
	 * @param string $elenco          [Opcional] Elenco do filme, apenas os atores principais
	 * 
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function updateFilmeCompleto(string $codigo, string $titulo = null, string $descricao = null, string $genero = null, string $ano = null, string $avaliacao=null, string $diretor=null, string $elenco=null) {
		$tablePath = $this->tablePathFilme;
		$houveAlteracao = false;
		$Filme = $this->selectFilme($codigo);
		if($Filme == null) {
			return "ERRO: Não há Filme com o código ".$codigo.".";
		}
		$Filme = $Filme[0];
		if(($Filme->codigo != $codigo) or ($codigo == null)) {
			return "ERRO: código invalido.";
		}
		if(($Filme->titulo != $titulo) and ($titulo != null)) {
			if($this->verificaTituloFilme($titulo)){
				return "ERRO: Este Título já está cadastrado em outro filme.";
			}
			$Filme->titulo = $titulo;
			$houveAlteracao = true;
		}
		if(($Filme->descricao != $descricao) and ($descricao != null)) {
			$Filme->descricao = $descricao;
			$houveAlteracao = true;
		}
		if(($Filme->genero != $genero) and ($genero != null)) {
			$Filme->genero = $genero;
			$houveAlteracao = true;
		}
		if(($Filme->ano != $ano) and ($ano != null)) {
			$Filme->ano = $ano;
			$houveAlteracao = true;
		}
		if(($Filme->avaliacao != $avaliacao) and ($avaliacao != null)) {
			if($this->validaFormatoAvaliacao($avaliacao)){
				return "ERRO: Campo 'Avaliação' deve ser preenchido com valores entre 0.0 e 5.0 (podendo ser 0.0 ou 5.0 também)";
			}
			$Filme->avaliacao = $avaliacao;
			$houveAlteracao = true;
		}
		if(($Filme->diretor != $diretor) and ($diretor != null)) {
			$Filme->diretor = $diretor;
			$houveAlteracao = true;
		}
		if(($Filme->elenco != $elenco) and ($elenco != null)) {
			$Filme->elenco = $elenco;
			$houveAlteracao = true;
		}
		if(!$houveAlteracao) {
			return "Não houve alteração.";
		}
		$domFilme = dom_import_simplexml($Filme);
		$domXML = $domFilme->parentNode->parentNode; //  documento XML
		// Salva as alterações na tabela
		if($domXML->save($tablePath)) {
			return "Alteração efetuada com sucesso!";
		}else {
			return "ERRO: Ao salvar modificações na tabela ".$tablePath.".";
		}
	}


	/**
	 * insertFilme
	 *
	 * Salva na tabela tFilme.xml os atributos do objeto desta classe
	 * 
	 * @param void
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function insertFilme() {
		$msgRetorno = $this->insertFilmeCompleto($this->titulo, 
                                                 $this->descricao, 
                                                 $this->genero,
                                                 $this->ano,
                                                 $this->avaliacao,
                                                 $this->diretor,
                                                 $this->elenco
		);
		if (strcmp($msgRetorno, "Inserção realizada com sucesso!") != 0) {
			return $msgRetorno; // Significa que deu erro.
		}
		$this->codigo = $this->getCodigoByFilme($this);
		$this->reg_date = $this->getFilmeByCodigo($this->codigo)[0]->reg_date;
		return $msgRetorno;
	}


	/**
	 * updateFilme
	 *
	 * Salva na tabela tFilme.xml as alteracoes presentes nos atributos do objeto desta classe.
	 * 
	 * @param void
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function updateFilme() {
		if($this->codigo == null) {
			return "ERRO: Código do Filme invalido.";
		}
		return $this->updateFilmeCompleto($this->codigo,
			                              $this->titulo, 
		                                  $this->descricao, 
		                                  $this->genero,
		                                  $this->ano,
		                                  $this->avaliacao,
		                                  $this->diretor,
		                                  $this->elenco
		);
	}


	/**
	 * clearFilme
	 *
	 * Limpa os atributos do objeto da classe lFilme.
	 * 
	 * @param void
	 * @return void
	 * 
	 */
	public function clearFilme() {
		$this->codigo = null;
		$this->titulo = null;
		$this->descricao = null;
		$this->genero = null;
		$this->ano = null;
		$this->diretor = null;
		$this->elenco = null;
		$this->reg_date = null;
	}


	/**
	 * getFilmeByCodigo
	 * 
	 * Busca os Filmes que possuem o codigo informado e retorna um array com objetos da classe lFilme.
	 * 
	 * @param string $codigo          Codigo do Filme a ser buscado.
	 * @return lFilme[] $ListaFilme   Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByCodigo(string $codigo) {
		$ListSimpleXMLObject = $this->selectFilme($codigo);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}


	/**
	 * getFilmeByTitulo
	 *
	 * Busca os Filmes que possuem o titulo informado e retorna um array com objetos da classe lFilme.
	 * 
	 * @param string $titulo         Nome do Filme a ser buscado.
	 * @return lFilme[] $ListaFilme  Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByTitulo(string $titulo) {
		$ListSimpleXMLObject = $this->selectFilme(null, $titulo);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}


	/**
	 * getFilmeByDescricao
	 * 
	 * Busca os Filmes que possuem a descricao informado e retorna um array com objetos da classe lFilme.
	 * 
	 * @param string $descricao     Descrição do Filme a ser buscado.
	 * @return lFilme[] $ListaFilme Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByDescricao(string $descricao) {
		$ListSimpleXMLObject = $this->selectFilme(null, null, $descricao);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}


	/**
	 * getFilmeByGenero
	 *
	 * Busca os Filmes que possuem o genero informado e retorna um array com objetos da classe lFilme.
	 * 
	 * @param string $genero        Gênero do Filme a ser buscado.
	 * @return lFilme[] $ListaFilme Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByGenero($genero) {
		$ListSimpleXMLObject = $this->selectFilme(null, null, null, $genero);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}


	/**
	 * getFilmeByAno
	 *
	 * Busca os Filmes que possuem o ano informado e retorna um array com objetos da classe lFilme.
	 * 
	 * @param string $endereco				Endereco do Filme a ser buscado.
	 * @return lFilme[] $ListaFilme	Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByAno($ano) {
		$ListSimpleXMLObject = $this->selectFilme(null, null, null, null, $ano);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}


	/**
	 * getFilmeByAvaliacao
	 *
	 * Busca os Filmes que possuem avaliacao entre as avaliações máxima e mínima informadas e retorna um array com objetos da classe lFilme.
	 * 
	 * @param float $avaliacaoMinima         Avaliação mínima desejada
	 * @param float $avaliacaoMaxima         Avaliação máxima desejada
	 * @return lFilme[] $ListaFilme          Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByAvaliacao($avaliacaoMinima, $avaliacaoMaxima) {
		$ListSimpleXMLObject = $this->selectFilme(null, null, null, null, null, $avaliacaoMinima, $avaliacaoMaxima);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}


	/**
	 * getFilmeByDiretor
	 *
	 * Busca os Filmes que possuem o diretor informado e retorna um array com objetos da classe lFilme.
	 * 
	 * @param string $diretor        Diretor do Filme a ser buscado.
	 * @return lFilme[] $ListaFilme	 Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByDiretor($diretor) {
		$ListSimpleXMLObject = $this->selectFilme(null, null, null, null, null, null,null, $diretor);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}


	/**
	 * getFilmeByElenco
	 *
	 * Busca os Filmes que possuem o elenco informado e retorna um array com objetos da classe lFilme.
	 * 
	 * @param string $elenco         Elenco do Filme a ser buscado.
	 * @return lFilme[] $ListaFilme	 Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByElenco($elenco) {
		$ListSimpleXMLObject = $this->selectFilme(null, null, null, null, null, null, null, null, $elenco);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}

	/**
	 * getFilmeByRegDate
	 *
	 * Busca os Filmes que foram inseridos no sistema na data informada e retorna um array com objetos da classe lFilme.
	 * 
	 * @param string $reg_date       Data de cadastro no sistema do Filme a ser buscado.
	 * @return lFilme[] $ListaFilme	 Retorna um array de objetos da classe lFilme.
	 * 
	 */
	public function getFilmeByRegDate($reg_date) {
		$ListSimpleXMLObject = $this->selectFilme(null, null, null, null, null, null, null, null, null, $reg_date);
		$ListaFilmes = $this->traduzSimpleXMLObjectToFilme($ListSimpleXMLObject);
		return $ListaFilmes;
	}


	/**
	 * getCodigoByFilme
	 *
	 * Busca o Filme informado e retorna uma string com o seu codigo.
	 * 
	 * @param lFilme $oFilme      Objeto da classe lFilme.
	 * @return string $codigo     Retorna o Codigo do Filme (em string).
	 * 
	 */
	public function getCodigoByFilme(lFilme $oFilme) {
		$temp = $this->selectFilme (null,
									$oFilme->titulo,
									$oFilme->descricao,
									$oFilme->genero,
									$oFilme->ano,
									$oFilme->avaliacao,
									$oFilme->avaliacao,
									$oFilme->diretor,
									$oFilme->elenco
								    );
		$codigo = (string) $temp[0]->codigo;
		return $codigo;
	}

	/**
	 * getTabela
	 *
	 * Retorna toda a tabela tFilme em um array de objetos da classe lFilme.
	 * 
	 * @param void
	 * @return array lFilme       Array de objetos da classe lFilme
	 * 
	 */
	public function getTabela(){
		return $this->traduzSimpleXMLObjectToFilme($this->selectFilme());
	}
}

?>
