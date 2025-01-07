from sentence_transformers import SentenceTransformer, util

class Recommender:
    def __init__(self, google_books_client, text_processor):
        self.google_books_client = google_books_client
        self.text_processor = text_processor
        self.embedding_model = SentenceTransformer('all-MiniLM-L6-v2')

    def get_recommendations(self, query, genres, read_books, max_requests=50):
        keywords = self.text_processor.extract_keywords(query)
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

        # Сортиране по сходство
        return sorted(recommendations, key=lambda x: x['similarity'], reverse=True)[:100]

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
        # Филтриране на книги, които са прочетени (по ISBN или заглавие)
        read_isbns = {book['isbn'] for book in read_books if book.get('isbn')}
        read_titles = {book['title'] for book in read_books}
        return [
            book for book in books
            if book['isbn'] not in read_isbns and book['title'] not in read_titles
        ]
