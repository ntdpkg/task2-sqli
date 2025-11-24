import time
import psycopg2
from faker import Faker
import random
import math
import psycopg2.extras

DB_CONFIG = {
    'host': 'localhost',
    'port': '5432',
    'user': 'todotask_user',
    'password': 'todotask_passwd',
    'database': 'todotask_db'
}

N_USERS = 1000
N_TASKS = 3000
BATCH_SIZE = 1000

fake = Faker()

def create_connection():
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except psycopg2.OperationalError as e:
        return None

def generate_in_batches(cursor, num_records, batch_size, gen_func, insert_query):
    num_batches = math.ceil(num_records / batch_size)
    for i in range(num_batches):
        current_batch_size = batch_size if i < num_batches - 1 else num_records - (i * batch_size)     
        batch_data = [gen_func() for _ in range(current_batch_size)]
        psycopg2.extras.execute_batch(cursor, insert_query, batch_data)

def generate_users(cursor, num_users, batch_size):
    query = "INSERT INTO users (username, email, password, role) VALUES (%s, %s, %s, %s)"

    generator = lambda: (
        fake.unique.user_name(), 
        fake.unique.email(), 
        fake.password(), 
        'user'
    )
    
    generate_in_batches(cursor, num_users, batch_size, generator, query)

def generate_tasks(cursor, num_tasks, num_users, batch_size):
    query = "INSERT INTO tasks (user_id, title, description) VALUES (%s, %s, %s)"
    max_user_id = num_users + 1
    
    generator = lambda: (
        random.randint(1, max_user_id),
        fake.sentence(nb_words=8),     
        fake.paragraph(nb_sentences=3)  
    )
    
    generate_in_batches(cursor, num_tasks, batch_size, generator, query)

conn = create_connection()
if not conn:
    exit(1)
    
try:
    overall_start_time = time.time()
    with conn.cursor() as cursor:
        
        cursor.execute("TRUNCATE TABLE tasks, users RESTART IDENTITY CASCADE;")
        conn.commit()

        query = "INSERT INTO users (username, email, password, role) VALUES ('admin', 'admin@admin.com', 'admin123', 'admin') ON CONFLICT (username) DO NOTHING"
        cursor.execute(query)
        conn.commit()

        generate_users(cursor, N_USERS, BATCH_SIZE)
        conn.commit()
        
        generate_tasks(cursor, N_TASKS, N_USERS, BATCH_SIZE)
        conn.commit()

    overall_end_time = time.time()

except (Exception, psycopg2.Error) as e:
    print(f"DB error: {e}")
    conn.rollback() 
finally:
    if conn:
        conn.close()
        print("Database connection closed.")
