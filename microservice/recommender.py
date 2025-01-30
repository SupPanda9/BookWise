import json
import pika
import random
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

        print("keywords:", keywords)
        recommendations = []
        request_count = 0

        # Generate all possible (keyword, genre) pairs
        query_pairs = [(keyword, genre) for genre in genres for keyword in keywords]

        # Shuffle to randomize query execution order
        random.shuffle(query_pairs)
        print(f"Shuffled query pairs: {query_pairs}")

        # Distribute the requests fairly
        request_limit = max(1, max_requests // len(query_pairs))  # Ensure at least 1 request per pair

        for keyword, genre in query_pairs:
            start_index = 0
            request_count_for_pair = 0  # Reset request count per query pair

            while (request_count < max_requests
                and request_count_for_pair < request_limit):
                
                search_query = f"{keyword} subject:\"{genre}\""
                books = self.google_books_client.search_books(search_query, max_results=10, start_index=start_index)
                print(f"📌 API Response for '{search_query}': {len(books)} books found.")

                if not books:
                    break  # Stop if no more books found

                processed_books = self._process_books(books)
                filtered_books = self._filter_read_books(processed_books, read_books)
                recommendations.extend(filtered_books)

                start_index += 10  # Move to the next page
                request_count += 1
                request_count_for_pair += 1  # Track per query pair

            print(f"Finished querying for '{genre}' using keyword '{keyword}'")

        # Generate embeddings and calculate similarity
        query_embedding = self.embedding_model.encode(query, convert_to_tensor=True)
        for rec in recommendations:
            description = rec['description'] or ""
            rec_embedding = self.embedding_model.encode(description, convert_to_tensor=True)
            rec['similarity'] = util.cos_sim(query_embedding, rec_embedding).item()

        unique_recommendations = {}
        for rec in recommendations:
            book_id = rec["googleBooksId"]  # Use the Google Books ID as a unique key
            if book_id not in unique_recommendations:
                unique_recommendations[book_id] = rec

        # Sort by similarity
        sorted_recommendations = sorted(unique_recommendations.values(), key=lambda x: x['similarity'], reverse=True)[:100]
        return sorted_recommendations

    def _process_books(self, books):
        results = []
        
        if not isinstance(books, list):
            print(f" [!] Error: Expected a list of books, but got {type(books)}")
            print(f" [!] Books content: {books}")  # Print the actual content for debugging
            return results  # Return empty list if books format is incorrect

        for book in books:
            if not isinstance(book, dict):
                print(f" [!] Warning: Skipping invalid book format: {book}")
                continue

            volume_info = book.get("volumeInfo", {})
            results.append({
                "googleBooksId": book.get("id", "Unknown ID"),
                "title": volume_info.get("title", "Unknown Title"),
                "authors": volume_info.get("authors", ["Unknown Author"]),
                "rating": volume_info.get("averageRating", 0),
                "description": volume_info.get("description", "No description available."),
                "categories": volume_info.get("categories", []),
                "thumbnail": volume_info.get("imageLinks", {}).get("thumbnail", ""),
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
        read_isbns = {book['bookId'] for book in read_books if 'bookId' in book}
        read_titles = {book['title'] for book in read_books}
        return [
            book for book in books
            if book['googleBooksId'] not in read_isbns and book['title'] not in read_titles
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

    request_id = payload.get("requestId")
    if not request_id or not isinstance(request_id, str) or not request_id.strip():
        raise ValueError("RequestId must be a non-empty string.")

    query = payload.get("query")
    if not query or not isinstance(query, str) or not query.strip():
        raise ValueError("Query must be a non-empty string.")

    genres = payload.get("genres", [])
    if not isinstance(genres, list) or not all(isinstance(genre, str) for genre in genres):
        raise ValueError("Genres must be a list of strings.")
    if not genres:
        print(" [!] Warning: 'genres' is empty. Defaulting to 'General'.")
        genres.append("General")

    read_books = payload.get("read_books", [])

    if not isinstance(read_books, list) or not all(isinstance(book, dict) for book in read_books):
        raise ValueError("Read books must be a list of dictionaries with 'bookId' fields.")

    for book in read_books:
        if "bookId" not in book or not isinstance(book["bookId"], str):
            raise ValueError("Each book must have a valid 'bookId' as a string.")

    return {"requestId": request_id, "query": query, "genres": genres, "read_books": read_books}
