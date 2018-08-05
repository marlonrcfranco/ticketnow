<?php

/**
 * ..:: Logica para manipulacao da tabela tSubscriber.xml ::..
 * 
 * @author Marlon R C Franco
 * @author Marlon R C Franco <marlonrcfranco@gmail.com>
 * 
 */

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
date_default_timezone_set('America/Sao_Paulo');

/**
 * Classe de lógica para manipulação da tabela tSubscriber.xml
 * 
 * @var string $codigo          Codigo de identificacao no padrão "S0000"
 * @var string $nome            Nome do Subscriber
 * @var string $cpf             CPF do Subscriber
 * @var string $email           Email do Subscriber
 * @var string $telefone        Telefone do Subscriber
 * @var string $endereco        Endereco do Subscriber
 * @var string $bairro          Bairro do Subscriber
 * @var string $cep             CEP do Subscriber
 * @var string $reg_date        Data e Hora de registro do Subscriber do sistema
 * @var boolean $semEspaco      Flag para verificacao se ha espaco vazio no meio da tabela tSubscriber.xml
 * @var string $tablePathSubscriber  File path da tabela tSubscriber.xml
 * 
 */
class lSubscriber {
	public $codigo;
	public $nome;
	public $cpf;
	public $email;
	public $telefone;
	public $endereco;
	public $bairro;
	public $cep;
	public $reg_date;
	private $semEspaco;
	private $tablePathSubscriber;
	
/**
 * __construct
 * 
 * Construtor da classe lSubscriber, que inicializa os parametros como null.
 * 
 * @param void
 * @return void
 * 
 */
	function __construct() {
        $this->codigo = null;
		$this->nome = null;
		$this->cpf = null;
		$this->email = null;
		$this->telefone = null;
		$this->endereco = null;
		$this->bairro = null;
		$this->cep = null;
		$this->reg_date = null;
		$this->semEspaco = false;
		// $this->tablePathSubscriber = $_SERVER['DOCUMENT_ROOT']."/Database/db/tSubscriber.xml";
		$this->tablePathSubscriber = "0.SistemaWeb/Database/db/tSubscriber.xml";
    }
	

	/**
	 * createTableSubscriber
	 *
	 * Metodo para a criacao da tabela Subscriber.xml, contendo o primeiro registro como Template.
	 * 
	 * @param void
	 * @return int 1|0	Retorna 1 se houve ERRO, ou 0 se a criacao da tabela foi realizada com sucesso.
	 * 
	 */
	public function createTableSubscriber() {
		$filePathSubscriber = $this->tablePathSubscriber;
		$file = fopen($filePathSubscriber, "w+");
	$template = <<<XML
<?xml version="1.0" encoding="UTF-8"?>
<root>
	<subscriber>
		<codigo>S0000</codigo>
		<nome>Template Junior</nome>
		<cpf>777.777.777-77</cpf>
		<email>template.junior@gmail.com</email>
		<telefone>(sem informações de telefone)</telefone>
		<endereco>(sem informações de endereço)</endereco>
		<bairro>(sem informações de bairro)</bairro>
		<cep>(sem informações de CEP)</cep>
		<reg_date>1993-05-31 23:59:59</reg_date>
  </subscriber>
</root>
XML;
		fwrite($file,$template);
		fclose($file);
		$file = fopen($filePathSubscriber, "r");
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
			$codAnterior = ("S".sprintf('%04d', $i)); $i++;
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
	 * traduzSimpleXMLObjectToSubscriber
	 *
	 * Traduz um array de SimpleXMLObject em um array de lSubscriber
	 * 
	 * @param SimpleXMLElement[] $ListSimpleXMLObject   Array de SimpleXMLObject.
	 * @return lSubscriber[] $ListaSubscriber                     Array de lSubscriber.
	 * 
	 */
	public function traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject) {
		$ListaSubscriber = array();
		foreach($ListSimpleXMLObject as $SimpleXMLObject) {
			$oSubscriber = new lSubscriber();
			$oSubscriber->codigo    = (string)$SimpleXMLObject->codigo;
			$oSubscriber->nome      = (string)$SimpleXMLObject->nome;
			$oSubscriber->cpf       = (string)$SimpleXMLObject->cpf;
			$oSubscriber->email     = (string)$SimpleXMLObject->email;
			$oSubscriber->telefone  = (string)$SimpleXMLObject->telefone;
			$oSubscriber->endereco  = (string)$SimpleXMLObject->endereco;
			$oSubscriber->bairro    = (string)$SimpleXMLObject->bairro;
			$oSubscriber->cep       = (string)$SimpleXMLObject->cep;
			$oSubscriber->reg_date  = (string)$SimpleXMLObject->reg_date;
			array_push($ListaSubscriber, $oSubscriber);
		}
		return $ListaSubscriber;
	}


	/**
	 * verificaCPFSubscriber
	 *
	 * Verifica se o CPF informado já está presente na tabela tSubscriber.xml
	 * 
	 * @param string $cpf       CPF do Subscriber a ser verificado.
	 * @return false|true       'false' se Sucesso | 'true' se ERRO: Subscriber já cadastrado.
	 * 
	 */
	public function verificaCPFSubscriber(string $cpf) {
		if ($this->getSubscriberByCPF($cpf) == null) {
			return false; // Sucesso
		}
		return true; // ERRO: Subscriber já cadastrado.
	}


	/**	
	 * verificaEmailSubscriber
	 *
	 * Verifica se o Email informado já está presente na tabela tSubscriber.xml
	 * 
	 * @param string $email     Email do Subscriber a ser verificado.
	 * @return false|true       'false' se Sucesso | 'true' se ERRO: Subscriber já cadastrado.
	 * 
	 */
	public function verificaEmailSubscriber(string $email) {
		if ($this->getSubscriberByEmail($email) == null) {
			return false; // Sucesso
		}
		return true; // ERRO: Subscriber já cadastrado.
	}


	/**
	 * insertSubscriberCompleto
	 *
	 * Salva na tabela tSubscriber.xml o novo Subscriber que possui os parametros informados.
	 * 
	 * @param string $nome            Nome do Subscriber
 	 * @param string $cpf             CPF do Subscriber
     * @param string $email           Email do Subscriber
     * @param string $telefone        Telefone do Subscriber
     * @param string $endereco        Endereco do Subscriber
     * @param string $bairro          Bairro do Subscriber
     * @param string $cep             CEP do Subscriber
	 * 
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function insertSubscriberCompleto(string $nome, string $cpf, string $email, string $telefone = null, string $endereco = null, string $bairro = null, string $cep = null) {
		$tablePath  = $this->tablePathSubscriber;
		$domXML = new DOMDocument('1.0');
		$domXML->preserveWhiteSpace = false;
		$domXML->formatOutput = true;
		if ($domXML->load($tablePath)) {
			//echo "segue o baile\n";
		}else {
			echo "\n\nTabela '". $tablePath."' não encontrada.\nCriando tabela...\n\n";
			if( createTableSubscriber($tablePath) ) exit("ERRO ao criar a tabela");
			else echo "\nTabela '".$tablePath. "' criada com sucesso!\n";
			$domXML->load($tablePath);
		}
		$root = $domXML->getElementsByTagName('root')->item(0);
		// busca o primeiro espaco vazio
		$domPosition = lSubscriber::buscaEspacoVazio($domXML);
		//Extrai o codigo e transforma em int
		$strCodigo = $domPosition->firstChild->nodeValue;
		$strCodigo = substr($strCodigo, 1);
		$intCodigo = intval($strCodigo);
		$Subscriber = $domXML->createElement('subscriber');
		if ($this->semEspaco) {
			$codigo = "S".sprintf('%04d', $intCodigo + 1);
			$root->appendChild($Subscriber);
		}else {
			$codigo = "S".sprintf('%04d', $intCodigo - 1);
			$root->insertBefore($Subscriber, $domPosition);
		}
		// ******* Inserção do Código *******
		$codigoElement = $domXML->createElement("codigo", $codigo);
		$Subscriber->appendChild($codigoElement);
		// ******* Inserção do Nome *******
		if($nome == null) {
			return "ERRO: Campo 'Nome' é obrigatório.";
		}
		$nomeElement = $domXML->createElement("nome", $nome);
		$Subscriber->appendChild($nomeElement);
		// ******* Inserção da CPF *******
		if($cpf == null) {
			return "ERRO: Campo 'CPF' é obrigatório.";
		}
		if($this->verificaCPFSubscriber($cpf)){
			return "ERRO: Este CPF já está cadastrado.";
		}
		$cpfElement = $domXML->createElement("cpf", $cpf);
		$Subscriber->appendChild($cpfElement);
		// ******* Inserção da Email *******
		if($email == null) {
			return "ERRO: Campo 'Email' é obrigatório.";
		}
		if($this->verificaEmailSubscriber($email)){
			return "ERRO: Este Email já está cadastrado.";
		}
		$emailElement = $domXML->createElement("email", $email);
		$Subscriber->appendChild($emailElement);
		// ******* Inserção da Telefone *******
		if($telefone == null) {
			$telefone = "(sem informações de telefone)";
		}
		$telefoneElement = $domXML->createElement("telefone", $telefone);
		$Subscriber->appendChild($telefoneElement);
		// ******* Inserção da Endereço *******
		if($endereco == null) {
			$endereco = "(sem informações de endereço)";
		}
		$enderecoElement = $domXML->createElement("endereco", $endereco);
		$Subscriber->appendChild($enderecoElement);
		// ******* Inserção da Bairro *******
		if($bairro == null) {
			$bairro = "(sem informações de bairro)";
		}
		$bairroElement = $domXML->createElement("bairro", $bairro);
		$Subscriber->appendChild($bairroElement);
		// ******* Inserção da CEP *******
		if($cep == null) {
			$cep = "(sem informações de CEP)";
		}
		$cepElement = $domXML->createElement("cep", $cep);
		$Subscriber->appendChild($cepElement);
		// ******* Inserção da Data de Registro no Sitema *******
		$reg_dateElement = $domXML->createElement("reg_date", date("Y-m-d H:i:s",time()));
		$Subscriber->appendChild($reg_dateElement);
		if($domXML->save($tablePath)) {
			return "Inserção realizada com sucesso!";
		}else {
			return "Erro ao inserir registro de Subscriber.";
		}
	}


	/**
	 * selectSubscriber
	 *
	 * Seleciona na tabela tSubscriber.xml todos os Subscribers que possuem os campos informados, retornando um array do tipo SimpleXMLElement.
	 *
	 * @param string $codigo          [Opcional] Codigo de identificacao no padrão "S0000"
	 * @param string $nome            [Opcional] Nome do Subscriber
 	 * @param string $cpf             [Opcional] CPF do Subscriber
     * @param string $email           [Opcional] Email do Subscriber
     * @param string $telefone        [Opcional] Telefone do Subscriber
     * @param string $endereco        [Opcional] Endereco do Subscriber
     * @param string $bairro          [Opcional] Bairro do Subscriber
     * @param string $cep             [Opcional] CEP do Subscriber
	 * @param string $reg_date        [Opcional] Data e Hora de registro do Subscriber do sistema
	 * 
	 * @return SimpleXMLElement[] $xml	Retorna um array de SimpleXMLObject contendo o resultado da consulta.
	 * 
	 */
	public function selectSubscriber(string $codigo = null, string $nome = null, string $cpf = null, string $email = null, string $telefone = null, string $endereco = null, string $bairro = null, string $cep = null, string $reg_date = null) {
		$tablePath = $this->tablePathSubscriber;
		$xml=simplexml_load_file($tablePath) or die("Error: Cannot create object");
		$maisDeUmParametro = false;
		if (($codigo == null) && 
			($nome == null) &&
			($cpf == null) &&
			($email == null) &&
			($telefone == null) &&
			($endereco == null) &&
			($bairro == null) &&
			($cep == null) &&
			($reg_date == null))
		{
			$xPathQuery = "subscriber";
			return $xml->xpath($xPathQuery); // Retorna um Array de SimpleXML Object, contendo os resultados 
		}
		$xPathQuery = "subscriber[";
		if ($codigo != null) {
			$xPathQuery = $xPathQuery."codigo/text()='".$codigo."'";
			$maisDeUmParametro = true;
		} 
		if ($nome != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."nome/text()='".$nome."'";
			$maisDeUmParametro = true;
		}
		if ($cpf != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."cpf/text()='".$cpf."'";
			$maisDeUmParametro = true;
		}
		if ($email != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."email/text()='".$email."'";
			$maisDeUmParametro = true;
		}
		if ($telefone != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."telefone/text()='".$telefone."'";
			$maisDeUmParametro = true;
		}
		if ($endereco != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."endereco/text()='".$endereco."'";
			$maisDeUmParametro = true;
		}
		if ($bairro != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."bairro/text()='".$bairro."'";
			$maisDeUmParametro = true;
		}
		if ($cep != null) {
			if ($maisDeUmParametro) $xPathQuery = $xPathQuery." and ";
			$xPathQuery = $xPathQuery."cep/text()='".$cep."'";
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
	 * excluirSubscriber
	 *
	 * Exclui da tabela tSubscriber.xml o Subscriber que possui o codigo informado.
	 * 
	 * @param string $codigo             Codigo do Subscriber a ser excluido da tabela.
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function excluirSubscriber(string $codigo) {
		$tablePath = $this->tablePathSubscriber;
		$Subscriber = $this->selectSubscriber($codigo);
		if($Subscriber == null) {
			return "ERRO: Não há Subscriber com o código ".$codigo.".";
		}
		$domSubscriber = dom_import_simplexml($Subscriber[0]);
		$domXML = $domSubscriber->parentNode->parentNode; //  documento XML
		$domSubscriber->parentNode->removeChild($domSubscriber);
		if($domXML->save($tablePath)) {
			return "Exclusão efetuada com sucesso!";
		}else {
			return "ERRO: Ao salvar modificações na tabela ".$tablePath.".";
		}
	}


	/**
	 * updateSubscriberCompleto
	 *
	 * Salva na tabela tSubscriber.xml as alteracoes no Subscriber que possui o codigo informado.
	 * 
	 * @param string $codigo          Codigo de identificacao no padrão "S0000"
	 * @param string $nome            [Opcional] Nome do Subscriber
 	 * @param string $cpf             [Opcional] CPF do Subscriber
     * @param string $email           [Opcional] Email do Subscriber
     * @param string $telefone        [Opcional] Telefone do Subscriber
     * @param string $endereco        [Opcional] Endereco do Subscriber
     * @param string $bairro          [Opcional] Bairro do Subscriber
     * @param string $cep             [Opcional] CEP do Subscriber
	 * 
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function updateSubscriberCompleto(string $codigo, string $nome = null, string $cpf = null, string $email = null, string $telefone = null, string $endereco = null, string $bairro = null, string $cep = null) {
		$tablePath = $this->tablePathSubscriber;
		$houveAlteracao = false;
		$Subscriber = $this->selectSubscriber($codigo);
		if($Subscriber == null) {
			return "ERRO: Não há Subscriber com o código ".$codigo.".";
		}
		$Subscriber = $Subscriber[0];
		if(($Subscriber->codigo != $codigo) or ($codigo == null)) {
			return "ERRO: código invalido.";
		}
		if(($Subscriber->nome != $nome) and ($nome != null)) {
			$Subscriber->nome = $nome;
			$houveAlteracao = true;
		}
		if(($Subscriber->cpf != $cpf) and ($cpf != null)) {
			if($this->verificaCPFSubscriber($cpf)){
				return "ERRO: Este CPF já está cadastrado para outro Subscriber.";
			}
			$Subscriber->cpf = $cpf;
			$houveAlteracao = true;
		}
		if(($Subscriber->email != $email) and ($email != null)) {
			if($this->verificaEmailSubscriber($email)){
				return "ERRO: Este Email já está cadastrado para outro Subscriber.";
			}
			$Subscriber->email = $email;
			$houveAlteracao = true;
		}
		if(($Subscriber->telefone != $telefone) and ($telefone != null)) {
			$Subscriber->telefone = $telefone;
			$houveAlteracao = true;
		}
		if(($Subscriber->endereco != $endereco) and ($endereco != null)) {
			$Subscriber->endereco = $endereco;
			$houveAlteracao = true;
		}
		if(($Subscriber->bairro != $bairro) and ($bairro != null)) {
			$Subscriber->bairro = $bairro;
			$houveAlteracao = true;
		}
		if(($Subscriber->cep != $cep) and ($cep != null)) {
			$Subscriber->cep = $cep;
			$houveAlteracao = true;
		}
		if(!$houveAlteracao) {
			return "Não houve alteração.";
		}
		$domSubscriber = dom_import_simplexml($Subscriber);
		$domXML = $domSubscriber->parentNode->parentNode; //  documento XML
		// Salva as alterações na tabela
		if($domXML->save($tablePath)) {
			return "Alteração efetuada com sucesso!";
		}else {
			return "ERRO: Ao salvar modificações na tabela ".$tablePath.".";
		}
	}


	/**
	 * insertSubscriber
	 *
	 * Salva na tabela tSubscriber.xml os atributos do objeto desta classe
	 * 
	 * @param void
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function insertSubscriber() {
		$msgRetorno = $this->insertSubscriberCompleto($this->nome, 
													  $this->cpf, 
													  $this->email, 
													  $this->telefone, 
													  $this->endereco, 
													  $this->bairro, 
													  $this->cep
													);
		if (strcmp($msgRetorno, "Inserção realizada com sucesso!") != 0) {
			return $msgRetorno; // Significa que deu erro.
		}
		$this->codigo = $this->getCodigoBySubscriber($this);
		$this->reg_date = $this->getSubscriberByCodigo($this->codigo)[0]->reg_date;
		return $msgRetorno;
	}


	/**
	 * updateSubscriber
	 *
	 * Salva na tabela tSubscriber.xml as alteracoes presentes nos atributos do objeto desta classe.
	 * 
	 * @param void
	 * @return string "Sucesso"|"ERRO"
	 * 
	 */
	public function updateSubscriber() {
		if($this->codigo == null) {
			return "ERRO: Código do Subscriber invalido.";
		}
		return $this->updateSubscriberCompleto($this->codigo,
											   $this->nome, 
											   $this->cpf, 
											   $this->email, 
											   $this->telefone, 
											   $this->endereco, 
											   $this->bairro, 
											   $this->cep
											);
	}


	/**
	 * clearSubscriber
	 *
	 * Limpa os atributos do objeto da classe lSubscriber.
	 * 
	 * @param void
	 * @return void
	 * 
	 */
	public function clearSubscriber() {
		$this->codigo = null;
		$this->nome = null;
		$this->cpf = null;
		$this->email = null;
		$this->telefone = null;
		$this->endereco = null;
		$this->bairro = null;
		$this->cep = null;
		$this->reg_date = null;
	}


	/**
	 * getSubscriberByCodigo
	 * 
	 * Busca os Subscribers que possuem o Codigo informado e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param string $codigo                    Codigo do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber   Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByCodigo(string $codigo) {
		$ListSimpleXMLObject = $this->selectSubscriber($codigo);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}


	/**
	 * getSubscriberByNome
	 *
	 * Busca os Subscribers que possuem o Nome informado e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param string $nome                     Nome do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber  Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByNome(string $nome) {
		$ListSimpleXMLObject = $this->selectSubscriber(null, $nome);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}


	/**
	 * getSubscriberByCPF
	 * 
	 * Busca os Subscribers que possuem o CPF informado e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param string $cpf                     CPF do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByCPF(string $cpf) {
		$ListSimpleXMLObject = $this->selectSubscriber(null, null, $cpf);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}


	/**
	 * getSubscriberByEmail
	 *
	 * Busca os Subscribers que possuem o Email informado e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param string $email                   Email do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByEmail($email) {
		$ListSimpleXMLObject = $this->selectSubscriber(null, null, null, $email);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}


	/**
	 * getSubscriberByTelefone
	 *
	 * Busca os Subscribers que possuem o Telefone informado e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param string $telefone				    Telefone do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber	Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByTelefone($telefone) {
		$ListSimpleXMLObject = $this->selectSubscriber(null, null, null, null, $telefone);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}


	/**
	 * getSubscriberByEndereco
	 *
	 * Busca os Subscribers que possuem o Endereço informado e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param float $endereco                   Endereço do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber   Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByEndereco($endereco) {
		$ListSimpleXMLObject = $this->selectSubscriber(null, null, null, null, null, $endereco);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}


	/**
	 * getSubscriberByBairro
	 *
	 * Busca os Subscribers que possuem o Bairro informado e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param string $bairro                     Bairro do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber	 Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByBairro($bairro) {
		$ListSimpleXMLObject = $this->selectSubscriber(null, null, null, null, null, null, $bairro);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}


	/**
	 * getSubscriberByCEP
	 *
	 * Busca os Subscribers que possuem o CEP informado e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param string $cep                        CEP do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber	 Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByCEP($cep) {
		$ListSimpleXMLObject = $this->selectSubscriber(null, null, null, null, null, null, null, $cep);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}

	/**
	 * getSubscriberByRegDate
	 *
	 * Busca os Subscribers que foram inseridos no sistema na data informada e retorna um array com objetos da classe lSubscriber.
	 * 
	 * @param string $reg_date                   Data de cadastro no sistema do Subscriber a ser buscado.
	 * @return lSubscriber[] $ListaSubscriber	 Retorna um array de objetos da classe lSubscriber.
	 * 
	 */
	public function getSubscriberByRegDate($reg_date) {
		$ListSimpleXMLObject = $this->selectSubscriber(null, null, null, null, null, null, null, null, $reg_date);
		$ListaSubscribers = $this->traduzSimpleXMLObjectToSubscriber($ListSimpleXMLObject);
		return $ListaSubscribers;
	}


	/**
	 * getCodigoBySubscriber
	 *
	 * Busca o Subscriber informado e retorna uma string com o seu codigo.
	 * 
	 * @param lSubscriber $oSubscriber   Objeto da classe lSubscriber.
	 * @return string $codigo            Retorna o Codigo do Subscriber (em string).
	 * 
	 */
	public function getCodigoBySubscriber(lSubscriber $oSubscriber) {
		$temp = $this->selectSubscriber (null,
										 $oSubscriber->nome,
										 $oSubscriber->cpf,
										 $oSubscriber->email,
										 $oSubscriber->telefone,
										 $oSubscriber->endereco,
										 $oSubscriber->bairro,
										 $oSubscriber->cep
										);
		$codigo = (string) $temp[0]->codigo;
		return $codigo;
	}

	/**
	 * getTabela
	 *
	 * Retorna toda a tabela tSubscriber em um array de objetos da classe lSubscriber.
	 * 
	 * @param void
	 * @return array lSubscriber       Array de objetos da classe lSubscriber
	 * 
	 */
	public function getTabela(){
		return $this->traduzSimpleXMLObjectToSubscriber($this->selectSubscriber());
	}
}

?>
