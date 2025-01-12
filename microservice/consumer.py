import json
from config import get_connection
from recommender import validate_payload
from recommender import Recommender
from text_processor import TextProcessor
from google_books import GoogleBooksClient

def callback(ch, method, properties, body):
    """Callback function to process received messages."""
    try:
        # Decode and validate the message
        message = json.loads(body.decode())
        print(f" [x] Received message: {message}")
        validated_payload = validate_payload(message)

        # Extract fields from validated payload
        query = validated_payload['query']
        genres = validated_payload['genres']
        read_books = validated_payload['read_books']
        request_id = validated_payload['requestId']

        # Initialize dependencies
        text_processor = TextProcessor()
        google_books_client = GoogleBooksClient()
        recommender = Recommender(google_books_client, text_processor)

        # Generate recommendations
        recommendations = recommender.get_recommendations(query, genres, read_books)

        # Send recommendations back to RabbitMQ
        recommender.send_recommendations("recommendations_response_queue", recommendations, request_id)

    except ValueError as ve:
        print(f" [!] Validation error: {ve}")
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
    except json.JSONDecodeError as je:
        print(f" [!] Failed to decode JSON: {je}")
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
    except Exception as e:
        print(f" [!] Unexpected error: {e}")
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
    else:
        # Acknowledge successful processing
        ch.basic_ack(delivery_tag=method.delivery_tag)


def start_consumer(queue_name):
    """Starts consuming messages from the specified queue."""
    connection = get_connection()
    channel = connection.channel()

    # Declare the queue (ensures it exists)
    channel.queue_declare(queue=queue_name, passive=True)

    # Start consuming messages with manual acknowledgment
    channel.basic_consume(queue=queue_name, on_message_callback=callback, auto_ack=False)

    print(f" [*] Waiting for messages in '{queue_name}'. To exit press CTRL+C")
    channel.start_consuming()
