package com.mycompany.bibliotecadb.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao 
{

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=BibliotecaDB;"
            + "integratedSecurity=true;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    public static Connection conectar() 
    {

        try {

            Connection conexao = DriverManager.getConnection(URL);

            System.out.println("Conectado ao SQL Server com sucesso!");

            return conexao;

        } catch (SQLException e) {

            System.out.println("Erro ao conectar ao SQL Server:");
            System.out.println(e.getMessage());

            return null;
        }
    }
}