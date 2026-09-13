The `pg_dump -Fc` command is used to back up a PostgreSQL database into a highly flexible, compressed custom format. [1, 2, 3]

## **Core Syntax Breakdowns**

- **`pg_dump`**: The native command-line utility provided by PostgreSQL to backup a single database.
- **`F`**: Specifies the output file **F**ormat.
- **`c`**: Selects the **c**ustom format proprietary to PostgreSQL. [4, 5, 6, 7, 8]

## **Why Use `Fc`?**

- 🗜️ **Automatic Compression**: It automatically compresses the data using zlib, resulting in much smaller backup files. [9]
- 🛠️ **Flexibility on Restore**: You cannot run this file directly as a regular SQL script. Instead, you restore it using the `pg_restore` utility. This allows you to selectively restore specific tables, schemas, or data. [10, 11, 12, 13, 14]
- 🏎️ **Parallel Restores**: The custom format supports the `j` (jobs) flag in `pg_restore`, allowing you to restore data using multiple CPU cores simultaneously to save time. [15, 16, 17, 18]

## **Common Complete Examples**

**Basic Backup to a File**

```bash
pg_dump -F c -d my_database -f my_backup.dump
```

- `d my_database`: Name of the database you want to back up.
- `f my_backup.dump`: Name and path of the output file. [19, 20]

**Backup a Specific Schema** [21]

```bash
pg_dump -Fc -n public -d my_database -f public_schema.dump
```

- `n public`: Restricts the backup only to the "public" schema. [22]

**Backup from a Remote Server** [23]

```bash
pg_dump -Fc -h localhost -p5432 -U my_user -d my_database -f backup.dump
```

- `h`: Hostname of the database server.
- `p`: Port number (default is 5432).
- `U`: Database user name. [24, 25, 26, 27, 28]

## **How to Restore This Backup**

Because the file is compressed and formatted specifically for PostgreSQL, you must use `pg_restore` instead of `psql`: [29, 30]

```bash
pg_restore -Fc -d my_database backup.dump
```

To speed up a large restore by using 4 parallel processor jobs:

```bash
pg_restore -Fc -j4 -d my_database backup.dump
```

Would you like help writing a specific backup script, or do you need to see how to **extract only a single table** from a `-Fc` backup file?