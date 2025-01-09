import json
import pika
from sentence_transformers import SentenceTransformer, util

class Recommender:
    def __init__(self, google_books_client, text_processor):
        self.google_books_client = google_books_client
        self.text_processor = text_processor
        self.embedding_model = SentenceTransformer('all-MiniLM-L6-v2')

    def get_recommendations(self, query, genres, read_books, max_requests=50):
        keywords = self.text_processor.extract_keywords(query)
        if not keywords:
            print(" [!] No keywords extracted. Using the query as a fallback.")
            keywords = [query]

        recommendations = []
        request_count = 0

        for genre in genres:
            for keyword in keywords:
                start_index = 0
                while request_count < max_requests and len(recommendations) < 100:
                    search_query = f"{keyword} subject:{genre}"
                    books = self.google_books_client.search_books(search_query, max_results=10, start_index=start_index)
                    processed_books = self._process_books(books)
                    filtered_books = self._filter_read_books(processed_books, read_books)
                    recommendations.extend(filtered_books)
                    start_index += 10
                    request_count += 1
                    if not books:
                        break

        # Генериране на embeddings и изчисляване на сходство
        query_embedding = self.embedding_model.encode(query, convert_to_tensor=True)
        for rec in recommendations:
            description = rec['description'] or ""
            rec_embedding = self.embedding_model.encode(description, convert_to_tensor=True)
            rec['similarity'] = util.cos_sim(query_embedding, rec_embedding).item()

        # Sort by similarity
        sorted_recommendations = sorted(recommendations, key=lambda x: x['similarity'], reverse=True)[:100]

        return sorted_recommendations

    def _process_books(self, books):
        results = []
        for book in books:
            volume_info = book.get("volumeInfo", {})
            results.append({
                "title": volume_info.get("title", "Unknown Title"),
                "authors": volume_info.get("authors", ["Unknown Author"]),
                "rating": volume_info.get("averageRating", 0),
                "description": volume_info.get("description", "No description available."),
                "categories": volume_info.get("categories", []),
                "isbn": self._get_isbn(volume_info),
                "similarity": 0
            })
        return results

    def _get_isbn(self, volume_info):
        identifiers = volume_info.get("industryIdentifiers", [])
        for identifier in identifiers:
            if identifier.get("type") == "ISBN_13":
                return identifier.get("identifier")
        return None

    def _filter_read_books(self, books, read_books):
        if not read_books:
            print(" [!] No read books provided. Skipping filtering step.")
            return books
        read_isbns = {book['isbn'] for book in read_books if book.get('isbn')}
        read_titles = {book['title'] for book in read_books}
        return [
            book for book in books
            if book['isbn'] not in read_isbns and book['title'] not in read_titles
        ]
    
    def send_recommendations(self, queue_name, recommendations, request_id):
        """Send recommendations to RabbitMQ with the requestId."""
        connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        channel = connection.channel()

        # Declare the queue
        channel.queue_declare(queue=queue_name, durable=True)

        # Send recommendations with requestId
        message = {
            "requestId": request_id,
            "recommendations": recommendations
        }
        channel.basic_publish(
            exchange='',
            routing_key=queue_name,
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=2  # Persistent message
            )
        )
        print(f" [x] Sent recommendations for request {request_id} to {queue_name}")
        connection.close()


def validate_payload(payload):
    """Validates the JSON payload received from RabbitMQ."""
    if not isinstance(payload, dict):
        raise ValueError("Payload must be a dictionary.")

    # Validate 'requestId'
    request_id = payload.get("requestId")
    if not request_id or not isinstance(request_id, str) or not request_id.strip():
        raise ValueError("RequestId must be a non-empty string.")

    # Validate 'query'
    query = payload.get("query")
    if not query or not isinstance(query, str) or not query.strip():
        raise ValueError("Query must be a non-empty string.")

    # Validate 'genres'
    genres = payload.get("genres", [])
    if not isinstance(genres, list) or not all(isinstance(genre, str) for genre in genres):
        raise ValueError("Genres must be a list of strings.")
    if not genres:
        print(" [!] Warning: 'genres' is empty. Defaulting to 'General'.")
        genres.append("General")

    # Validate 'read_books'
    read_books = payload.get("read_books", [])
    if not isinstance(read_books, list) or not all(isinstance(book, dict) for book in read_books):
        raise ValueError("Read books must be a list of dictionaries.")

    # Optional: Validate each book's fields
    for book in read_books:
        if "title" not in book or not isinstance(book["title"], str):
            raise ValueError("Each book must have a valid 'title'.")
        if "isbn" not in book or not isinstance(book["isbn"], str):
            raise ValueError("Each book must have a valid 'isbn'.")

    # Return cleaned payload including requestId
    return {"requestId": request_id, "query": query, "genres": genres, "read_books": read_books}


