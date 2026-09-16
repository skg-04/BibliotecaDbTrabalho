package com.mycompany.bibliotecadb.servidor;

import com.mycompany.bibliotecadb.dao.EmprestimoDao;
import com.mycompany.bibliotecadb.model.Emprestimo;
import com.mycompany.bibliotecadb.model.EmprestimoDetalhado;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmprestimoHandler implements HttpHandler
{

    private final EmprestimoDao emprestimoDao = new EmprestimoDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        String metodo = exchange.getRequestMethod();

        try
        {
            if ("GET".equalsIgnoreCase(metodo))
            {
                listarEmprestimos(exchange);
            }
            else if ("POST".equalsIgnoreCase(metodo))
            {
                realizarEmprestimo(exchange);
            }
            else if ("PUT".equalsIgnoreCase(metodo))
            {
                devolverLivro(exchange);
            }
            else
            {
                enviarResposta(
                        exchange,
                        405,
                        "{\"mensagem\":\"Método não permitido.\"}"
                );
            }

        } catch (NumberFormatException e)
        {
            enviarResposta(
                    exchange,
                    400,
                    "{\"mensagem\":\"Número inválido.\"}"
            );

        } catch (IllegalArgumentException e)
        {
            enviarResposta(
                    exchange,
                    400,
                    "{\"mensagem\":\""
                    + escaparJson(e.getMessage())
                    + "\"}"
            );

        } catch (Exception e)
        {
            e.printStackTrace();

            enviarResposta(
                    exchange,
                    500,
                    "{\"mensagem\":\"Erro interno do servidor.\"}"
            );
        }
    }

    // ==========================================
    // GET - LISTAR EMPRÉSTIMOS
    // ==========================================

    private void listarEmprestimos(HttpExchange exchange)
            throws IOException
    {
        List<EmprestimoDetalhado> emprestimos =
                emprestimoDao.listarEmprestimos();

        StringBuilder json = new StringBuilder();

        json.append("[");

        for (int i = 0; i < emprestimos.size(); i++)
        {
            EmprestimoDetalhado emprestimo =
                    emprestimos.get(i);

            json.append("{");

            json.append("\"idEmprestimo\":")
                    .append(emprestimo.getIdEmprestimo())
                    .append(",");

            json.append("\"nomeUsuario\":\"")
                    .append(escaparJson(
                            emprestimo.getNomeUsuario()
                    ))
                    .append("\",");

            json.append("\"tituloLivro\":\"")
                    .append(escaparJson(
                            emprestimo.getTituloLivro()
                    ))
                    .append("\",");

            json.append("\"dataEmprestimo\":\"")
                    .append(
                            emprestimo.getDataEmprestimo() != null
                            ? emprestimo.getDataEmprestimo().toString()
                            : ""
                    )
                    .append("\",");

            json.append("\"dataDevolucaoPrevista\":\"")
                    .append(
                            emprestimo.getDataDevolucaoPrevista() != null
                            ? emprestimo.getDataDevolucaoPrevista().toString()
                            : ""
                    )
                    .append("\",");

            json.append("\"dataDevolucaoReal\":");

            if (emprestimo.getDataDevolucaoReal() == null)
            {
                json.append("null");
            }
            else
            {
                json.append("\"")
                        .append(
                                emprestimo
                                        .getDataDevolucaoReal()
                                        .toString()
                        )
                        .append("\"");
            }

            json.append(",");

            json.append("\"status\":\"")
                    .append(escaparJson(
                            emprestimo.getStatus()
                    ))
                    .append("\"");

            json.append("}");

            if (i < emprestimos.size() - 1)
            {
                json.append(",");
            }
        }

        json.append("]");

        enviarResposta(
                exchange,
                200,
                json.toString()
        );
    }

    // ==========================================
    // POST - REALIZAR EMPRÉSTIMO
    // ==========================================

    private void realizarEmprestimo(HttpExchange exchange)
            throws IOException
    {
        String corpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> dados =
                lerFormulario(corpo);

        int idUsuario = Integer.parseInt(
                dados.get("idUsuario")
        );

        int idLivro = Integer.parseInt(
                dados.get("idLivro")
        );

        LocalDate dataDevolucaoPrevista =
                LocalDate.parse(
                        dados.get("dataDevolucaoPrevista")
                );

        Emprestimo emprestimo = new Emprestimo();

        emprestimo.setIdUsuario(idUsuario);
        emprestimo.setIdLivro(idLivro);
        emprestimo.setDataDevolucaoPrevista(
                dataDevolucaoPrevista
        );

        emprestimoDao.realizarEmprestimo(
                emprestimo
        );

        enviarResposta(
                exchange,
                201,
                "{\"mensagem\":\"Empréstimo realizado com sucesso!\"}"
        );
    }

    // ==========================================
    // PUT - DEVOLVER LIVRO
    // ==========================================

    private void devolverLivro(HttpExchange exchange)
            throws IOException
    {
        int idEmprestimo =
                obterIdDevolucaoDaUrl(exchange);

        emprestimoDao.devolverLivro(
                idEmprestimo
        );

        enviarResposta(
                exchange,
                200,
                "{\"mensagem\":\"Livro devolvido com sucesso!\"}"
        );
    }

    // ==========================================
    // PEGAR ID DA URL DE DEVOLUÇÃO
    // ==========================================

    private int obterIdDevolucaoDaUrl(
            HttpExchange exchange)
    {
        String caminho =
                exchange.getRequestURI().getPath();

        /*
         * Esperamos:
         *
         * /api/emprestimos/3/devolucao
         */

        String prefixo = "/api/emprestimos/";

        if (!caminho.startsWith(prefixo))
        {
            throw new IllegalArgumentException(
                    "Empréstimo inválido."
            );
        }

        String restante =
                caminho.substring(prefixo.length());

        String[] partes =
                restante.split("/");

        if (partes.length != 2
                || !"devolucao".equals(partes[1]))
        {
            throw new IllegalArgumentException(
                    "Endereço de devolução inválido."
            );
        }

        return Integer.parseInt(partes[0]);
    }

    // ==========================================
    // LER FORMULÁRIO
    // ==========================================

    private Map<String, String> lerFormulario(
            String corpo)
    {
        Map<String, String> dados =
                new HashMap<>();

        String[] campos =
                corpo.split("&");

        for (String campo : campos)
        {
            String[] partes =
                    campo.split("=", 2);

            if (partes.length == 2)
            {
                String chave =
                        URLDecoder.decode(
                                partes[0],
                                StandardCharsets.UTF_8
                        );

                String valor =
                        URLDecoder.decode(
                                partes[1],
                                StandardCharsets.UTF_8
                        );

                dados.put(
                        chave,
                        valor
                );
            }
        }

        return dados;
    }

    // ==========================================
    // RESPOSTA HTTP
    // ==========================================

    private void enviarResposta(
            HttpExchange exchange,
            int status,
            String resposta) throws IOException
    {
        byte[] bytes =
                resposta.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                status,
                bytes.length
        );

        try (OutputStream os =
                exchange.getResponseBody())
        {
            os.write(bytes);
        }
    }

    // ==========================================
    // ESCAPAR JSON
    // ==========================================

    private String escaparJson(String texto)
    {
        if (texto == null)
        {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}