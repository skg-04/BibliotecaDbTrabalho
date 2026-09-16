package com.mycompany.bibliotecadb.dao;

import com.mycompany.bibliotecadb.model.Livro;
import com.mycompany.bibliotecadb.model.Validacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class LivroDao
{

    public boolean cadastrar(Livro livro)
    {
        Validacao.validarLivro(livro);

        String sql = """
            INSERT INTO LIVRO
            (titulo, autor, ano_publicacao,
             quantidade_total, quantidade_disponivel)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql))
        {
            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setInt(3, livro.getAnoPublicacao());
            stmt.setInt(4, livro.getQuantidadeTotal());
            stmt.setInt(5, livro.getQuantidadeDisponivel());

            int linhasAfetadas = stmt.executeUpdate();

            return linhasAfetadas > 0;
        } catch (SQLException e)
        {
            System.out.println("Erro ao cadastrar livro:");
            System.out.println(e.getMessage());

            return false;
        }
    }

    public List<Livro> listarTodos()
    {

        List<Livro> livros = new ArrayList<>();

        String sql = "SELECT * FROM LIVRO";

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql); ResultSet rs = stmt.executeQuery())
        {

            while (rs.next())
            {

                Livro livro = new Livro();

                livro.setIdLivro(rs.getInt("id_livro"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setAutor(rs.getString("autor"));
                livro.setAnoPublicacao(rs.getInt("ano_publicacao"));
                livro.setQuantidadeTotal(rs.getInt("quantidade_total"));
                livro.setQuantidadeDisponivel(rs.getInt("quantidade_disponivel"));

                livros.add(livro);
            }

        } catch (SQLException e)
        {
            System.out.println("Erro ao listar livros:");
            System.out.println(e.getMessage());
        }

        return livros;
    }

    public Livro buscarPorId(int idLivro)
    {
        Validacao.validarId(idLivro);

        String sql = "SELECT * FROM LIVRO WHERE id_livro = ?";

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql))
        {

            stmt.setInt(1, idLivro);

            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {

                Livro livro = new Livro();

                livro.setIdLivro(rs.getInt("id_livro"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setAutor(rs.getString("autor"));
                livro.setAnoPublicacao(rs.getInt("ano_publicacao"));
                livro.setQuantidadeTotal(rs.getInt("quantidade_total"));
                livro.setQuantidadeDisponivel(rs.getInt("quantidade_disponivel"));

                return livro;
            }

        } catch (SQLException e)
        {
            System.out.println("Erro ao buscar livro:");
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean atualizar(Livro livro)
    {
        Validacao.validarLivro(livro);
        Validacao.validarId(livro.getIdLivro());

        String sql = """
            UPDATE LIVRO
            SET titulo = ?,
                autor = ?,
                ano_publicacao = ?,
                quantidade_total = ?,
                quantidade_disponivel = ?
            WHERE id_livro = ?
            """;

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql))
        {
            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setInt(3, livro.getAnoPublicacao());
            stmt.setInt(4, livro.getQuantidadeTotal());
            stmt.setInt(5, livro.getQuantidadeDisponivel());
            stmt.setInt(6, livro.getIdLivro());

            int linhasAfetadas = stmt.executeUpdate();

            return linhasAfetadas > 0;
        } catch (SQLException e)
        {
            System.out.println("Erro ao atualizar livro:");
            System.out.println(e.getMessage());

            return false;
        }
    }

    public boolean excluir(int idLivro)
    {
        Validacao.validarId(idLivro);

        String sql = "DELETE FROM LIVRO WHERE id_livro = ?";

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql))
        {
            stmt.setInt(1, idLivro);

            int linhasAfetadas = stmt.executeUpdate();

            return linhasAfetadas > 0;

        } catch (SQLException e)
        {
            System.out.println("Erro ao excluir livro:");
            System.out.println(e.getMessage());

            return false;
        }
    }
}
