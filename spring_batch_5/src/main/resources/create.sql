-- mysql
CREATE TABLE AfterEntity (
                             documentId     varchar(100)            not null
                                 primary key,
                             username NVARCHAR(255)
);
-- mssql
CREATE TABLE BeforeEntity (
                              documentId     varchar(100)            not null
                                  constraint pk_coupon
                                      primary key,
                              username NVARCHAR(255)
);