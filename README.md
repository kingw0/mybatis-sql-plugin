# mybatis-sql-plugin

## Overview
mybatis-sql-plugin is a mybatis plugin based on SQL provider mechanism.
Use it you can generate sql statement for mapper method instead of writing sql in xml.
At present, the plug-in only supports MySQL database

## Install
Maven(Inside the pom.xml)
```xml
        <dependency>
            <groupId>cn.intros.mybatis</groupId>
            <artifactId>mybatis-sql-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        
        <repository>
            <id>sonatype-snapshots</id>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
```

## How to use
You need do to add Provider annotation on your mapper method, eg.
```java
@SelectProvider(type = ResolvedSqlProvider.class)
List<Domain> select();
```
and add -parameters in your pom.xml
```xml
<compilerArgs>
    <arg>-parameters</arg>
</compilerArgs>
```

## Example
1. Select  
    First you need create a class such as below.@Tab is the table name and @Column is the column name when generate sql. 
    ```java
    @Tab(name = "t_domain")
    public class Domain {
      public static final String COLUMN_ID = "id_";

      @Column(name = "id_", update = false)
      private long id;

      @Column(name = "name_")
      private String name;

      public long id() {
        return id;
      }

      public Domain id(long id) {
        this.id = id;
        return this;
      }

      public String name() {
        return name;
      }

      public Domain name(String name) {
        this.name = name;
        return this;
      }
    }
    ```  

    ```java
   // select id_ id, name_ name from t_domain
    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> select();

   // select ... from t_domain where name_ = #{name}
     @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByCriteria(@Criterion(column = "name_") String name);

   // select ... from t_domain <if test="name != null && name != ''"> where name_ = #{name}</if>
    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByCriteriaDynamic(@Criterion(column = "name_",
            test = "name != null && name != ''") String name);

   // select ... from t_domain where name_ != #{name}
    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByCriteriaExpression(@Criterion(column = "name_", expression = "name_ != #{name}") String name);

   // select ... from t_domain where id_ in (#{id[0]}, #{id[1]}, ...)
    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByInCriteria(@Criterion(column = "id_", builder = In.class) List<Long> ids);

   // 
    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByMultiCriteria(@Criterion(column = "name_") String name, @Criterion(column = "id_", builder =
            In.class) List<Long> ids);

   // select * from t_domain order by id_ asc
    @SelectProvider(type = ResolvedSqlProvider.class)
    @Sort(column = "id_")
    List<Domain> selectOrder();
    ```
2. Insert  
    ```java
    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void insert(Domain domain);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void batchInsertList(List<Domain> domains);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void batchInsertArray(Domain... domains);
    ```
    
3. Update  
    ```java
    @UpdateProvider(type = ResolvedSqlProvider.class)
    void update(@Criterion(column = Domain.COLUMN_ID, prop = "id") Domain domain);

    @UpdateProvider(type = ResolvedSqlProvider.class)
    void batchUpdate(@Criterion(column = Domain.COLUMN_ID, prop = "id") List<Domain> domains);
    ```
    
4. Delete  
    ```java
    @DeleteProvider(type = ResolvedSqlProvider.class)
    @Tab(name = "t_domain")
    void delete(@Criterion(column = "id_") long id);
    ```
   
