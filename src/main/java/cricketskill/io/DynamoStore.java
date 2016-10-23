package cricketskill.io;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;


class DynamoStore {
  private final DynamoDB _dynamoDB;
  private final String _tableName;
  private final Table _table;

  // use HTTPS if you are testing locally
  protected DynamoStore(Protocol protocol, String tableName) {
    AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ClientConfiguration().withProtocol(protocol));

    this._dynamoDB = new DynamoDB(client);
    this._tableName = tableName;
    this._table = _dynamoDB.getTable(tableName);
  }

  protected Item get(GetItemSpec spec) {
    return _table.getItem(spec);
  }

  protected void put(Item i) {
    _table.putItem(i);
  }

  protected  <T> List<Item> batchGet(Set<T> primaryKeys, String primaryKeyName) {
    TableKeysAndAttributes batchGetAttributes = new TableKeysAndAttributes(_tableName);

    primaryKeys.stream()
        .map(i -> new KeyAttribute(primaryKeyName, i))
        .map(PrimaryKey::new)
        .forEach(batchGetAttributes::addPrimaryKey);

    return _dynamoDB.batchGetItem(batchGetAttributes)
        .getTableItems()
        .getOrDefault(_tableName, Lists.newArrayList());
  }
}
