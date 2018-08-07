<?php
namespace TicketNow;
/**
 * ..:: Logica para manipulacao da tabela tFilmeSubscriber.xml ::..
 * 
 * @author Marlon R C Franco
 * @author Marlon R C Franco <marlonrcfranco@gmail.com>
 * 
 */

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
date_default_timezone_set('America/Sao_Paulo');

include_once 'lFilme.php';
include_once 'lSubscriber.php';
include_once 'ILogica.php';

/**
 * Classe de lógica para manipulação da tabela tFilmeSubscriber.xml
 * 
 * @var string $codigo          Codigo de identificacao no padrão "X0000"
 * @var string $titulo          [FK] Titulo do Filme
 * @var string $email           [FK] Email do Subscriber
 * @var string $reg_date        Data e Hora de registro do FilmeSubscriber do sistema
 * @var boolean $semEspaco      Flag para verificacao se ha espaco vazio no meio da tabela tFilmeSubscriber.xml
 * @var string $tablePathFilmeSubscriber  File path da tabela tFilmeSubscriber.xml
 * 
 */
class lFilmeSubscriber implements ILogica{
	public $codigo;
	public $titulo;
	public $email;
	public $reg_date;
	private $semEspaco;
	private $tablePathFilmeSubscriber;
	
/**
 * __construct
 * 
 * Construtor da classe lFilmeSubscriber, que inicializa os parametros como null.
 * 
 * @param void
 * @return void
 * 
 */
	function __construct() {
        $this->codigo = null;
		$this->titulo = null;
		$this->email = null;
		$this->reg_date = null;
		$this->semEspaco = false;
		// $this->tablePathFilmeSubscriber = $_SERVER['DOCUMENT_ROOT']."/Database/db/tFilmeSubscriber.xml";
		$this->tablePathFilmeSubscriber = "0.SistemaWeb/Database/db/tFilmeSubscriber.xml";
    }
	

	/**
	 * createTableFilmeSubscriber
	 *
	 * Metodo para a criacao da tabela FilmeSubscriber.xml, contendo o primeiro registro como Template.
	 * 
	 * @param void
	 * @return int 1|0	Retorna 1 se houve ERRO, ou 0 se a criacao da tabela foi realizada com sucesso.
	 * 
	 */
	public function createTableFilmeSubscriber() {
		$filePathFilmeSubscriber = $this->tablePathFilmeSubscriber;
		$file = fopen($filePathFilmeSubscriber, "w+");
	$template = <<<XML
<?xml version="1.0" encoding="UTF-8"?>
<root>
	<filmesubscriber>
		<codigo>X0000</codigo>
		<titulo>Template, o Filme</titulo>
		<email>template.junior@gmail.com</email>
		<reg_date>1993-05-31 23:59:59</reg_date>
  </filmesubscriber>
</root>
XML;
		fwrite($file,$template);
		fclose($file);
		$file = fopen($filePathFilmeSubscriber, "r");
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
			$codAnterior = ("X".sprintf('%04d', $i)); $i++;
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
	 * traduzSimpleXMLObjectToFilmeSubscriber
	 *
	 * Traduz um array de SimpleXMLObject em um array de lFilmeSubscriber
	 * 
	 * @param SimpleXMLElement[] $ListSimpleXMLObject         Array de SimpleXMLObject.
	 * @return lFilmeSubscriber[] $ListaFilmeSubscriber       Array de lFilmeSubscriber.
	 * 
	 */
	public function traduzSimpleXMLObjectToFilmeSubscriber($ListSimpleXMLObject) {
		$ListaFilmeSubscriber = array();
		foreach($ListSimpleXMLObject as $SimpleXMLObject) {
			$oFilmeSubscriber = new lFilmeSubscriber();
			$oFilmeSubscriber->codigo    = (string)$SimpleXMLObject->codigo;
			$oFilmeSubscriber->titulo    = (string)$SimpleXMLObject->titulo;
			$oFilmeSubscriber->email     = (string)$SimpleXMLObject->email;
			$oFilmeSubscriber->reg_date  = (string)$SimpleXMLObject->reg_date;
			array_push($ListaFilmeSubscriber, $oFilmeSubscriber);
		}
		return $ListaFilmeSubscriber;
	}


	/**
	 * verificaTituloFilme
	 *
	 * Verifica se o Titulo informado está presente na tabela tFilme.xml
	 * 
	 * @param string $titulo    Titulo do Filme a ser verificado.
	 * @return false|true       'false' se Sucesso | 'true' se ERRO: FilmeSubscriber já cadastrado.
	 * 
	 */
	public function verificaTituloFilme(string $titulo) {
		$oFilme = new lFilme();
		if ($oFilme->getFilmeByTitulo($titulo) == null) {
			return true; // ERRO: Filme com este Titulo não foi encontrado na tabela tFilme.xml.
		}
		return false; // Sucesso
	}


	/**	
	 * verificaEmailSubscriber
	 *
	 * Verifica se o Email informado está presente na tabela tSubscriber.xml
	 * 
	 * @param string $email     Email do Subscriber a ser verificado.
	 * @return false|true       'false' se Sucesso | 'true' se ERRO: FilmeSubscriber já cadastrado.
	 * 
	 */
	public function verificaEmailSubscriber(string $email) {
		$oSubscriber = new lSubscriber();
		if ($oSubscriber->getSubscriberByEmail($email) == null) {
			return true; // ERRO: Subscriber com este Email não foi encontrado na tabela tSubscriber.xml.
		}
		return false; // Sucesso
	}


	/**
	 * insertFilmeSubscriberCompleto
	 *
	 * Salva na tabela tFilmeSubscriber.xml o novo registro relacionando as tabelas tFilme.xml e tSubscriber.xml.
	 * 
	 * @param string $nome            Nome do FilmeSubscriber
 	 * @param string $cpf             CPF do FilmeSubscriber
     * @param string $email           Email do FilmeSubscriber
     * @param string $telefone        Telefone do FilmeSubscriber
     * @param string $endereco        Endereco do FilmeSubscriber
     * @param string $bairro          Bairro do FilmeSubscriber
     * @param string $cep             CEP do FilmeSubscriber
	 * 
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function insertFilmeSubscriberCompleto(string $titulo, string $email) {
		$tablePath  = $this->tablePathFilmeSubscriber;
		$domXML = new DOMDocument('1.0');
		$domXML->preserveWhiteSpace = false;
		$domXML->formatOutput = true;
		if ($domXML->load($tablePath)) {
			//echo "segue o baile\n";
		}else {
			echo "\n\nTabela '". $tablePath."' não encontrada.\nCriando tabela...\n\n";
			if( createTableFilmeSubscriber($tablePath) ) exit("ERRO ao criar a tabela");
			else echo "\nTabela '".$tablePath. "' criada com sucesso!\n";
			$domXML->load($tablePath);
		}
		$root = $domXML->getElementsByTagName('root')->item(0);
		// busca o primeiro espaco vazio
		$domPosition = lFilmeSubscriber::buscaEspacoVazio($domXML);
		//Extrai o codigo e transforma em int
		$strCodigo = $domPosition->firstChild->nodeValue;
		$strCodigo = substr($strCodigo, 1);
		$intCodigo = intval($strCodigo);
		$FilmeSubscriber = $domXML->createElement('filmesubscriber');
		if ($this->semEspaco) {
			$codigo = "X".sprintf('%04d', $intCodigo + 1);
			$root->appendChild($FilmeSubscriber);
		}else {
			$codigo = "X".sprintf('%04d', $intCodigo - 1);
			$root->insertBefore($FilmeSubscriber, $domPosition);
		}
		// ******* Inserção do Código *******
		$codigoElement = $domXML->createElement("codigo", $codigo);
		$FilmeSubscriber->appendChild($codigoElement);
		// ******* Inserção do Titulo *******
		if($titulo == null) {
			return "ERRO: Campo 'Titulo' é obrigatório.";
		}

		$tituloElement = $domXML->createElement("titulo", $titulo);
		$FilmeSubscriber->appendChild($tituloElement);
		// ******* Inserção da Email *******
		if($email == null) {
			return "ERRO: Campo 'Email' é obrigatório.";
		}

		$emailElement = $domXML->createElement("email", $email);
		$FilmeSubscriber->appendChild($emailElement);
		// ******* Inserção da Data de Registro no Sitema *******
		$reg_dateElement = $domXML->createElement("reg_date", date("Y-m-d H:i:s",time()));
		$FilmeSubscriber->appendChild($reg_dateElement);
		if($domXML->save($tablePath)) {
			return "Inserção realizada com sucesso!";
		}else {
			return "Erro ao inserir registro de FilmeSubscriber.";
		}
	}


	/**
	 * selectFilmeSubscriber
	 *
	 * Seleciona na tabela tFilmeSubscriber.xml todos os registros que possuem os campos informados, retornando um array do tipo SimpleXMLElement.
	 *
	 * @param string $codigo          [Opcional] Codigo de identificacao no padrão "X0000"
	 * @param string $titulo          [Opcional] Titulo do Filme
     * @param string $email           [Opcional] Email do Subscriber
	 * @param string $reg_date        [Opcional] Data e Hora de registro do FilmeSubscriber do sistema
	 * 
	 * @return SimpleXMLElement[] $xml	Retorna um array de SimpleXMLObject contendo o resultado da consulta.
	 * 
	 */
	public function selectFilmeSubscriber(string $codigo = null, string $titulo = null, string $email = null, string $reg_date = null) {
		$tablePath = $this->tablePathFilmeSubscriber;
		$xml=simplexml_load_file($tablePath) or die("Error: Cannot create object");
		$maisDeUmParametro = false;
		if (($codigo == null) && 
			($titulo == null) &&
			($email == null) &&
			($reg_date == null))
		{
			$xPathQuery = "filmesubscriber";
			return $xml->xpath($xPathQuery); // Retorna um Array de SimpleXML Object, contendo os resultados 
		}
		$xPathQuery = "filmesubscriber[";
		if ($codigo != null) {
			$xPathQuery = $xPathQuery."codigo/text()='".$codigo."'";
			$maisDeUmParametro = true;
		} 
		if ($titulo != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."titulo/text()='".$titulo."'";
			$maisDeUmParametro = true;
		}
		if ($email != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."email/text()='".$email."'";
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
	 * excluirFilmeSubscriber
	 *
	 * Exclui da tabela tFilmeSubscriber.xml o registro que possui o codigo informado.
	 * 
	 * @param string $codigo             Codigo do registro a ser excluido da tabela, no padrão "X0000"
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function excluirFilmeSubscriber(string $codigo) {
		$tablePath = $this->tablePathFilmeSubscriber;
		$FilmeSubscriber = $this->selectFilmeSubscriber($codigo);
		if($FilmeSubscriber == null) {
			return "ERRO: Não há registro com o código ".$codigo.".";
		}
		$domFilmeSubscriber = dom_import_simplexml($FilmeSubscriber[0]);
		$domXML = $domFilmeSubscriber->parentNode->parentNode; //  documento XML
		$domFilmeSubscriber->parentNode->removeChild($domFilmeSubscriber);
		if($domXML->save($tablePath)) {
			return "Exclusão efetuada com sucesso!";
		}else {
			return "ERRO: Ao salvar modificações na tabela ".$tablePath.".";
		}
	}


	/**
	 * updateFilmeSubscriberCompleto
	 *
	 * Salva na tabela tFilmeSubscriber.xml as alteracoes no registro que possui o codigo informado.
	 * 
	 * @param string $codigo          Codigo de identificacao no padrão "X0000"
	 * @param string $titulo          [Opcional] Titulo do Filme
     * @param string $email           [Opcional] Email do Subscriber
	 * 
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function updateFilmeSubscriberCompleto(string $codigo, string $titulo = null, string $email = null) {
		$tablePath = $this->tablePathFilmeSubscriber;
		$houveAlteracao = false;
		$FilmeSubscriber = $this->selectFilmeSubscriber($codigo);
		if($FilmeSubscriber == null) {
			return "ERRO: Não há registro com o código ".$codigo.".";
		}
		$FilmeSubscriber = $FilmeSubscriber[0];
		if(($FilmeSubscriber->codigo != $codigo) or ($codigo == null)) {
			return "ERRO: código invalido.";
		}
		if(($FilmeSubscriber->titulo != $titulo) and ($titulo != null)) {
			if($this->verificaTituloFilme($titulo)){
				return "ERRO: Não há Filme cadastrado com este Título.";
			}
			$FilmeSubscriber->titulo = $titulo;
			$houveAlteracao = true;
		}
		if(($FilmeSubscriber->email != $email) and ($email != null)) {
			if($this->verificaEmailSubscriber($email)){
				return "ERRO: Não há Subscriber cadastrado com este email.";
			}
			$FilmeSubscriber->email = $email;
			$houveAlteracao = true;
		}
		if(!$houveAlteracao) {
			return "Não houve alteração.";
		}
		$domFilmeSubscriber = dom_import_simplexml($FilmeSubscriber);
		$domXML = $domFilmeSubscriber->parentNode->parentNode; //  documento XML
		// Salva as alterações na tabela
		if($domXML->save($tablePath)) {
			return "Alteração efetuada com sucesso!";
		}else {
			return "ERRO: Ao salvar modificações na tabela ".$tablePath.".";
		}
	}


	/**
	 * insertFilmeSubscriber
	 *
	 * Salva na tabela tFilmeSubscriber.xml os atributos do objeto desta classe
	 * 
	 * @param void
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function insertFilmeSubscriber() {
		$msgRetorno = $this->insertFilmeSubscriberCompleto($this->titulo, $this->email);
		if (strcmp($msgRetorno, "Inserção realizada com sucesso!") != 0) {
			return $msgRetorno; // Significa que deu erro.
		}
		$this->codigo = $this->getCodigoByFilmeSubscriber($this);
		$this->reg_date = $this->getFilmeSubscriberByCodigo($this->codigo)[0]->reg_date;
		return $msgRetorno;
	}


	/**
	 * updateFilmeSubscriber
	 *
	 * Salva na tabela tFilmeSubscriber.xml as alteracoes presentes nos atributos do objeto desta classe.
	 * 
	 * @param void
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function updateFilmeSubscriber() {
		if($this->codigo == null) {
			return "ERRO: Código do registro invalido.";
		}
		return $this->updateFilmeSubscriberCompleto($this->codigo, $this->titulo, $this->email);
	}


	/**
	 * clearFilmeSubscriber
	 *
	 * Limpa os atributos do objeto da classe lFilmeSubscriber.
	 * 
	 * @param void
	 * @return void
	 * 
	 */
	public function clearFilmeSubscriber() {
		$this->codigo = null;
		$this->titulo = null;
		$this->email = null;
		$this->reg_date = null;
	}


	/**
	 * getFilmeSubscriberByCodigo
	 * 
	 * Busca os registro que possuem o Codigo informado e retorna um array com objetos da classe lFilmeSubscriber.
	 * 
	 * @param string $codigo                              Codigo do registro a ser buscado.
	 * @return lFilmeSubscriber[] $ListaFilmeSubscriber   Retorna um array de objetos da classe lFilmeSubscriber.
	 * 
	 */
	public function getFilmeSubscriberByCodigo(string $codigo) {
		$ListSimpleXMLObject = $this->selectFilmeSubscriber($codigo);
		$ListaFilmeSubscribers = $this->traduzSimpleXMLObjectToFilmeSubscriber($ListSimpleXMLObject);
		return $ListaFilmeSubscribers;
	}


	/**
	 * getFilmeSubscriberByTitulo
	 *
	 * Busca os registros que possuem o Titulo informado e retorna um array com objetos da classe lFilmeSubscriber.
	 * 
	 * @param string $titulo                             Titulo do Filme no registro a ser buscado.
	 * @return lFilmeSubscriber[] $ListaFilmeSubscriber  Retorna um array de objetos da classe lFilmeSubscriber.
	 * 
	 */
	public function getFilmeSubscriberByTitulo(string $titulo) {
		$ListSimpleXMLObject = $this->selectFilmeSubscriber(null, $titulo);
		$ListaFilmeSubscribers = $this->traduzSimpleXMLObjectToFilmeSubscriber($ListSimpleXMLObject);
		return $ListaFilmeSubscribers;
	}


	/**
	 * getFilmeSubscriberByEmail
	 *
	 * Busca os registros que possuem o Email informado e retorna um array com objetos da classe lFilmeSubscriber.
	 * 
	 * @param string $email                             Email do Subscriber no registro a ser buscado.
	 * @return lFilmeSubscriber[] $ListaFilmeSubscriber Retorna um array de objetos da classe lFilmeSubscriber.
	 * 
	 */
	public function getFilmeSubscriberByEmail($email) {
		$ListSimpleXMLObject = $this->selectFilmeSubscriber(null, null, $email);
		$ListaFilmeSubscribers = $this->traduzSimpleXMLObjectToFilmeSubscriber($ListSimpleXMLObject);
		return $ListaFilmeSubscribers;
	}


	/**
	 * getFilmeSubscriberByRegDate
	 *
	 * Busca os registros que foram inseridos no sistema na data informada e retorna um array com objetos da classe lFilmeSubscriber.
	 * 
	 * @param string $reg_date                           Data de cadastro no sistema do registro a ser buscado.
	 * @return lFilmeSubscriber[] $ListaFilmeSubscriber	 Retorna um array de objetos da classe lFilmeSubscriber.
	 * 
	 */
	public function getFilmeSubscriberByRegDate($reg_date) {
		$ListSimpleXMLObject = $this->selectFilmeSubscriber(null, null, null, $reg_date);
		$ListaFilmeSubscribers = $this->traduzSimpleXMLObjectToFilmeSubscriber($ListSimpleXMLObject);
		return $ListaFilmeSubscribers;
	}


	/**
	 * getCodigoByFilmeSubscriber
	 *
	 * Busca o registro informado e retorna uma string com o seu codigo.
	 * 
	 * @param lFilmeSubscriber $oFilmeSubscriber   Objeto da classe lFilmeSubscriber.
	 * @return string $codigo                      Retorna o Codigo do FilmeSubscriber (em string).
	 * 
	 */
	public function getCodigoByFilmeSubscriber(lFilmeSubscriber $oFilmeSubscriber) {
		$temp = $this->selectFilmeSubscriber(null, $oFilmeSubscriber->titulo, $oFilmeSubscriber->email);
		$codigo = (string) $temp[0]->codigo;
		return $codigo;
	}

	/**
	 * getTabela
	 *
	 * Retorna toda a tabela tFilmeSubscriber em um array de objetos da classe lFilmeSubscriber.
	 * 
	 * @param void
	 * @return array lFilmeSubscriber       Array de objetos da classe lFilmeSubscriber
	 * 
	 */
	public function getTabela(){
		return $this->traduzSimpleXMLObjectToFilmeSubscriber($this->selectFilmeSubscriber());
	}
}

?>
