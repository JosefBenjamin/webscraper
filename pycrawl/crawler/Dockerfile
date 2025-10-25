FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY crawler.py .
EXPOSE 8000
CMD ["uvicorn", "crawler:app", "--host", "0.0.0.0", "--port", "8000"]
