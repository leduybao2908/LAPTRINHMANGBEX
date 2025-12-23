int ledPin=11;

void setup() {
  pinMode(ledPin,HIGH);

}

void loop() {
  digitalWrite(ledPin,HIGH);
  delay(1000);
  digitalWrite(ledPin, LOW);
  delay(1000);

}
