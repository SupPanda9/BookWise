import json
import pika
import threading
from concurrent.futures import ThreadPoolExecutor
from recommender import Recommender, validate_payload
from google_books import GoogleBooksClient
from text_processor import TextProcessor

# Define the number of parallel worker threads
MAX_WORKERS = 5

executor = ThreadPoolExecutor(max_workers=MAX_WORKERS)

# Initialize dependencies
google_books_client = GoogleBooksClient()
text_processor = TextProcessor()
recommender = Recommender(google_books_client, text_processor)

def process_request(payload):
    """Processes a single request and sends recommendations."""
    try:
        validated_payload = validate_payload(payload)
        request_id = validated_payload["requestId"]
        query = validated_payload["query"]
        genres = validated_payload["genres"]
        read_books = validated_payload["read_books"]

        print(f" [x] Processing request {request_id} in thread {threading.current_thread().name}")

        recommendations = recommender.get_recommendations(query, genres, read_books)
        recommender.send_recommendations("recommendations_response_queue", recommendations, request_id)

    except Exception as e:
        print(f" [!] Error processing request: {str(e)}")

def callback(ch, method, properties, body):
    """RabbitMQ callback function that hands off processing to the executor."""
    payload = json.loads(body)
    
    # Submit the task to the thread pool for concurrent execution
    executor.submit(process_request, payload)
    
    ch.basic_ack(delivery_tag=method.delivery_tag)

def start_consumer(queue_name):
    """Starts the RabbitMQ consumer."""
    global executor
    executor = ThreadPoolExecutor(max_workers=MAX_WORKERS)  # Create the thread pool

    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()
    channel.queue_declare(queue=queue_name, durable=True)

    channel.basic_consume(queue=queue_name, on_message_callback=callback)
    
    print(" [*] Waiting for messages. To exit press CTRL+C")
    channel.start_consuming()

if __name__ == "__main__":
    start_consumer("recommendations_request_queue")
