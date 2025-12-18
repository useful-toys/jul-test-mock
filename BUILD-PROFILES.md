# Maven Build Profiles - SLF4J Versions

Este projeto suporta múltiplas versões da API SLF4J através de Maven Profiles.

## Profiles Disponíveis

### 1. slf4j-1.7 (Padrão)
- **Versão**: SLF4J 1.7.36 (última versão estável da linha 1.7.x)
- **Ativação**: Ativo por padrão
- **Uso**: Compatível com projetos legados e Java 8+

### 2. slf4j-2.0
- **Versão**: SLF4J 2.0.16 (última versão estável da linha 2.0.x)
- **Ativação**: Deve ser explicitamente ativado
- **Uso**: Versão moderna com melhorias de API e performance

## Como Usar

### Build Padrão (SLF4J 1.7)
```bash
mvn clean install
```

ou explicitamente:
```bash
mvn clean install -P slf4j-1.7
```

### Build com SLF4J 2.0
```bash
mvn clean install -P slf4j-2.0
```

### Verificar a versão sendo usada
```bash
mvn help:active-profiles
mvn dependency:tree | grep slf4j
```

### Executar testes com diferentes versões
```bash
# Testes com SLF4J 1.7
mvn test -P slf4j-1.7

# Testes com SLF4J 2.0
mvn test -P slf4j-2.0
```

### Build completo com ambas as versões (CI/CD)
```bash
# Build e teste com SLF4J 1.7
mvn clean verify -P slf4j-1.7

# Build e teste com SLF4J 2.0
mvn clean verify -P slf4j-2.0
```

## Integração Contínua (CI)

### GitHub Actions

Os workflows do projeto estão configurados para testar automaticamente com ambas as versões do SLF4J:

#### Build and Test Workflow
Executa em todos os PRs e pushes (exceto main):

```yaml
strategy:
  matrix:
    slf4j-profile: [slf4j-1.7, slf4j-2.0]
```

- Testa com SLF4J 1.7.36 e 2.0.16 em paralelo
- Envia cobertura de código apenas do build com SLF4J 1.7 (para evitar duplicação)
- Falha se qualquer versão falhar

#### Release Workflow
Executa ao criar uma tag de versão:

1. **Etapa de Testes**: Executa testes com ambas as versões
2. **Etapa de Deploy**: Só executa se ambas as versões passarem
3. **Artefato Publicado**: Construído com profile SLF4J 1.7 (padrão)

#### Fluxo Visual

```
┌─────────────────────────────────────────────────────────────┐
│                     Pull Request / Push                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
         ┌────────────────────────────────────────┐
         │   Maven Build, Test and Analyze        │
         └────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
              ▼                               ▼
    ┌─────────────────┐           ┌─────────────────┐
    │ Test SLF4J 1.7  │           │ Test SLF4J 2.0  │
    │   (318 tests)   │           │   (318 tests)   │
    └─────────────────┘           └─────────────────┘
              │                               │
              └───────────────┬───────────────┘
                              ▼
                    ┌───────────────────┐
                    │  Upload Coverage  │
                    │  (from SLF4J 1.7) │
                    └───────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      Release Tag (v*)                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
         ┌────────────────────────────────────────┐
         │      Test Compatibility (parallel)      │
         └────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
              ▼                               ▼
    ┌─────────────────┐           ┌─────────────────┐
    │ Test SLF4J 1.7  │           │ Test SLF4J 2.0  │
    │   (must pass)   │           │   (must pass)   │
    └─────────────────┘           └─────────────────┘
              │                               │
              └───────────────┬───────────────┘
                              ▼
                        Both Pass? ───No──> ❌ Abort
                              │
                             Yes
                              ▼
         ┌────────────────────────────────────────┐
         │    Build and Deploy to Maven Central   │
         │      (using SLF4J 1.7 profile)         │
         └────────────────────────────────────────┘
                              │
                              ▼
         ┌────────────────────────────────────────┐
         │       Create GitHub Release             │
         │       Upload Release Assets             │
         └────────────────────────────────────────┘
```

### Configuração para Outros CI/CD

Para garantir compatibilidade com ambas as versões, configure seu pipeline CI para executar:

#### GitLab CI (.gitlab-ci.yml)
```yaml
test:slf4j-1.7:
  script:
    - mvn clean verify -P slf4j-1.7

test:slf4j-2.0:
  script:
    - mvn clean verify -P slf4j-2.0
```

#### Jenkins (Jenkinsfile)
```groovy
pipeline {
    stages {
        stage('Test with SLF4J 1.7') {
            steps {
                sh 'mvn clean verify -P slf4j-1.7'
            }
        }
        stage('Test with SLF4J 2.0') {
            steps {
                sh 'mvn clean verify -P slf4j-2.0'
            }
        }
    }
}
```

#### Azure Pipelines (azure-pipelines.yml)
```yaml
strategy:
  matrix:
    slf4j_1_7:
      slf4jProfile: 'slf4j-1.7'
    slf4j_2_0:
      slf4jProfile: 'slf4j-2.0'

steps:
  - script: mvn clean verify -P $(slf4jProfile)
    displayName: 'Test with $(slf4jProfile)'
```

## Diferenças Principais entre SLF4J 1.7 e 2.0

### SLF4J 2.0
- **Java 8+** como requisito mínimo (1.7 suportava Java 5)
- API fluente melhorada
- Melhor suporte para logging estruturado
- Performance aprimorada
- Novos métodos na API (incluindo suporte a Deque no MDC)
- Service Provider baseado em `SLF4JServiceProvider` (substitui os Static Binders)

### SLF4J 1.7
- Suporte a Java 5+
- Service Provider baseado em `StaticLoggerBinder`, `StaticMarkerBinder`, e `StaticMDCBinder`
- API MDC básica sem suporte a Deque

### Compatibilidade
Este mock implementation foi projetado para funcionar com ambas as versões:

- **Para SLF4J 1.7.x**: Usa os Static Binders tradicionais
- **Para SLF4J 2.0.x**: Usa `MockServiceProvider` que implementa `SLF4JServiceProvider`
- **MDCAdapter**: Implementa todos os métodos, incluindo os métodos Deque do SLF4J 2.0
  - Os métodos Deque (`pushByKey`, `popByKey`, `getCopyOfDequeByKey`, `clearDequeByKey`) estão disponíveis mas não causam erro de compilação no SLF4J 1.7

## Troubleshooting

### Verificar qual versão está ativa
```bash
mvn dependency:tree | findstr slf4j-api
```

### Limpar build anterior ao trocar de profile
```bash
mvn clean
```

### Verificar conflitos de dependências
```bash
mvn dependency:tree -Dverbose
```

