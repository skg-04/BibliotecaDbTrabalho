package com.mycompany.bibliotecadb.dao;

import com.mycompany.bibliotecadb.model.Usuario;
import com.mycompany.bibliotecadb.model.Validacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDao
{

    // CREATE
    public boolean cadastrar(Usuario usuario)
    {
        Validacao.validarUsuario(usuario);

        String sql = """
            INSERT INTO USUARIO
            (nome, email, telefone)
            VALUES (?, ?, ?)
            """;

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql))
        {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefone());

            int linhasAfetadas = stmt.executeUpdate();

            return linhasAfetadas > 0;

        } catch (SQLException e)
        {
            System.out.println("Erro ao cadastrar usuário:");
            System.out.println(e.getMessage());

            return false;
        }
    }

    // READ
    public List<Usuario> listarTodos()
    {

        List<Usuario> usuarios = new ArrayList<>();

        String sql = "SELECT * FROM USUARIO";

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql); ResultSet rs = stmt.executeQuery())
        {

            while (rs.next())
            {

                Usuario usuario = new Usuario();

                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setTelefone(rs.getString("telefone"));

                usuario.setDataCadastro(
                        rs.getDate("data_cadastro").toLocalDate()
                );

                usuarios.add(usuario);
            }

        } catch (SQLException e)
        {
            System.out.println("Erro ao listar usuários:");
            System.out.println(e.getMessage());
        }

        return usuarios;
    }

    // BUSCAR POR ID
    public Usuario buscarPorId(int idUsuario)
    {

        Validacao.validarId(idUsuario);

        String sql = "SELECT * FROM USUARIO WHERE id_usuario = ?";

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql))
        {

            stmt.setInt(1, idUsuario);

            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {

                Usuario usuario = new Usuario();

                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setTelefone(rs.getString("telefone"));

                usuario.setDataCadastro(
                        rs.getDate("data_cadastro").toLocalDate()
                );

                return usuario;
            }

        } catch (SQLException e)
        {
            System.out.println("Erro ao buscar usuário:");
            System.out.println(e.getMessage());
        }

        return null;
    }

    // UPDATE
    public boolean atualizar(Usuario usuario)
    {
        Validacao.validarUsuario(usuario);
        Validacao.validarId(usuario.getIdUsuario());

        String sql = """
            UPDATE USUARIO
            SET nome = ?,
                email = ?,
                telefone = ?
            WHERE id_usuario = ?
            """;

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql))
        {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefone());
            stmt.setInt(4, usuario.getIdUsuario());

            int linhasAfetadas = stmt.executeUpdate();

            return linhasAfetadas > 0;

        } catch (SQLException e)
        {
            System.out.println("Erro ao atualizar usuário:");
            System.out.println(e.getMessage());

            return false;
        }
    }

    // DELETE
    public boolean excluir(int idUsuario)
    {
        Validacao.validarId(idUsuario);

        String sql = "DELETE FROM USUARIO WHERE id_usuario = ?";

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql))
        {
            stmt.setInt(1, idUsuario);

            int linhasAfetadas = stmt.executeUpdate();

            return linhasAfetadas > 0;

        } catch (SQLException e)
        {
            System.out.println("Erro ao excluir usuário:");
            System.out.println(e.getMessage());

            return false;
        }
    }
}
