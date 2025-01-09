import pika

def get_connection():
    """Establishes a connection to RabbitMQ."""
    connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
    return connection
