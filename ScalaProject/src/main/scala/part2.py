import sqlite3
from sqlite3 import Error
import os


"""
SQLite for python tutorials : https://www.sqlitetutorial.net/sqlite-python/
"""

def create_connection(db_file):
    """
    create a database connection to the SQLite database specified by db_file
    """
    conn = None
    try:
        conn = sqlite3.connect(db_file)
        c = conn.cursor()
        c.execute(''' DROP TABLE spells ''')
    except Error as e:
        print(e)
    return conn

def create_table(conn, create_table_sql):
    """
    create a table from the create_table_sql statement
    """
    try:
        c = conn.cursor()
        c.execute(create_table_sql)
    except Error as e:
        print(e)

def create_spell(conn, project):
    """
    Create a new spell in the database
    """
    sql = ''' INSERT INTO spells(title,level,components,spellResist)
              VALUES(?,?,?,?) '''
    cur = conn.cursor()
    cur.execute(sql, project)

def open_files(conn):
    """
    Fills the table with all the spells
    """
    spell_files_directory = os.path.dirname(os.path.dirname(os.path.dirname(os.getcwd())))

    for i in range(0,30):
        spell_file = spell_files_directory + "\\spells_thread" + str(i) + ".txt"
        if os.path.exists(spell_file):
            f=open(spell_file, "r")
            if f.mode == 'r':
                spell_collection =f.read()
                for spell_line in spell_collection.split("\n"):
                    spell_inf = spell_line.split(";")

                    if len(spell_inf) == 4:
                        spell_title = spell_inf[0]
                        spell_level = spell_inf[1]
                        spell_components = spell_inf[2]
                        spell_resist = spell_inf[3]

                        if spell_resist == "true":
                            spell_resist = True
                        else:
                            spell_resist = False

                        dang = spell_level.split(",")
                        for role_level in dang:
                            if "wizard" in role_level: #only the wizard spells are placed in database
                                spell_level = role_level.strip()[-1:]
                                spell = (spell_title,spell_level,spell_components,spell_resist)
                                create_spell(conn,spell)

def select_all_spells(conn):
    """
    Query all rows in the spells table
    """

    sql = ''' SELECT * FROM spells WHERE components = 'V' and level < 5 '''
    cur = conn.cursor()
    cur.execute(sql)
    rows = cur.fetchall()

    print(str(len(rows))+" spells found:\n")
    for row in rows:
        if row[4] == 1:
            spellResist = "true"
        else:
            spellResist = "False"

        print(row[1] + ": levels " + row[2] + " / components " + row[3] + " / spell resistance " +  spellResist)

def main():
    database = r"spells.db"

    sql_create_projects_table = """ CREATE TABLE IF NOT EXISTS spells (
                                        id integer PRIMARY KEY,
                                        title text NOT NULL,
                                        level text,
                                        components text,
                                        spellResist boolean
                                    ); """
    conn = create_connection(database)

    if conn is not None:
        create_table(conn, sql_create_projects_table)
        open_files(conn)
        select_all_spells(conn)
        conn.close()
    else:
        print("Error! cannot create the database connection.")

if __name__ == '__main__':
    main()

