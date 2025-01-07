# text_processor.py
from keybert import KeyBERT

class TextProcessor:
    def __init__(self):
        self.keyword_extractor = KeyBERT()

    def extract_keywords(self, text, top_n=None):
        # Динамично определяне на броя ключови думи
        word_count = len(text.split())
        top_n = top_n or min(10, max(3, word_count // 2))  # Например 3-10 ключови думи
        keywords = self.keyword_extractor.extract_keywords(text, keyphrase_ngram_range=(1, 2), top_n=top_n)
        return [kw[0] for kw in keywords]
