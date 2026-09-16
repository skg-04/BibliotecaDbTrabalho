/*
 Navicat Premium Dump SQL

 Source Server         : Matheus
 Source Server Type    : SQL Server
 Source Server Version : 16001000 (16.00.1000)
 Source Host           : localhost\SQLEXPRESS:1433
 Source Catalog        : BibliotecaDB
 Source Schema         : dbo

 Target Server Type    : SQL Server
 Target Server Version : 16001000 (16.00.1000)
 File Encoding         : 65001

 Date: 14/09/2026 15:23:36
*/


-- ----------------------------
-- Table structure for EMPRESTIMO
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[EMPRESTIMO]') AND type IN ('U'))
	DROP TABLE [dbo].[EMPRESTIMO]
GO

CREATE TABLE [dbo].[EMPRESTIMO] (
  [id_emprestimo] int  IDENTITY(1,1) NOT NULL,
  [id_usuario] int  NOT NULL,
  [id_livro] int  NOT NULL,
  [data_emprestimo] date DEFAULT getdate() NOT NULL,
  [data_devolucao_prevista] date  NOT NULL,
  [data_devolucao_real] date  NULL,
  [status] varchar(20) COLLATE Latin1_General_CI_AS DEFAULT 'Emprestado' NOT NULL
)
GO

ALTER TABLE [dbo].[EMPRESTIMO] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of EMPRESTIMO
-- ----------------------------
SET IDENTITY_INSERT [dbo].[EMPRESTIMO] ON
GO

INSERT INTO [dbo].[EMPRESTIMO] ([id_emprestimo], [id_usuario], [id_livro], [data_emprestimo], [data_devolucao_prevista], [data_devolucao_real], [status]) VALUES (N'1', N'1', N'1', N'2026-09-14', N'2026-09-21', N'2026-09-14', N'Devolvido')
GO

SET IDENTITY_INSERT [dbo].[EMPRESTIMO] OFF
GO


-- ----------------------------
-- Table structure for LIVRO
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[LIVRO]') AND type IN ('U'))
	DROP TABLE [dbo].[LIVRO]
GO

CREATE TABLE [dbo].[LIVRO] (
  [id_livro] int  IDENTITY(1,1) NOT NULL,
  [titulo] varchar(150) COLLATE Latin1_General_CI_AS  NOT NULL,
  [autor] varchar(100) COLLATE Latin1_General_CI_AS  NOT NULL,
  [ano_publicacao] int  NULL,
  [quantidade_total] int  NOT NULL,
  [quantidade_disponivel] int  NOT NULL
)
GO

ALTER TABLE [dbo].[LIVRO] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of LIVRO
-- ----------------------------
SET IDENTITY_INSERT [dbo].[LIVRO] ON
GO

INSERT INTO [dbo].[LIVRO] ([id_livro], [titulo], [autor], [ano_publicacao], [quantidade_total], [quantidade_disponivel]) VALUES (N'1', N'O Senhor dos Anéis', N'J. R. R. Tolkien', N'1954', N'3', N'3')
GO

SET IDENTITY_INSERT [dbo].[LIVRO] OFF
GO


-- ----------------------------
-- Table structure for USUARIO
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[USUARIO]') AND type IN ('U'))
	DROP TABLE [dbo].[USUARIO]
GO

CREATE TABLE [dbo].[USUARIO] (
  [id_usuario] int  IDENTITY(1,1) NOT NULL,
  [nome] varchar(100) COLLATE Latin1_General_CI_AS  NOT NULL,
  [email] varchar(100) COLLATE Latin1_General_CI_AS  NOT NULL,
  [telefone] varchar(20) COLLATE Latin1_General_CI_AS  NULL,
  [data_cadastro] date DEFAULT getdate() NOT NULL
)
GO

ALTER TABLE [dbo].[USUARIO] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of USUARIO
-- ----------------------------
SET IDENTITY_INSERT [dbo].[USUARIO] ON
GO

INSERT INTO [dbo].[USUARIO] ([id_usuario], [nome], [email], [telefone], [data_cadastro]) VALUES (N'1', N'Matheus', N'matheus@email.com', N'15999999999', N'2026-09-14')
GO

SET IDENTITY_INSERT [dbo].[USUARIO] OFF
GO


-- ----------------------------
-- Auto increment value for EMPRESTIMO
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[EMPRESTIMO]', RESEED, 1)
GO


-- ----------------------------
-- Checks structure for table EMPRESTIMO
-- ----------------------------
ALTER TABLE [dbo].[EMPRESTIMO] ADD CONSTRAINT [CK_EMPRESTIMO_STATUS] CHECK ([status]='Devolvido' OR [status]='Emprestado')
GO


-- ----------------------------
-- Primary Key structure for table EMPRESTIMO
-- ----------------------------
ALTER TABLE [dbo].[EMPRESTIMO] ADD CONSTRAINT [PK__EMPRESTI__45FD187E7E6D2349] PRIMARY KEY CLUSTERED ([id_emprestimo])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for LIVRO
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[LIVRO]', RESEED, 1)
GO


-- ----------------------------
-- Checks structure for table LIVRO
-- ----------------------------
ALTER TABLE [dbo].[LIVRO] ADD CONSTRAINT [CK_LIVRO_QUANTIDADE] CHECK ([quantidade_total]>=(0) AND [quantidade_disponivel]>=(0) AND [quantidade_disponivel]<=[quantidade_total])
GO


-- ----------------------------
-- Primary Key structure for table LIVRO
-- ----------------------------
ALTER TABLE [dbo].[LIVRO] ADD CONSTRAINT [PK__LIVRO__C252147D957D59B7] PRIMARY KEY CLUSTERED ([id_livro])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for USUARIO
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[USUARIO]', RESEED, 1)
GO


-- ----------------------------
-- Uniques structure for table USUARIO
-- ----------------------------
ALTER TABLE [dbo].[USUARIO] ADD CONSTRAINT [UQ__USUARIO__AB6E6164E5B1A926] UNIQUE NONCLUSTERED ([email] ASC)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table USUARIO
-- ----------------------------
ALTER TABLE [dbo].[USUARIO] ADD CONSTRAINT [PK__USUARIO__4E3E04AD5B880946] PRIMARY KEY CLUSTERED ([id_usuario])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Foreign Keys structure for table EMPRESTIMO
-- ----------------------------
ALTER TABLE [dbo].[EMPRESTIMO] ADD CONSTRAINT [FK_EMPRESTIMO_USUARIO] FOREIGN KEY ([id_usuario]) REFERENCES [dbo].[USUARIO] ([id_usuario]) ON DELETE NO ACTION ON UPDATE NO ACTION
GO

ALTER TABLE [dbo].[EMPRESTIMO] ADD CONSTRAINT [FK_EMPRESTIMO_LIVRO] FOREIGN KEY ([id_livro]) REFERENCES [dbo].[LIVRO] ([id_livro]) ON DELETE NO ACTION ON UPDATE NO ACTION
GO

