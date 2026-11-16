-- MediConnect database schema (SQL Server 2025)
-- Run once in SSMS / sqlcmd as a login allowed to create databases.
IF DB_ID('MediConnect') IS NULL CREATE DATABASE MediConnect;
GO
USE MediConnect;
GO

CREATE TABLE Staff (
    StaffID       VARCHAR(4)   NOT NULL CONSTRAINT PK_Staff PRIMARY KEY,
    StaffName     VARCHAR(50)  NOT NULL,
    PasswordHash  VARCHAR(200) NOT NULL          -- PBKDF2 hash, never plain text
);

CREATE TABLE Doctor (
    DoctorID       VARCHAR(4)  NOT NULL CONSTRAINT PK_Doctor PRIMARY KEY,
    Name           VARCHAR(50) NOT NULL,
    Specialization VARCHAR(50) NOT NULL,
    PhoneNo        VARCHAR(15) NOT NULL,         -- string: keeps leading zeros / + prefix
    EmailID        VARCHAR(30) NOT NULL
);

CREATE TABLE Patient (
    PatientID  VARCHAR(4)    NOT NULL CONSTRAINT PK_Patient PRIMARY KEY,
    Name       VARCHAR(50)   NOT NULL,
    Address    VARCHAR(40)   NOT NULL,
    Gender     VARCHAR(1)    NOT NULL CONSTRAINT CK_Patient_Gender CHECK (Gender IN ('M','F','O')),
    Age        INT           NOT NULL CONSTRAINT CK_Patient_Age CHECK (Age BETWEEN 0 AND 120),
    BloodGroup VARCHAR(4)    NOT NULL,
    Weight     DECIMAL(5,2)  NOT NULL CONSTRAINT CK_Patient_Weight CHECK (Weight > 0),
    Height     DECIMAL(5,2)  NOT NULL CONSTRAINT CK_Patient_Height CHECK (Height > 0)
);

CREATE TABLE PatientDetails (
    PatientDetailsID   VARCHAR(4)   NOT NULL CONSTRAINT PK_PatientDetails PRIMARY KEY,
    DoctorID           VARCHAR(4)   NOT NULL CONSTRAINT FK_PD_Doctor  REFERENCES Doctor(DoctorID),
    PatientID          VARCHAR(4)   NOT NULL CONSTRAINT FK_PD_Patient REFERENCES Patient(PatientID),
    Ailment            VARCHAR(50)  NOT NULL,
    Description        VARCHAR(150) NULL,
    CheckupVisitNumber INT          NOT NULL,
    CheckupDate        DATE         NOT NULL,    -- needed for "patients on a given date" query
    ConsFees           DECIMAL(10,2) NOT NULL CONSTRAINT CK_PD_Cons  CHECK (ConsFees >= 0),
    OtherExpenses      DECIMAL(10,2) NOT NULL CONSTRAINT CK_PD_Other CHECK (OtherExpenses >= 0)
);

CREATE TABLE Bill (
    BillID           INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Bill PRIMARY KEY,
    PatientDetailsID VARCHAR(4)  NOT NULL CONSTRAINT UQ_Bill_Visit UNIQUE
                                 CONSTRAINT FK_Bill_PD REFERENCES PatientDetails(PatientDetailsID),
    PatientID        VARCHAR(4)  NOT NULL CONSTRAINT FK_Bill_Patient REFERENCES Patient(PatientID),
    BillDate         DATETIME    NOT NULL CONSTRAINT DF_Bill_Date DEFAULT GETDATE(),
    BillAmount       DECIMAL(10,2) NOT NULL CONSTRAINT CK_Bill_Amt CHECK (BillAmount >= 0),
    ModeOfPayment    VARCHAR(6)  NOT NULL CONSTRAINT CK_Bill_Mode CHECK (ModeOfPayment IN ('Cash','Card','Cheque','Online'))
);
GO
-- The default staff account (S001) is created by the application on first start.
