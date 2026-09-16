package com.mycompany.bibliotecadb.model;

import java.time.LocalDate;

public class Validacao
{

    private Validacao()
    {
        // Impede criar objetos dessa classe
    }

    public static void validarUsuario(Usuario usuario)
    {

        if (usuario == null)
        {
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank())
        {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }

        if (usuario.getNome().length() > 100)
        {
            throw new IllegalArgumentException(
                    "Nome deve ter no máximo 100 caracteres."
            );
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank())
        {
            throw new IllegalArgumentException("E-mail é obrigatório.");
        }

        if (usuario.getEmail().length() > 100)
        {
            throw new IllegalArgumentException(
                    "E-mail deve ter no máximo 100 caracteres."
            );
        }

        if (!usuario.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        ))
        {
            throw new IllegalArgumentException("E-mail inválido.");
        }

        if (usuario.getTelefone() != null
                && usuario.getTelefone().length() > 20)
        {
            throw new IllegalArgumentException(
                    "Telefone deve ter no máximo 20 caracteres."
            );
        }
    }

    public static void validarLivro(Livro livro)
    {

        if (livro == null)
        {
            throw new IllegalArgumentException("Livro não pode ser nulo.");
        }

        if (livro.getTitulo() == null || livro.getTitulo().isBlank())
        {
            throw new IllegalArgumentException("Título é obrigatório.");
        }

        if (livro.getTitulo().length() > 150)
        {
            throw new IllegalArgumentException(
                    "Título deve ter no máximo 150 caracteres."
            );
        }

        if (livro.getAutor() == null || livro.getAutor().isBlank())
        {
            throw new IllegalArgumentException("Autor é obrigatório.");
        }

        if (livro.getAutor().length() > 100)
        {
            throw new IllegalArgumentException(
                    "Autor deve ter no máximo 100 caracteres."
            );
        }

        if (livro.getAnoPublicacao() < 0)
        {
            throw new IllegalArgumentException(
                    "Ano de publicação inválido."
            );
        }

        if (livro.getQuantidadeTotal() < 0)
        {
            throw new IllegalArgumentException(
                    "Quantidade total não pode ser negativa."
            );
        }

        if (livro.getQuantidadeDisponivel() < 0)
        {
            throw new IllegalArgumentException(
                    "Quantidade disponível não pode ser negativa."
            );
        }

        if (livro.getQuantidadeDisponivel()
                > livro.getQuantidadeTotal())
        {

            throw new IllegalArgumentException(
                    "Quantidade disponível não pode ser maior que a quantidade total."
            );
        }
    }

    public static void validarEmprestimo(Emprestimo emprestimo)
    {

        if (emprestimo == null)
        {
            throw new IllegalArgumentException(
                    "Empréstimo não pode ser nulo."
            );
        }

        if (emprestimo.getIdUsuario() <= 0)
        {
            throw new IllegalArgumentException(
                    "Usuário inválido."
            );
        }

        if (emprestimo.getIdLivro() <= 0)
        {
            throw new IllegalArgumentException(
                    "Livro inválido."
            );
        }

        if (emprestimo.getDataDevolucaoPrevista() == null)
        {
            throw new IllegalArgumentException(
                    "Data de devolução prevista é obrigatória."
            );
        }

        if (emprestimo.getDataDevolucaoPrevista()
                .isBefore(LocalDate.now()))
        {

            throw new IllegalArgumentException(
                    "A data de devolução prevista não pode ser anterior à data atual."
            );
        }
    }

    public static void validarId(int id)
    {

        if (id <= 0)
        {
            throw new IllegalArgumentException("ID inválido.");
        }
    }
}
