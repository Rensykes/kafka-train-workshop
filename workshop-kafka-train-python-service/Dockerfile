FROM python:3.12.10-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY maintenance_alerter.py .

CMD ["python", "maintenance_alerter.py"]
