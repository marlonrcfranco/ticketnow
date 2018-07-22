# Web Service

## Descrição
O primeiro middleware a ser utilizado é o Web Service.
A seguir, listaremos os principais componentes da arquitetura de um serviço WEB.

- **Sistema Web**: Sistema de interação com o usuário. De maneira geral, a implementação desse sistema fica a cargo de quem for utilizar o serviço.
- **Service Requestor**: Cliente do Serviço, pode ser escrito em qualquer linguagem, neste projeto, escolhemos escrever o cliente em PHP.
- **Service Registry**: Responsável por prover acesso ao UDDI. O UDDI serve para que a aplicação cliente localize o Web Service.
- **Service Provider**: O *Provider* é responsável por implementar o serviço propriamente dito, além disso, deve torná-lo disponível, publicando no *Service Registry*.

![Arquitetura do Web Service](modelagem_WebService.png)

