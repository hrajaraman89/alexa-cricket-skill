package cricketskill.io;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import java.util.Set;


class DynamoStore {
  private final DynamoDB _dynamoDB;

  // use HTTPS if you are testing locally
  protected DynamoStore(Protocol protocol) {
    AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ClientConfiguration().withProtocol(protocol));

    this._dynamoDB = new DynamoDB(client);
  }

  protected  <T> BatchGetItemOutcome batchGet(Set<T> primaryKeys, String tableName, String primaryKeyName) {
    TableKeysAndAttributes batchGetAttributes = new TableKeysAndAttributes(tableName);

    primaryKeys.stream()
        .map(i -> new KeyAttribute(primaryKeyName, i))
        .map(PrimaryKey::new)
        .forEach(batchGetAttributes::addPrimaryKey);

    return _dynamoDB.batchGetItem(batchGetAttributes);
  }
}
