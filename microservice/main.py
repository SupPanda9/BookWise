# main.py
from google_books import GoogleBooksClient
from text_processor import TextProcessor
from recommender import Recommender

# Инициализация на компонентите
google_books_client = GoogleBooksClient()
text_processor = TextProcessor()
recommender = Recommender(google_books_client, text_processor)

# Тестови данни
queries = [
    "Books about unicorns and magical dragons",
    "Novel about the high-life in France during the French Revolution"
]
genres = ["Fantasy", "Historical Fiction"]

# Примерни прочетени книги (идват от бекенда)
read_books = [{"title": "Fluff dragon", "isbn": "9781442450165"}
]

# Тестова логика
for query in queries:
    print(f"\nQuery: {query}")
    print(f"Genres: {', '.join(genres)}")
    try:
        recommendations = recommender.get_recommendations(query, genres, read_books)
        print("\nTop Recommendations:")
        for i, rec in enumerate(recommendations[:10], 1):  # Показваме първите 10 резултата
            print(f"{i}. Title: {rec['title']}")
            print(f"   Authors: {', '.join(rec['authors'])}")
            print(f"   Similarity: {rec['similarity']:.4f}")
            print(f"   Description: {rec['description'][:100]}...\n")
    except Exception as e:
        print(f"Error: {e}")
