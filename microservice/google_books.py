import os
import requests
from dotenv import load_dotenv

# Зареждаме .env файла
load_dotenv()

class GoogleBooksClient:
    BASE_URL = "https://www.googleapis.com/books/v1/volumes"
    API_KEY = os.getenv("GOOGLE_API_KEY")

    def search_books(self, query, max_results=10, start_index=0):
        params = {
            "q": query,
            "maxResults": max_results,
            "startIndex": start_index,
            "key": self.API_KEY
        }
        print(f"🔍 Searching Google Books API with query: {query}")

        try:
            response = requests.get(self.BASE_URL, params=params)
            response.raise_for_status()  # Проверкa за HTTP грешки
            data = response.json()  # Опитваме се да декодираме JSON

            if not isinstance(data, dict) or "items" not in data:
                print(f"❌ Unexpected API response format: {data}")
                return []  # Връщаме празен списък, вместо невалидни данни

            books = data.get("items", [])
            print(f"✅ Found {len(books)} books for query: {query}")
            return books if isinstance(books, list) else []  # Уверяваме се, че books е списък

        except requests.exceptions.RequestException as e:
            print(f"❌ Google Books API request failed: {e}")
            return []  # Връщаме празен списък при грешка
