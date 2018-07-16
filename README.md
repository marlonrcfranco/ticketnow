# SistemasDistribuidos_Middleware
O projeto se destina a criação de um sistema distribuído para compra de ingressos para um cinema.  
Este trabalho é referente ao segundo bimestre (2018) das disciplinas 23098-Sistemas Distribuídos (turma U - 2018) e 23097-Projeto e Desenvolvimento de Sistemas II (turma U - 2018) do curso de Engenharia de Computação da Universidade Federal do Rio Grande - FURG.

## Conteúdo abordado
### Middlewares
- Web Service
- Espaço de Tuplas
- Fila de Mensagens
- RMI 
### Padrões de Projeto
- MVC


## Autores

* **Marlon R C Franco** - *59782* - [marlonrcfranco](https://github.com/marlonrcfranco)
* **Vinicius Lucena dos Santos** - *85522* - [VsLucena](https://github.com/VsLucena)


### Pré-requisitos

- Linux
- Apache
- PHP 7.2
- Mozilla Firefox

### Instalação

- Extraia os arquivos do .zip

- Copie o conteúdo da pasta extraída e cole-os dentro do diretório do seu servidor (geralmente em _/var/www/html_);
	
- Entre no diretório do servidor e abra o terminal (Ctrl+Alt+T);

- Digite no terminal o comando: 
```
sudo chmod 777 -R /var/www/html
```
para libera as permissões de leitura e escrita nos arquivos recém copiados (substitua o '_/var/www/html_' pelo caminho completo do diretorio do seu servidor, caso não seja este;
	
- Abra o navegador Mozilla Firefox (necessariamente deve ser este navegador);

- Na barra de endereços, digite a URL:
```
localhost/Paginas/Login.php
```
substitua "localhost" pelo IP:PORTA da sua máquina, caso seja necessário;


## IMPORTANTE

* Erro de '_permission denied_': 
Entre no diretório do seu servidor, abra o terminal (Ctrl+Alt+T) e digite o comando para liberar as permissões de leitura e escrita nos arquivos recém copiados:
```
sudo chmod 777 -R /var/www/html
```
(substitua o '_/var/www/html_' pelo caminho completo do diretorio do servidor, caso não seja este.
