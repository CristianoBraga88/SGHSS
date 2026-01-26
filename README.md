# Sistema de Gestão Hospitalar e de Serviços de Saúde

API back-end desenvolvida para o **Sistema de Gestão Hospitalar e de Serviços de Saúde (SGHSS)**, com o objetivo de centralizar o gerenciamento de pacientes, profissionais da área da saúde, autenticação de usuários e a segurança das informações hospitalares. A aplicação segue os princípios da arquitetura **RESTful**, adotando boas práticas de organização, segurança e manutenção de software.

---

## 🏗️ Arquitetura

A aplicação é estruturada em camadas, promovendo a separação de responsabilidades e facilitando a manutenção e evolução do sistema:

- **Controller** – Responsável pela exposição dos endpoints REST;
- **Use Case** – Implementação das regras de negócio;
- **Repository** – Camada de acesso e persistência de dados;
- **Entities** – Modelagem das entidades do domínio.

---

## 🚀 Tecnologias Utilizadas

As principais tecnologias e ferramentas empregadas no desenvolvimento da API são:

1. **Java 17**
2. **Spring Boot 4.0.0**
3. **Spring Security + JWT** (autenticação e autorização)
4. **Spring Data JPA + Hibernate**
5. **MySQL**
6. **Bean Validation**
7. **Lombok**

---

## 🔐 Segurança

A API utiliza autenticação baseada em **JWT (JSON Web Token)**, garantindo acesso seguro aos recursos protegidos de acordo com o perfil do usuário. O projeto adota práticas voltadas à proteção de dados sensíveis, em conformidade com a **Lei Geral de Proteção de Dados (LGPD)**.

---

## 📌 Funcionalidades Principais

- Autenticação de usuários administrativos;
- Cadastro e gerenciamento de pacientes;
- Cadastro de profissionais da área da saúde;
- Controle de agendamentos;
- Registro e consulta de prontuários;
- Auditoria das ações realizadas no sistema.

---

## 📄 Documentação

O projeto conta com documentação técnica que inclui:
- Diagrama de classes;
- Requisitos funcionais;
- Descrição da arquitetura;
- Padrões e boas práticas adotadas no desenvolvimento.

---


