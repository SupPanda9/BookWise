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
        response = requests.get(self.BASE_URL, params=params)
        if response.status_code == 200:
            return response.json().get("items", [])
        else:
            raise Exception(f"Google Books API error: {response.status_code}")
