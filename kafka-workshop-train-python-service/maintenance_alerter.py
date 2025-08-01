import json
from kafka import KafkaConsumer

# * This consumer is in a DIFFERENT group.
# * This means it gets its own, independent copy of all messages.
consumer = KafkaConsumer(
    'train-locations',
    bootstrap_servers='localhost:9092',
    group_id='maintenance-group',  # <<< A different group ID
    auto_offset_reset='earliest',
    value_deserializer=lambda m: json.loads(m.decode('utf-8'))
)

print("Maintenance Alerter started. Listening for stopped trains...")

for message in consumer:
    position_data = message.value
    train_id = position_data.get('trainId')
    speed = position_data.get('speedKph')

    if speed < 60:
        print(f"🚨 [MAINTENANCE ALERT] Train {train_id} is slowing! "
              f"Last known location: ({position_data.get('latitude')}, {position_data.get('longitude')})")