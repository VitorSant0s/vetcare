# ADR-11 — API Gateway/BFF como camada de controllers no monólito

## Contexto

A arquitetura da Etapa 1 apresenta um componente **API Gateway / BFF** separando o cliente SPA dos componentes de domínio. A implementação desta sprint é um monólito modular Spring Boot, no qual cliente e servidor já são distribuídos, mas não há um processo de gateway independente.

## Decisão

O papel de API Gateway/BFF é exercido pela camada `api` do back-end: controllers REST, DTOs de entrada/saída, validação na fronteira, tratamento uniforme de erros, CORS e autenticação/autorização. Não é introduzido um processo separado de gateway nesta sprint.

## Status

Aceita.

## Consequências

**Positivas:**
- Mantém o baixo custo operacional e a simplicidade de deploy para o porte do projeto.
- Concentra validação, segurança e contratos em um único ponto, como previsto na arquitetura.
- A separação em pacotes permite extrair o gateway no futuro sem reescrever os casos de uso.

**Negativas:**
- Não há isolamento de rede nem escalabilidade independente do gateway.
- Se o sistema evoluir para múltiplos serviços, a camada `api` precisará ser redistribuída.
