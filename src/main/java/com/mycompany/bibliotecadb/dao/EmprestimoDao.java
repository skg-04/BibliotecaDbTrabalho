package com.mycompany.bibliotecadb.dao;

import com.mycompany.bibliotecadb.model.Emprestimo;
import com.mycompany.bibliotecadb.model.EmprestimoDetalhado;
import com.mycompany.bibliotecadb.model.Validacao;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmprestimoDao
{

    public void realizarEmprestimo(Emprestimo emprestimo)
    {
        Validacao.validarEmprestimo(emprestimo);

        String sqlVerificarLivro
                = "SELECT quantidade_disponivel FROM LIVRO WHERE id_livro = ?";

        String sqlInserirEmprestimo = """
                INSERT INTO EMPRESTIMO
                (id_usuario, id_livro, data_devolucao_prevista)
                VALUES (?, ?, ?)
                """;

        String sqlAtualizarLivro = """
                UPDATE LIVRO
                SET quantidade_disponivel = quantidade_disponivel - 1
                WHERE id_livro = ?
                """;

        Connection conexao = null;

        try
        {

            conexao = Conexao.conectar();

            if (conexao == null)
            {
                System.out.println("Não foi possível conectar ao banco.");
                return;
            }

            conexao.setAutoCommit(false);

            // Verifica disponibilidade
            try (PreparedStatement stmt
                    = conexao.prepareStatement(sqlVerificarLivro))
            {

                stmt.setInt(1, emprestimo.getIdLivro());

                ResultSet rs = stmt.executeQuery();

                if (!rs.next())
                {
                    conexao.rollback();

                    throw new IllegalArgumentException(
                            "Livro não encontrado."
                    );
                }

                int quantidadeDisponivel
                        = rs.getInt("quantidade_disponivel");

                if (quantidadeDisponivel <= 0)
                {
                    conexao.rollback();

                    throw new IllegalArgumentException(
                            "Livro indisponível para empréstimo."
                    );
                }
            }

            // Cadastra empréstimo
            try (PreparedStatement stmt
                    = conexao.prepareStatement(sqlInserirEmprestimo))
            {

                stmt.setInt(1, emprestimo.getIdUsuario());
                stmt.setInt(2, emprestimo.getIdLivro());

                stmt.setDate(
                        3,
                        java.sql.Date.valueOf(
                                emprestimo.getDataDevolucaoPrevista()
                        )
                );

                stmt.executeUpdate();
            }

            // Diminui estoque
            try (PreparedStatement stmt
                    = conexao.prepareStatement(sqlAtualizarLivro))
            {

                stmt.setInt(1, emprestimo.getIdLivro());

                stmt.executeUpdate();
            }

            conexao.commit();

            System.out.println("Empréstimo realizado com sucesso!");

        } catch (SQLException e)
        {

            System.out.println("Erro ao realizar empréstimo:");
            System.out.println(e.getMessage());

            if (conexao != null)
            {
                try
                {
                    conexao.rollback();
                } catch (SQLException ex)
                {
                    System.out.println("Erro ao desfazer operação.");
                }
            }

        } finally
        {

            if (conexao != null)
            {
                try
                {
                    conexao.setAutoCommit(true);
                    conexao.close();
                } catch (SQLException e)
                {
                    System.out.println("Erro ao fechar conexão.");
                }
            }
        }
    }

    public void devolverLivro(int idEmprestimo)
    {
        Validacao.validarId(idEmprestimo);

        String sqlBuscarEmprestimo = """
            SELECT id_livro, status
            FROM EMPRESTIMO
            WHERE id_emprestimo = ?
            """;

        String sqlDevolver = """
            UPDATE EMPRESTIMO
            SET status = 'Devolvido',
                data_devolucao_real = GETDATE()
            WHERE id_emprestimo = ?
            """;

        String sqlAtualizarLivro = """
            UPDATE LIVRO
            SET quantidade_disponivel = quantidade_disponivel + 1
            WHERE id_livro = ?
            """;

        Connection conexao = null;

        try
        {

            conexao = Conexao.conectar();

            if (conexao == null)
            {
                System.out.println("Não foi possível conectar ao banco.");
                return;
            }

            conexao.setAutoCommit(false);

            int idLivro;

            // 1 - Busca o empréstimo
            try (PreparedStatement stmt
                    = conexao.prepareStatement(sqlBuscarEmprestimo))
            {

                stmt.setInt(1, idEmprestimo);

                ResultSet rs = stmt.executeQuery();

                if (!rs.next())
                {
                    conexao.rollback();

                    throw new IllegalArgumentException(
                            "Empréstimo não encontrado."
                    );
                }

                idLivro = rs.getInt("id_livro");

                String status = rs.getString("status");

                // Impede devolver duas vezes
                if (status.equals("Devolvido"))
                {
                    conexao.rollback();

                    throw new IllegalArgumentException(
                            "Esse empréstimo já foi devolvido."
                    );
                }
            }

            // 2 - Atualiza o empréstimo
            try (PreparedStatement stmt
                    = conexao.prepareStatement(sqlDevolver))
            {

                stmt.setInt(1, idEmprestimo);

                stmt.executeUpdate();
            }

            // 3 - Devolve uma unidade ao estoque
            try (PreparedStatement stmt
                    = conexao.prepareStatement(sqlAtualizarLivro))
            {

                stmt.setInt(1, idLivro);

                stmt.executeUpdate();
            }

            // Confirma tudo
            conexao.commit();

            System.out.println("Livro devolvido com sucesso!");

        } catch (SQLException e)
        {

            System.out.println("Erro ao devolver livro:");
            System.out.println(e.getMessage());

            if (conexao != null)
            {
                try
                {
                    conexao.rollback();
                } catch (SQLException ex)
                {
                    System.out.println("Erro ao desfazer operação.");
                }
            }

        } finally
        {

            if (conexao != null)
            {
                try
                {
                    conexao.setAutoCommit(true);
                    conexao.close();
                } catch (SQLException e)
                {
                    System.out.println("Erro ao fechar conexão.");
                }
            }
        }
    }

    public List<EmprestimoDetalhado> listarEmprestimos()
    {

        List<EmprestimoDetalhado> emprestimos = new ArrayList<>();

        String sql = """
            SELECT
                E.id_emprestimo,
                U.nome AS usuario,
                L.titulo AS livro,
                E.data_emprestimo,
                E.data_devolucao_prevista,
                E.data_devolucao_real,
                E.status
            FROM EMPRESTIMO E
            INNER JOIN USUARIO U
                ON E.id_usuario = U.id_usuario
            INNER JOIN LIVRO L
                ON E.id_livro = L.id_livro
            ORDER BY E.id_emprestimo DESC
            """;

        try (
                Connection conexao = Conexao.conectar(); PreparedStatement stmt = conexao.prepareStatement(sql); ResultSet rs = stmt.executeQuery())
        {

            while (rs.next())
            {

                EmprestimoDetalhado emprestimo
                        = new EmprestimoDetalhado();

                emprestimo.setIdEmprestimo(
                        rs.getInt("id_emprestimo")
                );

                emprestimo.setNomeUsuario(
                        rs.getString("usuario")
                );

                emprestimo.setTituloLivro(
                        rs.getString("livro")
                );

                emprestimo.setDataEmprestimo(
                        rs.getDate("data_emprestimo").toLocalDate()
                );

                emprestimo.setDataDevolucaoPrevista(
                        rs.getDate("data_devolucao_prevista").toLocalDate()
                );

                // data_devolucao_real pode ser NULL
                if (rs.getDate("data_devolucao_real") != null)
                {
                    emprestimo.setDataDevolucaoReal(
                            rs.getDate("data_devolucao_real").toLocalDate()
                    );
                }

                emprestimo.setStatus(
                        rs.getString("status")
                );

                emprestimos.add(emprestimo);
            }

        } catch (SQLException e)
        {

            System.out.println("Erro ao listar empréstimos:");
            System.out.println(e.getMessage());
        }

        return emprestimos;
    }
}
