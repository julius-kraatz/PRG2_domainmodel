# PRG2_domainmodel

# Allgemeines
Es handelt sich um ein einfaches Domänenmodell, welches eine Geldüberweisung (*Transaction*) von einem Sender zu einem Empfänger samt Auswahl der Währung und des Zahlungsdienstes (*Kreditkarte oder PayPal*) modelliert.

# Beschreibung der Klassen

## Money
Die Klasse *Money* repräsentiert einen Geldbetrag (*amount*) mit Währungsangabe (*currency*). Zentral bei der Gestaltung dieser Klasse war die Frage, ob der Geldbetrag und/oder die Währungsangabe eigene Validierungslogik benötigt.
- Da ein Geldbetrag grundsätzlich auch negativ sein kann (etwa zur Darstellung einer Differenz oder wenn ein Konto überzogen wurde), muss der zulässige Wertebereich hier nicht eingeschränkt werden. Somit benötigt der Geldbetrag **an dieser Stelle** keine Validierungslogik. 
- Die Währungsangabe ist ein String. Es muss verhindert werden, dass im Konstruktor von *Money* irgendwelche unzulässigen Strings übergeben werden (die in der Realität für gar keine Währung stehen).

Deshalb wurde das Factory Pattern genutzt. Der Konstruktor wurde also privat gemacht und für jede unterstützte Währung eine Factory-Methode bereitgestellt: *Money.euros*, *Money.dollars*, *Money.pounds*.

Als Folge dessen ist *Money* **keine** data class, denn private Konstruktoren und Datenklassen sind nicht gut miteinander kompatibel, da die von Datenklassen automatisch implementierte Methode *copy* den Konstruktor benötigt.

## Email
Die Klasse *Email* repräsentiert eine E-Mail-Adresse. Es handelt sich um eine *value class*, welche aus zwei Gründen existiert:
- Verhinderung der primitiven Obsession (E-Mail-Adresse als String)
- Verhinderung der Angabe einer ungültigen E-Mail-Adresse ohne @-Symbol.

E-Mail-Adressen ohne @-Symbol sind grundsätzlich ungültig. Dies ist ein wichtiger Unterschied zum vorher beschriebenen Fall in der Klasse *Money*, in der negative Geldbeträge nicht grundsätzlich als ungültig angesehen werden.

## Sender/Recipient
*Sender* und *Recipient* sind zwei *value classes*, die sich inhaltlich nicht unterscheiden. Es handelt sich um einfache Wrapper um die Klasse *Email*.

Obwohl *Email* kein primitiver Datentyp ist, würde bei Übergabe von Sender-Email und Empfänger-Email zur Erstellung einer *Transaction* das typische Problem der primitiven Obsession auftreten, denn eine versehentlicher Vertauschung der beiden Argumente würde dem Compiler nicht auffallen. Um dieses Problem zu vermeiden, wurden die Klassen *Sender* und *Recipient* eingeführt.

## TransactionId
*TransactionId* ist eine *value class*. Es handelt sich um einen einfachen Wrapper um die Klasse *UUID* (aus java.util). Die *UUID* ermöglicht es, jeder *Transaction* automatisch eine eindeutige Nummer zuzuteilen. Der Zweck der Klasse *TransactionId* ist das Verhindern der primitiven Obsession in Bezug auf die *UUID*.

## TransactionStatus
Diese *sealed class* modelliert die möglichen Zustände einer Transaktion:
- Pending
- Completed
- Failed

Die Klasse ist *sealed*, damit Anwender eine exhaustive Auswertung des Status mit *when* vornehmen können.

## Transaction
### Parameter
Die Klasse *Transaction* steht für eine Überweisung. Da eine Instanz dieser Klasse ausschließlich durch die Identität (*TransactionId*) identifiziert werden soll, kann diese Klasse keine *data class* sein, weil dann die Methode *equals* automatisch implementiert würde. In diesem Fall muss aber *equals* so implementiert werden, dass zum Vergleich nur die *TransactionId* herangezogen wird.

Die Klasse *Transaction* benötigt grundsätzlich folgende Informationen:
- Identität (*TransactionId*)
- Betrag (*Money*)
- Sender (*Sender*)
- Empfänger (*Recipient*)

Um das Strategy Pattern zur Modellierung von Verarbeitungsstrategien (*Kreditkarte oder PayPal*) umzusetzen, benötigt die Klasse auch eine konkrete Instanz der *PaymentStrategy*.

Um den Status der Transaktion zu speichern, benötigt die Klasse außerdem eine Variable des Typs *TransactionStatus*.

#### Betrag/Sender/Empfänger
Diese drei Informationen müssen bei der Erstellung einer neuen *Transaction* vom Anwender übergeben werden.

#### PaymentStrategy
Diese Information stammt ebenfalls vom Anwender. Zur besseren Lesbarkeit wurde hier jedoch das Factory Pattern verwendet. 
So muss der Anwender keine konkrete *PaymentStrategy* initialisieren und an den Konstruktor der *Transaction* übergeben.
Stattdessen ist der Konstruktor privat und die Namen der Factory-Methoden verraten die jeweilige *PaymentStrategy*: *Transaction.byCreditCard* und *Transaction.byPayPal*.

#### Identität
Die Identität (*TransactionId*) wird im privaten Konstruktor automatisch erstellt, indem die Methode *UUID.randomUUID()* aufgerufen wird.

#### Status
Der Status der Transaktion wird als private veränderliche Variable repräsentiert, die im privaten Konstruktor auf *TransactionStatus.Pending* gesetzt und nach Abschluss des Zahlungsvorgangs aktualisiert wird. Von außen kann auf diese Information durch die read-only Property *status* zugegriffen werden.

### Validierungslogik
Im Kontext einer Transaktion ergeben negative Geldbeträge keinen Sinn, da der Sender dem Empfänger Geld geben, aber niemals nehmen kann. Auch Geldbeträge von genau 0 ergeben keinen Sinn, denn eine sogenannte "Transaktion" von z.B. 0,00 Euro ist keine Transaktion.

Deshalb wurde im init-Block sichergestellt, dass nur positive Geldbeträge angegeben werden. Außerdem wurde sichergestellt, dass Sender und Empfänger nicht identisch sind.

### Die Methode *execute*
Die Methode *execute* führt den Zahlungsvorgang anhand der *PaymentStrategy* dieser Instanz aus. Das Ergebnis des Zahlungsvorgangs wird zurückgegeben. Innerhalb der Methode wird die Status-Variable anhand des Ergebnisses des Zahlungsvorgangs aktualisiert.

### Weitere Methoden
Die Methoden *toString*, *equals* und *hashCode* wurden hier manuell überschrieben, da die Klasse *Transaction* keine *data class* ist. Wichtig ist hierbei, dass *equals* zum Vergleich nur die *TransactionId* verwendet.

## PaymentResult
Diese *sealed class* modelliert die möglichen Ergebnisse des Zahlungsvorgangs an sich:
- Completed
- Failed

Die Klasse ist *sealed*, damit das Ergebnis des Zahlungsvorgangs in der Methode *execute* der Klasse *Transaction* exhaustiv per *when* überprüft werden kann. Anhand dieses Ergebnisses wird der *TransactionStatus* aktualisiert.

## PaymentStrategy
Interface, das die Implementierung einer Methode *process* verlangt, welche den Zahlungsvorgang an sich abhandelt.

Dieses Interface existiert, um das Strategy Pattern zur Modellierung von Verarbeitungsstrategien umzusetzen. Es soll also stets eine konkrete Instanz (welche dieses Interface implementiert) per Komposition an die *Transaction* übergeben werden.
 
## CreditCardPaymentStrategy
Eine Klasse, die einen Zahlungsvorgang per Kreditkarte repräsentiert. Sie implementiert das Interface *PaymentStrategy*. Da die Details eines tatsächlichen Zahlungsablaufs in diesem Projekt nicht dargestellt werden sollen, gibt sie immer *PaymentResult.Completed* zurück.

## PayPalPaymentStrategy
Eine Klasse, die einen Zahlungsvorgang per PayPal repräsentiert. Sie implementiert das Interface *PaymentStrategy*. Da die Details eines tatsächlichen Zahlungsablaufs in diesem Projekt nicht dargestellt werden sollen, gibt sie immer *PaymentResult.Failed* mit einer Fehlermeldung zurück.

# KI-Werkzeuge
Ich habe Claude Haiku 4.5 für folgende Themen verwendet:
- Erstellen einer eindeutigen Transaktions-ID (Identität). Als Antwort gab die KI mir java.util.UUID. Das habe ich umgesetzt.
- Modellierung Ergebnis Zahlungsvorgang vs. Status Transaktion. Die KI hat mir erklärt, warum man beide Fälle tatsächlich unterschiedlich modellieren sollte und mir ein Beispiel für das Strategy Pattern gezeigt, das ich modifiziert habe.
- Die KI hat mich daran erinnert, dass man neben *equals* auch *hashCode* überschreiben muss. Das habe ich umgesetzt.
- Die KI hat mir empfohlen, im Unit-Test keine konkreten Error-Strings zu überprüfen. Daran habe ich mich gehalten.

Ich habe ChatGPT folgendes gefragt:
- Wie man die Test-Methoden in Kotlin am besten benennt. Die Antwort der KI hat mich daran erinnert, dass es die empfohlene Schreibweise mit den Backticks gibt. Ich habe mich bewusst dagegen entschieden und lieber *snake_case* verwendet plus eine Beschreibung per *@DisplayName* hinzugefügt, da in der Backtick-Schreibweise manche Schriftzeichen (z.B. der Punkt) nicht verfügbar sind.
- Wie man im Unit-Test mit dem Fall umgeht, dass verschiedene Factory-Methoden auf einen privaten Konstruktor zugreifen, wenn letzterer fast die gesamte Logik enthält. Ich bin der Empfehlung gefolgt, dass man gar nicht erst versuchen soll, auf den Konstruktor zuzugreifen (auch wenn es technisch möglich ist) und stattdessen die einzelnen Factory-Methoden aufrufen soll, obwohl dadurch Duplikation entsteht.

