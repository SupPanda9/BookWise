# # main.py
# from google_books import GoogleBooksClient
# from text_processor import TextProcessor
# from recommender import Recommender


# if __name__ == "__main__":
# # Инициализация на компонентите
#     google_books_client = GoogleBooksClient()
#     text_processor = TextProcessor()
#     recommender = Recommender(google_books_client, text_processor)

#     # Тестови данни
#     queries = [
#         "Magical school with wizards and witches with dragons",
#     ]
#     genres = ["Fantasy", "Young Adult", "Mystery"]

#     # Примерни прочетени книги (идват от бекенда)
#     read_books = [{"title": "The Shining", "isbn": "9781442450165"}, # should make error handling when no books are read
#     ]

#     # Тестова логика
#     for query in queries:
#         print(f"\nQuery: {query}")
#         print(f"Genres: {', '.join(genres)}")
#         try:
#             recommendations = recommender.get_recommendations(query, genres, read_books)
#             print("\nTop Recommendations:")
#             for i, rec in enumerate(recommendations[:10], 1):  # Показваме първите 10 резултата
#                 print(f"{i}. Title: {rec['title']}")
#                 print(f"   Authors: {', '.join(rec['authors'])}")
#                 print(f"   Similarity: {rec['similarity']:.4f}")
#                 print(f"   Description: {rec['description'][:100]}...\n")
#         except Exception as e:
#             print(f"Error: {e}")

from consumer import start_consumer

if __name__ == "__main__":
    queue_name = "recommendations_request_queue"
    start_consumer(queue_name)
